import java.util.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.lang.*;

public class Entity {
    protected static final int messageMaxLength = 512;
    protected ServiceTCP tcpserv;
    protected String id; // Au plus 8 caractères
    protected int portUDP; // < 9999
    protected boolean connected;
    protected boolean duplicated; //Indique si l'entité est en duplication
    protected InetSocketAddress dupl;
    // Multi difusion : toutes les entités d'un anneau possèdent la même addresse + port
    protected InetSocketAddress multidif; // Le port UDP de multi dif < 9999

    public boolean getConnected(){
        return connected;
    }

    public class ServiceTCP implements Runnable{

        public int portTCP;
        public InetSocketAddress next;
        protected InetSocketAddress multidif; // Le port UDP de multi dif < 9999
        public boolean duplicated;

        public ServiceTCP(int portTCP,int portUDP){
            multidif = null;
            this.portTCP = portTCP;
            try{
                next = new InetSocketAddress(InetAddress.getLocalHost(),portUDP);
            }
            catch(Exception e){
                
            }
        }

        public void run(){
            try{
                ServerSocket ss = new ServerSocket(portTCP);
                while(!duplicated) {
                    Socket s = ss.accept();
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    pw.println("WELC "+next.getAddress().getHostAddress()+" "+ next.getPort()+" "+multidif.getAddress().getHostAddress()+" "+multidif.getPort());
                    pw.flush();
                    String msg = br.readLine();
                    String[] ts = msg.split(" ");
                    if(ts.length!=3){
                        throw new ConnectionException("Mauvais comportement");
                    }
                    if(ts[0]!="NEWC"){
                        throw new ConnectionException("Mauvais comportement");
                    }
                    this.next = new InetSocketAddress(ts[1],Integer.parseInt(ts[2]));
                    pw.println("ACKC");
                    pw.flush();
                    pw.close();
                    br.close();
                    s.close();
                }
            }
            catch(Exception e){
                
            }
        }
    }

    public class ConnectionException extends Exception{
        public ConnectionException(String message){
            super(message);
        }
    }

    public Entity(String identifiant, int portTCP, int portUDP, String ip) {
        this.id = identifiant;
        this.portUDP = portUDP;
        this.tcpserv = new ServiceTCP(portTCP,portUDP);
        this.dupl = null;
        this.multidif = null;
        duplicated = false;
        connected = false;
    }


    public void parseWelc(String msg) throws ConnectionException {
        String[] ts = msg.split(" ");
        if (ts.length!=4){
            throw new ConnectionException("Mauvais comportement");
        }
        if (ts[0] != "WELC") {
            throw new ConnectionException("Mauvais comportement");
        }
        try{
            this.tcpserv.next =  new InetSocketAddress(ts[1], Integer.parseInt(ts[2]));
            this.multidif = new InetSocketAddress(ts[3], Integer.parseInt(ts[4]));
        } catch (Exception e) {
            throw new ConnectionException("Mauvais comportement");
        }
    }

    public void connect(String masterIP, int portTCP) throws ConnectionException {
        try {
            if (connected) {
                throw new ConnectionException("Déja connecté");
            }
            Socket connection = new Socket(masterIP, portTCP);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
            String msg = br.readLine();
            parseWelc(msg);
            pw.println("NEWC " + InetAddress.getLocalHost().getHostAddress() + " " + portUDP);
            pw.flush();
            pw.close();
            msg = br.readLine();
            if (msg!="ACKC\n") {
                throw new ConnectionException("Problème de connection TCP");
            }
            else{
                this.connected = true;
            }
            br.close();
            connection.close();
        } catch (Exception e) {
            throw new ConnectionException("Mauvais comportement");
        }
    }

    public String generateIDM(){
        return ""; // TO DO
    }

    public void receiveUDP(Selector selector, DatagramChannel chanel){
        try{
            ByteBuffer buff = ByteBuffer.allocate(messageMaxLength);
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()){
                SelectionKey sk = it.next();
                it.remove();
                if(sk.isReadable() && sk.channel() == chanel){
                    chanel.receive(buff);
                    String st = new String(buff.array(),0,buff.array().length);
                    buff.clear();
                    System.out.println("Message :" + st);
                } else{
                    System.out.println("Que s'est il passe");
                }
            }
        } catch (Exception e){

        }      
    }

    public void receiveAll(){
        try{
            Selector selector = Selector.open();
            DatagramChannel chanel = DatagramChannel.open();
            chanel.configureBlocking(false);
            chanel.bind(new InetSocketAddress(this.portUDP));
            chanel.register(selector,SelectionKey.OP_READ);
            while(true){
                receiveUDP(selector,chanel);
            }  
        }
        catch (Exception e){

        } 
    }
}

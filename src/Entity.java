import java.util.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.lang.*;


public class Entity {
    protected static final int messageMaxLength = 512;
    protected ServiceTCP tcpserv;
    protected long id; //Une des particularités de java : un long fait toujours 8 bytes.
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

    

    public Entity(int portTCP, int portUDP) {
        this.id = generateIDM();
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

    public long generateIDM(){//génération de l'identifiant pseudo-unique
	/* On utilisera 5 bytes de temps, donné par java, à la milliseconde près.
	   Ainsi, deux utilisateurs doivent se connecter précisément à la même milliseconde
	   pour avoir le même identifiant. De plus, les 3 autres bits seront générés aléatoirement.
	   Ainsi, deux utilisateurs se connectant à la même milliseconde n'ont qu'une probabilité
	   1/(256^3) d'avoir le même identifiant.
	 */
        long t = System.currentTimeMillis();
	byte[] randomness = new byte[3];
	Random rg = new Random();
	rg.nextBytes(randomness);
	long mod = 256;
	mod = mod*mod;
	mod = mod*mod;
	mod = mod*256;
	t = t%mod;
	t = t+(Byte.toUnsignedInt(randomness[0])*mod);
	mod *= 256;
	t = t+(Byte.toUnsignedInt(randomness[1])*mod);
	mod *=256;
	t = t+(Byte.toUnsignedInt(randomness[2])*mod);
	System.out.println(t);//Mesure de test, à retirer à terme !
	return t;
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
                    String message = new String(buff.array(),0,buff.array().length);
                    buff.clear();
                    String type = message.substring(0,3);
                    switch(type){
                        case "APPL":
                            String[] st = message.split(" ",4);
                            String idm = st[1];
                            int idApp = Integer.parseInt(st[2]);
                            String mess = st[3];
                            break;
                    }
                    
                } else{
                    System.out.println("wtf");
                }
            }
        } catch (Exception e){

        }      
    }

    public void sendUDP(String ip, int port, String request){
        try{
            DatagramSocket dso = new DatagramSocket();
            byte[] data;
            data = request.getBytes();
            DatagramPacket paquet = new DatagramPacket(data,data.length,new InetSocketAddress(ip,port));
            dso.send(paquet);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getAppRequest(int idApp, String messageApp){
        return "APPL " + generateIDM() + " " + idApp + " " + messageApp;
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


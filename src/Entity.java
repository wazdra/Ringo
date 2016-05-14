import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;

public class Entity {
    public class ServiceTCP implements Runnable{
        public int portTCP;
        public InetSocketAddress next;
        protected InetSocketAddress multidif; // Le port UDP de multi dif < 9999
        public boolean duplicated;
        public ServiceTCP(int portTCP,int portUDP){
            duplicated= false;
            multidif = null;
            this.portTCP = portTCP;
            try{next = new InetSocketAddress(InetAddress.getLocalHost(),portUDP);}
            catch(Exception e){//TO DO
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

    protected ServiceTCP tcpserv;
    protected String id; // Au plus 8 caractères
    protected int portUDP; // < 9999
    protected boolean connected;
    protected boolean duplicated; //Indique si l'entité est en duplication
    protected InetSocketAddress dupl;
    // Multi difusion : toutes les entités d'un anneau possèdent la même addresse + port
    protected InetSocketAddress multidif; // Le port UDP de multi dif < 9999


    public Entity(String identifiant, int portTCP, int portUDP, String ip) {
        this.id = identifiant;
        this.portUDP = portUDP;
        this.tcpserv = new ServiceTCP(
        this.dupl = null;
        this.multidif = null;
        duplicated = false;
        connected = false;
    }


    public void parseWelc(String msg) throws ConnectionException {
        // TO DO : modifier le next, et les ip et port de multidiff
        String[] ts = msg.split(" ");
        if (ts.length!=4){
            throw new ConnectionException("Mauvais comportement");
        }
        if (ts[0] != "WELC") {
            throw new ConnectionException("Mauvais comportement");
        }
        try{
            this.next =  new InetSocketAddress(ts[1], Integer.parseInt(ts[2]));
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
            br.close();
            connection.close();
            else{
                this.connected = true;
            }
        } catch (Exception e) {

        }
    }


    //--------------------------------
    //--------------------------------
    //--------------------------------
    // MAIN
    //--------------------------------
    //--------------------------------
    //--------------------------------

    public static void main(String[] args) {
        if (args.length == 3) {
            Entity entity;
            try {
                String identifiant = args[0];
                if (identifiant.length() > 8) {
                    System.out.println("Error while creating : the identifiant is not between 1 and 8 length");
                    return;
                }
                String ip = args[1];
                if (! Ring.checkIPv4(ip)) {
                    System.out.println("Error while creating : the ip is not correct");
                    return;
                }
                int port = Integer.parseInt(args[2]);

                try {
                    Socket socket = new Socket(ip, port);
                    socket.close();
                } catch (Exception e) {
                    System.out.println("Error : Fail to connect : " + e);
                }

            } catch (NumberFormatException e) {
                System.out.println("Error while creating : the port is not a int");
            } catch (Exception e) {
            }
        } else {
            System.out.println("Error : Wrong number of arguments.\nHere is the syntax : java Entity [identifiant] [IP] [portTCP]");
        }
    }
}

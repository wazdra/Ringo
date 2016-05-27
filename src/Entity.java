import javax.management.BadStringOperationException;
import java.util.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.lang.*;


public class Entity {
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static String ipToNW(String ip) {//Pour coder les IP sur 15 octets
        String[] parseur = ip.split("\\.");
        while(parseur[0].length()!=3){
            parseur[0] = "0"+parseur[0];
        }
        while(parseur[1].length()!=3){
            parseur[1] = "0"+parseur[1];
        }
        while(parseur[2].length()!=3){
            parseur[2] = "0"+parseur[2];
        }
        while(parseur[3].length()!=3){
            parseur[3] = "0"+parseur[3];
        }
        return parseur[0]+"."+parseur[1]+"."+parseur[2]+"."+parseur[3];
    }

    public static String portToNW(int p){
        String s = ""+p;
        while(s.length()<4){
            s = "0"+s;
        }
        return s;
    }
    public static final int messageMaxLength = 512;
    protected ArrayList<String> listIDS; //C'est la liste des messages en attente de récupération.
    protected ServiceTCP tcpserv;
    protected String ownip;
    protected String id; //Une des particularités de java : un long fait toujours 8 bytes.
    protected int portUDP; // < 9999
    protected boolean connected;
    protected boolean duplicated; //Indique si l'entité est en duplication
    protected InetSocketAddress next;
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
            try{
                multidif = new InetSocketAddress("255.255.255.255",12345);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            this.portTCP = portTCP;
            try{
                next = new InetSocketAddress(InetAddress.getLocalHost(),portUDP);
            }
            catch(Exception e){
                System.out.println("Can't get adress");
            }
        }

        public void run(){
            try{
                ServerSocket ss = new ServerSocket(portTCP);
                while(!duplicated) {
                    Socket s = ss.accept();
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    System.out.println("WELC "+ipToNW(next.getAddress().getHostAddress())+" "+ portToNW(next.getPort())+" "+ipToNW(multidif.getAddress().getHostAddress())+" "+portToNW(multidif.getPort()));
                    pw.println("WELC "+ipToNW(next.getAddress().getHostAddress())+" "+ portToNW(next.getPort())+" "+ipToNW(multidif.getAddress().getHostAddress())+" "+portToNW(multidif.getPort()));
                    pw.flush();
                    String msg = br.readLine();
                    System.out.println(msg);
                    String[] ts = msg.split(" ");
                    if(ts.length!=3){
                        throw new ConnectionException("Mauvais comportement côté serveur, mauvais nombre d'arguments");
                    }
                    if(!ts[0].equals("NEWC")){
                        throw new ConnectionException("Mauvais comportement côté serveur, attendait NEWC");
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
                e.printStackTrace();
            }
        }
    }

    

    public Entity(String ip,int portTCP, int portUDP){
        this.id = generateIDMs();
        this.portUDP = portUDP;
        this.tcpserv = new ServiceTCP(portTCP,portUDP);
        this.dupl = null;
        this.multidif = null;
        duplicated = false;
        this.ownip = ip;
        connected = false;
        Thread t = new Thread(tcpserv);
        t.start();
    }


    public void parseWelc(String msg) throws ConnectionException {
        String[] ts = msg.split(" ");
        if (ts.length!=5){
            throw new ConnectionException("Mauvais comportement côté client : mauvais nombre d'arguments");
        }
        if (!ts[0].equals("WELC")) {
            throw new ConnectionException("Mauvais comportement côté client : attendait WELC");
        }
        try{
            this.tcpserv.next =  new InetSocketAddress(ts[1], Integer.parseInt(ts[2]));
            this.next = this.tcpserv.next;
            this.multidif = new InetSocketAddress(ts[3], Integer.parseInt(ts[4]));
        } catch (Exception e) {
            throw new ConnectionException("Mauvais comportement côté client : attendait des adresses et ports.");
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
            pw.println("NEWC " + ipToNW(InetAddress.getLocalHost().getHostAddress()) + " " + portToNW(portUDP));
            pw.flush();
            msg = br.readLine();
            if (!msg.equals("ACKC")) {
                throw new ConnectionException("Problème de connection TCP");
            }
            else{
                this.connected = true;
            }
            br.close();
            connection.close();
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                throw (ConnectionException) e;
            } else {
                e.printStackTrace();
                throw new ConnectionException("Mauvais comportement");
            }
        }
    }

    public void sendDcRequest(){
        if(connected){
            sendUDP("GBYE "+id+" "+ipToNW(ownip)+" "+portToNW(portUDP)+" "+ipToNW(next.getHostName())+" "+portToNW(next.getPort()));
        }
    }

    public void disconnect(){//À appeler lorsqu'on attend une réponse à une requête de déco.
        try{
            this.next = new InetSocketAddress(InetAddress.getLocalHost(),this.portUDP);
            this.tcpserv.next = this.next;
            connected = false;
        }
        catch(Exception e){
            System.out.print("Erreur dans disconnect : ");
            e.printStackTrace();
        }
    }

    public String generateIDMs(){
        String s = new String(longToBytes(generateIDM()),0,8);
        return s;
    }
    public long generateIDM(){//génération de l'identifiant pseudo-unique. Il servira aussi bien aux machines qu'aux messages
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
	    System.out.println(new String(longToBytes(t),0,8));//Mesure de test, à retirer à terme !
	    return t;
    }

    public void receiveUDP(Selector selector, DatagramChannel chanel){//à supprimer à mon avis, il faut mettre en place ServiceUDP à la place.
        try{
            ByteBuffer buff = ByteBuffer.allocate(messageMaxLength);
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()) {
                SelectionKey sk = it.next();
                it.remove();
                if (sk.isReadable() && sk.channel() == chanel) {
                    chanel.receive(buff);
                    String message = new String(buff.array(), 0, buff.array().length);
                    buff.clear();
                    String type = message.substring(0, 3);
                    switch (type) {
                        case "APPL":
                            String[] st = message.split(" ", 4);
                            String idm = st[1];
                            int idApp = Integer.parseInt(st[2]);
                            String mess = st[3];
                            break;
                    }

                } else {
                    System.out.println("wtf");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }      
    }
    public void sendUDP(String request){
        try{
            DatagramSocket dso = new DatagramSocket();
            byte[] data;
            data = request.getBytes();
            DatagramPacket paquet = new DatagramPacket(data,data.length,next);
            dso.send(paquet);
        } catch(Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
        } 
    }

    public void handle(String str){
        if(listIDS.contains(str.substring(5,13))){//gérer messages envoyés.
            if(str.substring(0,4).equals("EYBG")){
                disconnect();
            }
            else if(str.substring(0,4).equals("TEST")){
                //Arrêter la procédure de test.
            }
            listIDS.remove(str.substring(5,13));
        }
        else {
            switch (str.substring(0, 4)) {//TO DO
                case "APPL":
                    handleAPPL(str);
                    break;
                case "WHOS":
                    sendUDP(str);
                    sendUDP("MEMB " + generateIDMs());
                    break;
                case "GBYE":
                    break;
                case "EYBG":
                    break;
                case "TEST":
                    break;
                default:
            }
        }
    }
    public void handleAPPL(String str){

    }
}


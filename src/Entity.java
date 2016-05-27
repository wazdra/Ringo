import javax.management.BadStringOperationException;
import java.util.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.lang.*;


public class Entity {
    public static char[] toTd={'1','2','3','4','5','6','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
            'Q','R','S','T','U','V','W','X','Y','Z'};
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);//On pourra utiliser Long.BYTES plutôt que 8 en java1.8 .
        buffer.putLong(x);
        buffer.flip();
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
    protected ArrayList<ListItemApp> enabledApps;
    protected ArrayList<String> listIDSdupl;
    protected ServiceTCP tcpserv;
    protected boolean testing;
    protected boolean disconnectingbis;
    protected ServiceUDP udpserv;
    String idmWhois;
    protected String ownip;
    protected boolean disconnecting;
    protected String id; //Une des particularités de java : un long fait toujours 8 bytes.
    protected int portUDP; // < 9999
    protected boolean connected;
    protected boolean duplicated; //Indique si l'entité est en duplication
    protected InetSocketAddress next;
    protected InetSocketAddress dupl;
    // Multi difusion : toutes les entités d'un anneau possèdent la même addresse + port
    protected InetSocketAddress multidif; // Le port UDP de multi dif < 9999
    protected InetSocketAddress multidifdupl;
    public boolean getConnected(){
        return connected;
    }
    public boolean cmdInteract(String cmd){
        for(ListItemApp o :enabledApps){
            if(cmd.startsWith(o.useId)){
                String str = cmd.substring(o.useId.length());//On enlève le nom de l'application de la string.
                if(str.equals("")){
                    o.app.useApp();
                }
                else{
                    o.app.useApp(str.substring(1));//On enlève l'espace
                }
                return true;
            }
        }
        return false;

    }
    public Application getApp(int index){
        return enabledApps.get(index).app;
    }
    public int getIndexApp(ListItemApp lia){
        return enabledApps.indexOf(lia);
    }


    

    public Entity(String ip,int portTCP, int portUDP){
        disconnecting = false;
        disconnectingbis = false;
        listIDSdupl = new ArrayList<>();
        testing = false;
        idmWhois = null;
        multidifdupl = null;
        this.id = generateIDM();
        this.enabledApps = new ArrayList<>();
        this.listIDS = new ArrayList<>();
        enabledApps.add(new ListItemApp("DIFF####","message",new Message(this)));
        this.portUDP = portUDP;
        this.tcpserv = new ServiceTCP(portTCP,portUDP,this);
        this.udpserv = new ServiceUDP(portUDP,next,this);
        this.dupl = null;
        try {
            setNext(Invite.getIPv4InetAddress().getHostAddress(), portUDP);
        }
        catch(Exception e){
            System.out.println("Issue fatal de réseau : pas d'adresse IP propre !");
            e.printStackTrace();
            System.exit(-1);
        }
        this.multidif = null;
        duplicated = false;
        this.ownip = ip;
        connected = false;
        Thread t = new Thread(tcpserv);
        Thread tt = new Thread(udpserv);
        t.start();
        tt.start();
    }
    public void setConnected(Boolean b){
        connected=b;
    }
    public synchronized void setMulti(String ip, int portUDP){
        multidif = new InetSocketAddress(ip,portUDP);

    }

    public synchronized void setMultiDupl(String ip,int portUDP){
        multidifdupl = new InetSocketAddress(ip,portUDP);
    }
    public synchronized void setDupl(String ip,int portUDP){
        dupl = new InetSocketAddress(ip,portUDP);
    }

    public synchronized void setNext(String ip,int portUDP){
        next = new InetSocketAddress(ip,portUDP);
        tcpserv.next = this.next;
        udpserv.next = this.next;
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
            if (msg.substring(0,4).equals("NOTC")){
                throw new ConnectionException("L'entité à laquelle vous tentez de vous connecter a été dupliquée.");
            }
            else{
                parseWelc(msg);
                pw.println("NEWC " + ipToNW(Invite.getIPv4InetAddress().getHostAddress()) + " " + portToNW(portUDP));
                pw.flush();
                msg = br.readLine();
                if (!msg.equals("ACKC")) {
                    throw new ConnectionException("Problème de connection TCP");
                }
                else{
                    this.connected = true;
                }
            }
            pw.close();
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

    public void duplication(String masterIP, int portTCP, String multiIP, int portmult) throws ConnectionException {
        try {
            if (connected) {
                throw new ConnectionException("Déja connecté");
            }
            Socket connection = new Socket(masterIP, portTCP);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
            String msg = br.readLine();
            if (msg.substring(0,4).equals("NOTC")){
                throw new ConnectionException("L'entité que vous tentez de dupliquer a déjà été dupliquée.");
            }
            else{
                parseWelc(msg);
                pw.println("DUPL " + ipToNW(Invite.getIPv4InetAddress().getHostAddress()) + " " + portToNW(portUDP) + " " + ipToNW(multiIP)+" "+portToNW(portmult));
                pw.flush();
                msg = br.readLine();
                if (!msg.equals("ACKC")) {
                    throw new ConnectionException("Problème de connection TCP");
                }
                else{
                    this.connected = true;
                }
            }
            pw.close();
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
            disconnecting = true;
            sendUDP("GBYE "+id+" "+ipToNW(ownip)+" "+portToNW(portUDP)+" "+ipToNW(next.getAddress().getHostAddress())+" "+portToNW(next.getPort()));
            sendUDPd("GBYE "+id+" "+ipToNW(ownip)+" "+portToNW(portUDP)+" "+ipToNW(next.getAddress().getHostAddress())+" "+portToNW(next.getPort()));

        }
    }

    public void disconnect(){//À appeler lorsqu'on reçoit EYBG une réponse à une requête de déco.
        try {
            if((!duplicated && disconnecting)||(duplicated && disconnecting && disconnectingbis)){
                setNext(Invite.getIPv4InetAddress().getHostAddress(),4343);
                connected = false;
                duplicated = false;
                Invite.addMsg("Vous êtes déconnecté");
            }
            else if(duplicated && disconnecting){
                disconnectingbis = true;
            }
        }
        catch(Exception e){
            System.out.print("Erreur dans disconnect : ");
            e.printStackTrace();
        }
    }

    public void sendWhoisRequest(){

    }

    public static String generateIDM(){/*Devant l'incompatibilité des strings avec certains caractères spéciaux, on
    utilisera uniquement des caractères parmis 32. Les 5 premiers sont déterminés par le temps actuel, les 3 derniers par un Random.
	 */
        long t = System.currentTimeMillis();
        byte[] b = longToBytes(t);
        char[] stringtobe = new char[8];
        for(int i = 0;i<5;i++){
            stringtobe[i]=toTd[(Math.abs((int) b[7-i]))%32];
        }
        Random rg = new Random();
        byte[] bb = new byte[3];
        rg.nextBytes(bb);
        for(int i = 0;i<3;i++){
            stringtobe[i+5]=toTd[(int) Math.abs(bb[i])%32];
        }
	    return new String(stringtobe);
    }
    public void sendUDPd(String request){
        try{
            if(duplicated) {
                DatagramSocket dso = new DatagramSocket();
                byte[] data;
                data = request.getBytes();
                DatagramPacket paquet = new DatagramPacket(data, data.length, dupl);
                dso.send(paquet);
                listIDSdupl.add(getIDM(request));
            }
        } catch(Exception e){
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

    public String getAppRequest(String idApp, String messageApp){
        String s = generateIDM();
        Invite.addMsg("Le message d'identifiant "+s+" a été envoyé");
        listIDS.add(s);
        return "APPL " + s + " " + idApp + " " + messageApp;
    }

    public static String getIDM(String msg){
        return msg.substring(5,13);
    }
    public static String getIpMsg(String msg,int offset){
        return msg.substring(offset,offset+15);
    }
    public static String getType(String msg){
        return msg.substring(0,4);
    }
    public static int getPortMsg(String msg, int offset){
        return Integer.parseInt(msg.substring(offset,offset+4));
    }

    public synchronized void handle(String str){
        System.out.println("Message reçu : "+str);
        if(listIDS.contains(getIDM(str))){//gérer messages envoyés.

            Invite.addMsg("Retour à l'expéditeur de "+getIDM(str));
            if(getType(str).equals("EYBG")){
                disconnect();
            }
            else if(getType(str).equals("TEST")){
                testing = false;
            }
            listIDS.remove(getIDM(str));
            if(listIDSdupl.contains(getIDM(str))){
                listIDSdupl.remove(getIDM(str));
            }
        }
        else if(listIDSdupl.contains(getIDM(str))){
            listIDS.add(getIDM(str));

            switch (getType(str)) {
                case "APPL":
                    Application.handle(str,this);
                    break;
                case "WHOS":
                    sendUDP(str);
                    String idmess = generateIDM();
                    sendUDP("MEMB " + idmess+" "+id+" "+ipToNW(ownip)+" "+portToNW(portUDP)+" "
                            +ipToNW(next.getHostName())+" "+portToNW(next.getPort()));
                    listIDS.add(idmess);
                    break;
                case "GBYE":
                    System.out.println(getIpMsg(str,14));
                    if(getIpMsg(str,14).equals(ipToNW(next.getHostName()))){
                        setNext(getIpMsg(str,35),getPortMsg(str,36));
                        sendUDP("EYBG "+generateIDM());
                    }
                    break;
                case "EYBG":disconnect();
                    break;
                case "TEST":
                    break;
                default:
            }
        }
        else{
            System.out.println("Message reconnu comme nouveau : "+str);
            if(duplicated){
                listIDSdupl.add(getIDM(str));
            }
            switch (getType(str)) {
                case "APPL":
                    Application.handle(str,this);
                    break;
                case "WHOS":
                    sendUDP(str);
                    sendUDPd(str);
                    String idmess = generateIDM();
                    sendUDP("MEMB " + idmess+" "+id+" "+ipToNW(ownip)+" "+portToNW(portUDP)+" "
                            +ipToNW(next.getHostName())+" "+portToNW(next.getPort()));
                    listIDS.add(idmess);
                    break;
                case "GBYE":
                    System.out.println("Message de GBYE : "+str);
                    System.out.println("Parsing de l'IP : "+getIpMsg(str,14));
                    System.out.println("IP de next retenue : "+ipToNW(next.getAddress().getHostAddress()));
                    if(getIpMsg(str,14).equals(ipToNW(next.getAddress().getHostAddress()))){
                        sendUDP("EYBG "+generateIDM());
                        setNext(getIpMsg(str,35),getPortMsg(str,36));
                    }
                    break;
                case "EYBG":disconnect();
                    break;
                case "TEST":
                    break;
                default:
            }
        }
    }
}


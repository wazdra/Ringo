import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Invite{
    static private InetAddress getIPv4InetAddress() throws SocketException, UnknownHostException {

        String os = System.getProperty("os.name").toLowerCase();

        if(os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            NetworkInterface ni = NetworkInterface.getByName("wlan0");

            Enumeration<InetAddress> ias = ni.getInetAddresses();

            InetAddress iaddress;
            do {
                iaddress = ias.nextElement();
            } while(!(iaddress instanceof Inet4Address));

            return iaddress;
        }

        return InetAddress.getLocalHost();  // for Windows and OS X it should work well
    }
    private static Entity entite;
    private static String msg;
    public static synchronized void addMsg(String message){
        msg = msg+message+"\n";
    }
    public static synchronized void clearMsg(){
        System.out.print(msg);
        msg = "";
    }
    public static synchronized boolean testMsg(){
        return msg.equals("");
    }
    public static boolean properIp(String totest){
	    try{
	        InetAddress a = InetAddress.getByName(totest);
	        return true;
	    }
	    catch(Exception e){
	        return false;
	    }
    }
    public static void main(String args[]){
        msg = "";
        try{
            entite = new Entity(InetAddress.getLocalHost().getHostAddress(),Integer.parseInt(args[0]),Integer.parseInt(args[1]));
	    }
	    catch(Exception exc){
            if(exc instanceof UnknownHostException){
                System.out.println("Impossible de trouver l'adresse de la machine.");
            }
	        System.out.println("Veuillez rentrer en arguments, dans l'ordre, le port TCP et le port UDP de votre choix.");
	        System.exit(-1);
	    }
	    String cmd;
	    String[] parseur;
	    Boolean running = true;
	    System.out.println("Bienvenue. Pour une aide à l'utilisation, taper help");
	    Scanner sc = new Scanner(System.in);
	    do{
            clearMsg();
            cmd = sc.nextLine();

            // CONNEXION
	        if(cmd.startsWith("connect")){
                parseur = cmd.split(" ");
                if(parseur.length ==3 &&
                        (properIp(parseur[1]) || parseur[1].equalsIgnoreCase("localhost"))){
                    try{
                        entite.connect(parseur[1],Integer.parseInt(parseur[2]));
                        System.out.println("Connecté avec succès au ring de "+parseur[1]);
                    }
                    catch(Exception exc){
                        if(exc instanceof ConnectionException){
                            System.out.println("La connection a été refusée : "+exc.toString());
                        }
                        else if(exc instanceof NumberFormatException){
                            System.out.println("Le deuxième argument doit être un port.");
                        }
                        else{exc.printStackTrace();}
                    }
                }
                else{
                    System.out.println("Entrez, dans l'ordre, l'IP de l'entité à laquelle vous souhaitez vous connecter, et son port TCP.");
                }
            }
            // DUPLICATION
            else if(cmd.startsWith("duplication")){
                parseur = cmd.split(" ");
                if(parseur.length ==3 &&
                        (properIp(parseur[1]) || parseur[1].equalsIgnoreCase("localhost"))){
                    try{
                        entite.duplication(parseur[1],Integer.parseInt(parseur[2]));
                        System.out.println("Connecté avec succès au ring de "+parseur[1]);
                    }
                    catch(Exception exc){
                        if(exc instanceof ConnectionException){
                            System.out.println("La connection a été refusée : "+exc.toString());
                        }
                        else if(exc instanceof NumberFormatException){
                            System.out.println("Le deuxième argument doit être un port.");
                        }
                        else{exc.printStackTrace();}
                    }
                }
                else{
                    System.out.println("Entrez, dans l'ordre, l'IP de l'entité à laquelle vous souhaitez vous dupliquer, et son port TCP.");
                }
            }
            // AUTRES COMMANDES
            else {
                switch(cmd){
                    case "locinfo" :
                        try{System.out.println("Mon IP : "+getIPv4InetAddress().getHostAddress());}catch(Exception e){}
                    case "info":
                        System.out.println("Connecté à "+entite.next.getHostName()+":"+entite.next.getPort());
                        break;
                    case "test": String tt = sc.nextLine();
                        System.out.println(Entity.getType(tt));
                        System.out.println(Entity.getIDM(tt));
                        System.out.println(Application.getIdApp(tt));
                        break;
                    case "help" : System.out.println("Commandes disponibles : \n"+
                            "- connect [ip] [port]\n"+
                            "- quit\n"+
                            "- help\n"+
                            "Pour en apprendre plus sur l'utilisation d'une commande en particuler, help peut être suivie du nom de la commande en question.");//à compléter !
                        break;
                    case "quit" : System.out.println("Au revoir !");
                        System.exit(0);
                        break;
                    case "1337" : System.out.println("Have an Easter Egg !");
                        break;
                    case "refresh" : if(testMsg()){System.out.println("Vous n'avez aucun message, patience !");}
                        break;
                    default : if(entite.cmdInteract(cmd)){}
                        else {
                        System.out.println(cmd + " n'est pas une commande correcte.\n" +
                                "Pour plus d'informations, rentrez la commande help.");
                    }
                }
            }
        }while(true);
    }
}

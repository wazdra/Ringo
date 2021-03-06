import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Invite{
    static public InetAddress getIPv4InetAddress() throws SocketException, UnknownHostException {

        String os = System.getProperty("os.name").toLowerCase();

        if(os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            NetworkInterface ni = NetworkInterface.getByName("eth0");

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
            entite = new Entity(getIPv4InetAddress().getHostAddress(),Integer.parseInt(args[0]),Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
	    }
	    catch(Exception exc){
            if(exc instanceof UnknownHostException){
                System.out.println("Impossible de trouver l'adresse de la machine.");
            }
	        System.out.println("Veuillez rentrer en arguments, dans l'ordre, le port TCP et le port UDP de votre choix, ainsi que l'adresse IP et le port de multidiffusion");
            exc.printStackTrace();
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
                if(parseur.length ==5 &&
                        (properIp(parseur[1]) || parseur[1].equalsIgnoreCase("localhost")) &&
                        (properIp(parseur[3]))){
                    try{
                        entite.duplication(parseur[1],Integer.parseInt(parseur[2]),parseur[3],Integer.parseInt(parseur[4]));
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
                        break;
                    case "info":
                        System.out.println("Connecté à "+entite.next.getAddress().getHostAddress()+":"+entite.next.getPort());
                        break;
                    case "test":
                        Boolean b = entite.test();
                        if(b){
                            System.out.println("L'anneau fonctionne correctement");
                        }
                        else{
                            System.out.println("L'anneau ne fonctionnait pas et a été dissous");
                        }
                        break;
                    case "help" : System.out.println("Commandes disponibles : \n"+
                            "- connect [ip] [port]\n"+
                            "- quit\n"+
                            "- help\n"+
                            "Pour en apprendre plus sur l'utilisation d'une commande en particuler, help peut être suivie du nom de la commande en question.");//à compléter !
                        break;
                    case "quit" :
                        if(entite.connected){
                            entite.sendDcRequest();
                            System.out.println("Veuillez attendre la déconnexion de l'anneau...");
                            while(entite.connected){
                                System.out.print(".");
                                try{Thread.sleep(1000);}catch(Exception e){e.printStackTrace();}
                            }
                            System.out.println("Au revoir !");
                            System.exit(0);
                        }
                        System.out.println("Au revoir !");
                        System.exit(0);
                        break;
                    case "disconnect":
                        entite.sendDcRequest();
                        System.out.println("Vous serez notifiez lorsque la déconnexion sera effective.");
                        break;
                    case "whois":
                        entite.sendWhoisRequest();
                        System.out.println("Vous serez notifiez des réponses de la communauté de l'anneau");
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

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Invite{
    private static String msgs;
    public synchronized void addMsg(String msg){
	msgs = msgs+msg+"\n";
    }
    public synchronized void readMsg(){
	System.out.print(msgs);
	msgs = "";
    }
    public static void main(String args[]){
	try{
	    Entity e = new Entity(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
	}
	catch(Exception exc){
	    System.out.println("Veuillez rentrer en arguments, dans l'ordre, le port TCP et le port UDP de votre choix.");
	    System.exit(-1);
	}
	String cmd;
	msgs = "";
	String[] parseur;
	Boolean running = true;
	System.out.println("Bienvenue. Pour une aide à l'utilisation, taper help");
	Scanner sc = new Scanner(System.in);
	while(running){
	    cmd = sc.nextLine();
	    if(cmd.startsWith("connect")){
		parseur = cmd.split(" ");
		switch(parseur.length){
		case 1: //InviteReseau sur constructeur mono de l'entité
		    break;
		case 2: //Connexion. Si réussie, InviteReseau sur l'entité
		    break;
		default: System.out.println("connect [ip]");
		}
	    }
	    else {
		switch(cmd){
		case "help" : System.out.println("Commandes disponibles : \n"+
						 "- connect [ip]\n"+
						 "- quit\n"+
						 "- help\n"+
						 "Pour en apprendre plus sur l'utilisation d'une commande en particuler, help peut être suivie du nom de la commande en question.");//à compléter !
		    break;
		case "quit" : System.out.println("Au revoir !");
		    running = false;
		    break;
		case "1337" : System.out.println("Have an Easter Egg !");
		}
	    }
	}
    }
}

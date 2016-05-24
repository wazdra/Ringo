import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import Entity.java;

public class Invite{
    public void InviteReseau(Entity e){
	Boolean running = true;
	String cmd;
	Scanner sc = new Scanner(System.in);
	String[] parseur;
	if(e.getConnected()){
	    System.out.println("Vous êtes connectés à un ring hôte.\n"+
			       "Vous pouvez commencer à envoyer des données.\n"+
			       "Utiliser help pour obtenir des informations sur les différentes commandes à votre disposition");
	}
	else{
	    System.out.println("Vous avez créé votre propre ring.\n"+
			       "Vous recevrez une notification lors d'une connexion extérieure.\n"+
			       "Utiliser help pour obtenir des informations sur les différentes commandes à votre disposition");
	}
	while(running){
	    System.out.print(">");
	    cmd = sc.nextLine();
	    switch(cmd){

	    }
	}
    }
    public static void main(String args[]){
	String cmd;
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

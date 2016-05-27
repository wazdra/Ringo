import java.util.*;


public class Message extends Application{
    public final int maxLength = 485;

    public Message(Entity e){
        this.entity = e;
        this.id = "DIFF####";
    }
    public static int getSizeMess(String msg){
        return Integer.parseInt(msg.substring(0,3));
    }

    public static String lengthToNW(int length){
        String str = "" + length;
        while(str.length() < 3){
            str = "0" + str;
        }
        return str;
    }

    public static String getMess(String msg, int size){
        return msg.substring(4,5+size);
    }

    public void handleApp(String msg){
        Invite.addMsg("Message reçu : "+getMess(msg,getSizeMess(msg))); // Ajout du message à transférer dans l'invite de commande
        entity.sendUDP(msg);
    }

    public void useApp(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Pour sortir du mode interactif, appuyez sur ENTREE.");
        System.out.print("Ecrivez votre message : ");
        String msg="";
        do{
            if (msg.length() > maxLength) System.out.print("Message trop long ! \n Ecrivez votre message : ");
            msg = sc.nextLine();
        } while(msg.length() > maxLength);

        if (msg.length() > 0){
            entity.sendUDP(entity.getAppRequest(this.id, "" + lengthToNW(msg.length()) + " " + msg));
        }
    }

    public void useApp(String msg){
        if (msg.length() > 0 && msg.length() <= maxLength){
            entity.sendUDP(entity.getAppRequest(this.id, "" + lengthToNW(msg.length()) + " " + msg));
        }
        else{
            System.out.println("Message trop long");
        }
    }
}

public class Message extends Application{
    public Message(Entity e){
        this.entity = e;
        this.id = "DIFF####";
    }
    public static int getSizeMess(String msg){
        return Integer.parseInt(msg.substring(0,3));
    }

    public static String getMess(String msg, int size){
        return msg.substring(4,5+size);
    }

    public void handleApp(String msg){
        Invite.addMsg("Message reçu : "+getMess(msg,getSizeMess(msg))); // Ajout du message à transférer dans l'invite de commande
        entity.sendUDP(msg);
    }
}

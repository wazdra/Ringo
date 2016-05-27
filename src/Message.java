public class Message extends Application{

    public static int getSizeMess(String msg){
        return Integer.parseInt(msg.substring(0,3));
    }

    public static String getMess(String msg, int size){
        return Integer.parseInt(msg.substring(4,5+size));
    }

    public void handleApp(String msg){
        Invite.addMsg(getMess(msg,getSizeMess(msg))); // Ajout du message à transférer dans l'invite de commande
        sendUDP(msg);
    }
}

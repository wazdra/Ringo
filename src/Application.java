import java.util.ListIterator;

public abstract class Application {
    protected String id;
    protected Entity entity;

    public static String getIdApp(String msg){
       return msg.substring(14,22);
    }



    public static void handle(String msg, Entity e){
        Boolean b = false;
        for(ListItemApp lia : e.enabledApps){
            if(lia.id.equals(getIdApp(msg))){
                lia.app.handleApp(msg);
                b = true;
                break;
            }
        }
        if(!b){
            e.sendUDP(msg);
        }
    }

    public abstract void handleApp(String msg);
    public abstract void useApp(); // Mode interactif
    public abstract void useApp(String msg); // mode non interactif
}

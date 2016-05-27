public abstract class Application {
    protected String id;
    protected Entity entity;

    public static String getIdApp(String msg){
        return msg.substring(14,22);
    }



    public static void handle(String msg, Entity e){
        int index = e.getIndexApp(getIdApp(msg));
        if (index != -1){
            Application app = e.getApp(index);
            app.handleApp(msg);
        }
        else{
            e.sendUDP(msg);
        }
    }

    public abstract void handleApp(String msg);
    public abstract void useApp(); // Mode interactif
    public abstract void useApp(String msg); // mode non interactif
}

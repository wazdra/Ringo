public abstract class Application {
    protected String id;
    protected String message;
    protected Entity entity;

    public static String getIdApp(String msg){
        return msg.substring(14,22);
    }

    public static String getMsgApp(String msg){
        return msg.substring(14,22);
    }


    public static void handle(String msg, Entity e){
        int index = e.getIndexApp(getIdApp(msg));
        if (index != -1){
            Application app = e.getApp(index);
            app.handleApp(msg);
        }
        else{
            sendUDP(msg);
        }
    }

    public abstract void handleApp(String msg);
}

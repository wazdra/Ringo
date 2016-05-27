public abstract class Application {
    protected String id;
    protected String message;
    protected Entity entity;

    public static String getIdApp(String msg){
        return "";
    }

    public static void handle(String msg, Entity e){
        String idApp = getIdApp(msg);
        int index = e.getIndexApp(idApp);
        if (index != -1){
            Application app = e.getApp(index);

        }
    }
}

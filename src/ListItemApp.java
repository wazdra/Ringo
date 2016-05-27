/**
 * Created by anatole on 27/05/2016.
 */
public class ListItemApp {
    public String id;
    public Application app;
    public String useId;
    public ListItemApp(String i, String ui,Application a){
        this.useId = ui;
        this.id = i;
        this.app = a;
    }
    public boolean equals(Object o){
        if(o instanceof ListItemApp){
            return o==this;
        }
        else if(o instanceof String){
            return o.equals(id);
        }
        return false;
    }
}

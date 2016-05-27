import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;


public class ServiceUDP implements Runnable{
    public DatagramSocket dso;
    public int port;
    public InetSocketAddress next;
    public Entity e;
    public boolean running;
    public ServiceUDP(int portUDP,InetSocketAddress n, Entity e){
        this.port = portUDP;
        running = true;
        next = n;
        this.e = e;
    }
    public void setPort(int p){
        port = p;
        try{dso = new DatagramSocket(port);}
        catch(Exception e){e.printStackTrace();}
    }
    public void run(){
        try{
            dso = new DatagramSocket(port);
            byte[] buf = new byte[e.messageMaxLength];
            DatagramPacket dp = new DatagramPacket(buf,buf.length);
            while(running){
                dso.receive(dp);
                String str = new String(dp.getData(),0,dp.getLength());
                e.handle(str);//Renvoie la string dans l'entité.
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}

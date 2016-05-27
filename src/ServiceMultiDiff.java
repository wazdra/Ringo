import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class ServiceMultiDiff implements Runnable{
    private MulticastSocket ms;
    private Entity e;
    public ServiceMultiDiff(InetSocketAddress isa, Entity e){
        try{
            this.e = e;
            ms = new MulticastSocket(isa.getPort());
            ms.joinGroup(isa.getAddress());
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
    }
    public void swapMC(InetSocketAddress isa){
        try {
            ms = new MulticastSocket(isa.getPort());
            ms.joinGroup(isa.getAddress());
        }catch(Exception e){e.printStackTrace();}
    }
    public void run(){
        byte[]data=new byte[512];
        DatagramPacket paquet=new DatagramPacket(data,data.length);
        while(true){
            try {
                ms.receive(paquet);
                String str = new String (paquet.getData(),0,paquet.getLength());
                if(str.equals("DOWN")){
                    e.broken();
                }

            }catch(Exception e){e.printStackTrace();}
        }
    }

}

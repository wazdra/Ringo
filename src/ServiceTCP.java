import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceTCP implements Runnable{
    public int portTCP;
    public int portUDP;
    public Entity ent;
    public InetSocketAddress next;
    protected InetSocketAddress multidif; // Le port UDP de multi dif < 9999
    public boolean duplicated;
    public ServiceTCP(int portTCP,int portUDP, Entity en){
        ent = en;
        try{
            multidif = new InetSocketAddress("255.255.255.255",12345);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.portTCP = portTCP;
    }

    public void run(){
        try{
            ServerSocket ss = new ServerSocket(portTCP);
            while(true) {
                Socket s = ss.accept();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                if (duplicated){
                    pw.println("NOTC");
                    pw.flush();
                }
                else{
                    pw.println("WELC "+Entity.ipToNW(next.getAddress().getHostAddress())+" "+ Entity.portToNW(next.getPort())+" "+
                            Entity.ipToNW(multidif.getAddress().getHostAddress())+" "+Entity.portToNW(multidif.getPort()));
                    pw.flush();
                    String msg = br.readLine();
                    String[] ts = msg.split(" ");
                    if(ts.length!=3){
                        throw new ConnectionException("Mauvais comportement côté serveur, mauvais nombre d'arguments");
                    }
                    if(ts[0].equals("NEWC")) {
                        ent.setNext(ts[1], Integer.parseInt(ts[2]));
                        pw.println("ACKC");
                        ent.setConnected(true);
                        pw.flush();
                    }
                    else if(ts[0].equals("DUPL")){
                        ent.setDupl(ts[1], Integer.parseInt(ts[2]));
                        ent.setMultiDupl(ts[3],Integer.parseInt(ts[3]));
                        pw.println("ACKD "+Entity.portToNW(portUDP));
                    }
                    else {
                        throw new ConnectionException("Mauvais comportement côté serveur, attendait NEWC");
                    }

                }
                pw.close();
                br.close();
                s.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
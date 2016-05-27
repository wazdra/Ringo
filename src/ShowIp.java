import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class ShowIp {

    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();

        while (eni.hasMoreElements()) {
            NetworkInterface ni = eni.nextElement();
            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();


            while (inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();
                if (!ia.isLinkLocalAddress()) {
                    System.out.println("Interface: " + ni.getName() + "   IP: " + ia.getHostAddress());

                }
            }
        }
    }

}
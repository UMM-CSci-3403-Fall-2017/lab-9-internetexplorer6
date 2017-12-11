package segmentedfilesystem;
import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        try {
//            InetSocketAddress address = new InetSocketAddress("heartofgold.morris.umn.edu",6014);
//            socket.bind(address);
            InetAddress address = InetAddress.getByName("heartofgold.morris.umn.edu");
            byte[] startbuffer = new byte[1028];
            DatagramPacket emptyPacket = new DatagramPacket(startbuffer,1028, address, 6014);
            socket.send(emptyPacket);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        RetrievePacketThread quote = new RetrievePacketThread(socket);
        quote.start();
    }
}

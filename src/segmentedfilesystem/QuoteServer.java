package segmentedfilesystem;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class QuoteServer {
    public static void main(String[] args) throws IOException {
        try {
            DatagramSocket socket = new DatagramSocket(6014);
            byte[] startbuffer = new byte[1024];
            DatagramPacket emptyPacket = new DatagramPacket(startbuffer,1024);
            socket.send(emptyPacket);
        }
        catch(SocketException e){
            e.printStackTrace();
        }
        new QuoteServerThread().start();

    }
}

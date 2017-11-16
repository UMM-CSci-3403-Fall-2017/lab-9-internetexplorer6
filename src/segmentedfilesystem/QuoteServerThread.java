package segmentedfilesystem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;


public class QuoteServerThread {
    public QuoteServerThread() throws IOException {
    }
    DatagramSocket socket = new DatagramSocket();
    BufferedReader in;
    public QuoteServerThread(String name) throws IOException {
        //super(name);
        socket = new DatagramSocket(6014);

        try {
            in = new BufferedReader(new FileReader("ayyyy-LMAO.txt"));
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }
}

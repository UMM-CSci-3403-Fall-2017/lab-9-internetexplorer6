package segmentedfilesystem;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Stack;


public class QuoteServerThread extends Thread{
    public QuoteServerThread() throws IOException {
        this("QuoteServer");
    }
    Stack<byte[]> packetStruct = new Stack<>();
    int stackSize = 0;
    DatagramSocket socket = new DatagramSocket();
    BufferedReader in;


    public QuoteServerThread(String name) throws IOException {
            socket = new DatagramSocket(6014);
    }
    public boolean foundFiles = false;
    public void run() {
        try {

            while (!foundFiles) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                packetStruct.push(buf);
                stackSize++;
            }
            DatagramPacket[] packetArray = new DatagramPacket[stackSize];
            while(stackSize != 0){
                if(packetStruct.peek()[0]%2==0){
                    //packetArray[0] = packetStruct.pop();
                }
                stackSize--;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}

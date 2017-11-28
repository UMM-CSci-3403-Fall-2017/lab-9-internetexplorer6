package segmentedfilesystem;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
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
            int upperLimit = Integer.MAX_VALUE;
            while (stackSize < upperLimit) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String packetNumber;
                byte[] packetNumArr = Arrays.copyOfRange(buf,2,3);
                packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                int realPacketNumber = Integer.parseInt(packetNumber);
                packetStruct.push(buf);
                stackSize++;

                if(buf[0]%4 == 3){
                    upperLimit = realPacketNumber;
                }
            }
            ArrayList<byte[]> packetArray = new ArrayList<byte[]>();
            while(stackSize != 0){
                if(packetStruct.peek()[0]%2==0){
                    packetArray.add(0, packetStruct.pop());
                } else {
                    String packetNumber;
                    byte[] packetNumArr = Arrays.copyOfRange(packetStruct.peek(),2,3);
                    packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                    int realPacketNumber = Integer.parseInt(packetNumber);
                    packetArray.add(realPacketNumber - 1, packetStruct.pop());
                }
                stackSize--;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}

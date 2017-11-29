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
            byte[] idList = new byte[3];
            int byteArrCounter = 0;
            int fileID = -1;
            ArrayList<byte[]> files = new ArrayList<>();
            for(int i = 1; i < 4; i++) {
                stackSize = 0;
                while (stackSize <= upperLimit) {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String packetNumber;
                    byte[] packetNumArr = Arrays.copyOfRange(buf, 2, 3);
                    if ((packetNumArr[1] == fileID) && fileID != -1) {
                        packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                        int realPacketNumber = Integer.parseInt(packetNumber);
                        packetStruct.push(buf);
                        stackSize++;
                        if (buf[0] % 4 == 3) {
                            upperLimit = realPacketNumber;
                        }
                    } else {
                        idList[byteArrCounter] = packetNumArr[1];
                        byteArrCounter++;
                    }
                }
                ArrayList<byte[]> packetArray = new ArrayList<byte[]>();
                int packetCount = stackSize + 1;
                while(stackSize != 0){
                    if(packetStruct.peek()[0]%2==0){
                        packetArray.add(0, packetStruct.pop());
                    } else {
                        String packetNumber;
                        byte[] packetNumArr = Arrays.copyOfRange(packetStruct.peek(),2,3);
                        packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                        int realPacketNumber = Integer.parseInt(packetNumber);
                        packetArray.add(realPacketNumber, packetStruct.pop());
                    }
                    stackSize--;
                }
                //make 3 array to store each files bytes
                byte[] fullFile = new byte[packetCount*1024];
                int incrementer = 0;
                for(int j = 0; j < packetCount; j++){

                    for(int x = 0; x < 1024; x++){
                        fullFile[incrementer] = packetArray.get(j)[x];
                        incrementer++;
                    }

                }
                fileID = idList[i];
                files.add(fullFile);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}

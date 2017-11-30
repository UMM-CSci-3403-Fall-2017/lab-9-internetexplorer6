package segmentedfilesystem;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.Stack;

//main thread class
public class QuoteServerThread extends Thread {
    int stackSize = 0;
    BufferedReader in;
    Stack<byte[]> packetStruct = new Stack<>();
    DatagramSocket socket;
    public QuoteServerThread(DatagramSocket socket) throws IOException, SocketException {
        this.socket = socket;
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
                    byte[] packetNumArr = Arrays.copyOfRange(buf, 2, 4);
                    System.out.println(packetNumArr[0]);
                    //System.out.println(packetNumArr[1]);
                    //System.out.println(packetNumArr[2]);
                    //System.out.println(packetNumArr[3]);
                    if ((fileID != -1)&& packetNumArr[1] == fileID) {
                        packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                        int realPacketNumber = Integer.parseInt(packetNumber);
                        packetStruct.push(buf);
                        stackSize++;
                        if (buf[0] % 4 == 3) {
                            System.out.println("found last: " + realPacketNumber);
                            upperLimit = realPacketNumber;
                        }
                    } else {
                        System.out.println("found a data packet");
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
            for(int i = 0; i < 3; i++){
                try(FileOutputStream fileOut = new FileOutputStream(".../aruba-test/etc/" + files.get(i)[1])) {
                    fileOut.write(files.get(i));
                    fileOut.close();
                }
//                for(int j = 0; j<files.get(i).length;j++){
//                    System.out.write(files.get(i)[j]);
//                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}

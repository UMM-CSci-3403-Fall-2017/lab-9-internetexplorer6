package segmentedfilesystem;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;



//main thread class
public class QuoteServerThread extends Thread {
    int stackSize = 0;
    int upperLimit = Integer.MAX_VALUE;
    BufferedReader in;
    Stack<byte[]> packetStruct = new Stack<>();
    DatagramSocket socket;
    int footCount = 0;
    int[] footInts = new int[3];
    HashMap<Byte, Integer> IDandSizePair = new HashMap<Byte, Integer>();
    byte[] IDs = new byte[3];
    boolean threeFound = false;
    public QuoteServerThread(DatagramSocket socket) throws IOException, SocketException {
        this.socket = socket;
    }

    public boolean foundFiles = false;
    public void run() {
        try{
            System.out.println();

            while(stackSize<=upperLimit) {
                if(footCount==3){
                    upperLimit = 0;
                    for(int i = 0; i < footCount; i++){
                        upperLimit += footInts[i];
                    }
                }
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] packetNumArr = Arrays.copyOfRange(buf, 2, 4);
                if (buf[0] % 4 == 3) {
                    footInts[footCount] = buf[0];
                    int packetNumber = ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
                    System.out.println("found last: " + packetNumber);
                    IDandSizePair.put(buf[1],(packetNumber));
                    IDs[footCount] = buf[1];
                    footCount++;
                }
                packetStruct.push(buf);
                stackSize++;
            }

            System.out.println("first loop done, stack-size: " + stackSize);
            System.out.println("the hashmap values" + IDandSizePair.values());
            System.out.println("The IDs: [" + IDs[0]+", "+IDs[1]+", "+IDs[2]+"]");
            byte[][] file1 = new byte[IDandSizePair.get(IDs[0])+1][1024];
            byte[][] file2 = new byte[IDandSizePair.get(IDs[1])+1][1024];
            byte[][] file3 = new byte[IDandSizePair.get(IDs[2])+1][1024];


            while(!packetStruct.empty()) {
                //System.out.println("doin loop");
                int packetNumber = ((packetStruct.peek()[2] & 0xff) << 8) | (packetStruct.peek()[3] & 0xff);
                if (packetStruct.peek()[1] == IDs[0]) {
                    file1[packetNumber] = packetStruct.pop();
                    System.out.println("file 1 packet number: "+packetNumber);
                }
                else if (packetStruct.peek()[1] == IDs[1]) {
                    file2[packetNumber] = packetStruct.pop();
                    System.out.println("file 2 packet number: "+packetNumber);
                }
                else if (packetStruct.peek()[1] == IDs[2]) {
                    file3[packetNumber] = packetStruct.pop();
                    System.out.println("file 3 packet number: "+packetNumber);
                }
            }

        } catch(IOException e){
            e.printStackTrace();
        }

    }



    /*int stackSize = 0;
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
            //this for loop handles each file
            for(int i = 1; i < 4; i++) {
                stackSize = 0;
                //this while loop handles a single file with multiple packets
                boolean firstRecieved = true;
                while (stackSize <= upperLimit) {

                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String packetNumber;
                    byte[] packetNumArr = Arrays.copyOfRange(buf, 2, 4);
                    System.out.println("Datagram Packet ID: "+ buf[1]);

                    //System.out.println(packetNumArr[0]);
                    //System.out.println(packetNumArr[1]);
                    //System.out.println(packetNumArr[2]);
                    //System.out.println(packetNumArr[3]);
                    if ((!firstRecieved)&&fileID==buf[1]) {
                        System.out.println("we made it woo");
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
                        boolean alreadyFound = false;
                        for(int k = 0; k < idList.length;k++){
                            if(idList[k] == buf[1]){
                                alreadyFound = true;
                            }
                        }
                        if(!alreadyFound) {
                            idList[byteArrCounter] = buf[1];
                            byteArrCounter++;
                        }
                    }
                    if(firstRecieved){
                        System.out.println("First recieved added");
                        packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                        int realPacketNumber = Integer.parseInt(packetNumber);
                        packetStruct.push(buf);
                        stackSize++;
                        if (buf[0] % 4 == 3) {
                            System.out.println("found last: " + realPacketNumber);
                            upperLimit = realPacketNumber;
                        }
                        firstRecieved = false;
                    }
                    fileID = buf[1];
                }
                ArrayList<byte[]> packetArray = new ArrayList<byte[]>();
                int packetCount = stackSize + 1;
                for(int k = 0; k < packetCount; k++){
                    packetArray.add(new byte[0]);
                }
                System.out.println("length: " + packetArray.size());
                while(stackSize != 0){
                    if(packetStruct.peek()[0]%2==0){
                        packetArray.add(0, packetStruct.pop());
                    } else {
                        String packetNumber;
                        byte[] packetNumArr = Arrays.copyOfRange(packetStruct.peek(),2,4);
                        packetNumber = (packetNumArr[0] + "" + packetNumArr[1]);
                        System.out.println("Packet Number: " + packetNumber);
                        int realPacketNumber = Integer.parseInt(packetNumber);
                        System.out.println("Packet Number: " + realPacketNumber);
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
                //fileID = idList[i];
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
    }*/

}

package segmentedfilesystem;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;



//main thread class
public class RetrievePacketThread extends Thread {
    int stackSize = 0;
    int upperLimit = Integer.MAX_VALUE;
    BufferedReader in;
    Stack<byte[]> packetStruct = new Stack<>();
    DatagramSocket socket;
    int footCount = 0;
    int[] footInts = new int[3];

    HashMap<Byte, Integer> IDandSizePair = new HashMap<Byte, Integer>();
    HashMap<Byte, Integer> IDandNamePair = new HashMap<>();
    Stack<byte[]> heads = new Stack<>();
    private static byte[] head1 = new byte[1024];
    private static byte[] head2 = new byte[1024];
    private static byte[] head3 = new byte[1024];
    private static String fileName1 = "";
    private static String fileName2 = "";
    private static String fileName3 = "";

    byte[] IDs = new byte[3];
    boolean threeFound = false;

    public RetrievePacketThread(DatagramSocket socket) throws IOException, SocketException {
        this.socket = socket;
    }

    public boolean foundFiles = false;

    public void run() {
        try {
            System.out.println();

            while (stackSize <= upperLimit) {
                if (footCount == 3) {
                    upperLimit = 0;
                    for (int i = 0; i < footCount; i++) {
                        System.out.println("OUR FOOT INTS ARE: " + footInts[i]);
                        upperLimit += footInts[i];
                    }
                    footCount++;
                }
                // do not push headers.
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] packetNumArr = Arrays.copyOfRange(buf, 2, 4);
                int packetNumber = ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
                if (buf[0] % 4 == 3) {
                    footInts[footCount] = packetNumber;
                    //int packetNumber = ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
                    //System.out.println("found last: " + packetNumber);
                    IDandSizePair.put(buf[1], (packetNumber + 1));
                    IDs[footCount] = buf[1];
                    footCount++;
                    packetStruct.push(buf);
                    stackSize++;
                } else if (buf[0] % 2 == 0) {
                    //System.out.println("Found a header packet");
                    IDandNamePair.put(buf[1], (packetNumber));
                    packetStruct.push(buf);
                    //System.out.println("Name of the file " + IDandNamePair.get(buf[1]));
                } else {
                    System.out.println("pushing ID: " + buf[0] + ", and packet number: " + packetNumber);
                    packetStruct.push(buf);
                    stackSize++;
                }
            }
            System.out.println(heads.size());

            System.out.println("first loop done, stack-size: " + stackSize);
            System.out.println("the hashmap values: " + IDandSizePair.values());
            System.out.println("the hashmap name values: " + IDandNamePair.values());
            System.out.println("The IDs: [" + IDs[0] + ", " + IDs[1] + ", " + IDs[2] + "]");
            //ArrayList<byte[]> file1 = new ArrayList<>();
            //ArrayList<byte[]> file2 = new ArrayList<>();
            //ArrayList<byte[]> file3 = new ArrayList<>();
            byte[][] file1Matrix = new byte[IDandSizePair.get(IDs[0]) + 1][1024];
            byte[][] file2Matrix = new byte[IDandSizePair.get(IDs[1]) + 1][1024];
            byte[][] file3Matrix = new byte[IDandSizePair.get(IDs[2]) + 1][1024];

            int file1length = 1;
            int file2length = 1;
            int file3length = 1;
            while (!packetStruct.empty()) {

                //System.out.println("doin loop");
                int packetNumber = ((packetStruct.peek()[2] & 0xff) << 8) | (packetStruct.peek()[3] & 0xff);
                //System.out.println(packetNumber);
                if (packetStruct.peek()[1] == IDs[0]) {
                    if(packetStruct.peek()[0]%2==0){
                        head1 = packetStruct.pop();
                    }
                    else {
                        file1Matrix[packetNumber] = packetStruct.peek();
                        /*for (int i = 0; i < packetStruct.peek().length; i++) {
                            file1Matrix[packetNumber][i] = packetStruct.peek()[i];
                            file1length++;
                        }*/
                        packetStruct.pop();
                    }

                    System.out.println("file 1 packet number: " + packetNumber);
                } else if (packetStruct.peek()[1] == IDs[1]) {
                    if(packetStruct.peek()[0]%2==0){
                        head2 = packetStruct.pop();
                    }
                    else {
                        file2Matrix[packetNumber] = packetStruct.peek();
                        /*for (int i = 0; i < packetStruct.peek().length; i++) {
                            file2Matrix[packetNumber][i] = packetStruct.peek()[i];
                            file2length++;
                        }*/
                        packetStruct.pop();
                    }

                    System.out.println("file 2 packet number: " + packetNumber);
                } else if (packetStruct.peek()[1] == IDs[2]) {
                    if(packetStruct.peek()[0]%2==0){
                        head3 = packetStruct.pop();
                    }
                    else {
                        file3Matrix[packetNumber] = packetStruct.peek();
                        /*for (int i = 0; i < packetStruct.peek().length; i++) {
                            file3Matrix[packetNumber][i] = packetStruct.peek()[i];
                            file3length++;
                        }*/
                        packetStruct.pop();
                    }

                    System.out.println("file 3 packet number: " + packetNumber);
                }
            }
            System.out.println("the lovely filename status bytes are "+ head1[0]+", "+head2[0]+", "+head3[0]);
            for (int i = 0; i < file3Matrix.length; i++) {
                int packetNumber = ((file3Matrix[i][2] & 0xff) << 8) | (file3Matrix[i][3] & 0xff);
                System.out.println(packetNumber);
            }
            //add counters for file length
            byte[] file1 = new byte[file1length];
            byte[] file2 = new byte[file2length];
            byte[] file3 = new byte[file3length];

            int file1ByteCounter = 0;
            for (int i = 0; i < file1Matrix.length; i++) {
                for (int k = 0; k < file1Matrix[i].length; k++) {
                    //file1[file1ByteCounter] = file1Matrix[i][k];
                }
            }
            writeFileNames(head1,head2,head3);

            try (FileOutputStream fos = new FileOutputStream("./" + IDandNamePair.get(IDs[0]))) {
                //fos.write(file1Matrix);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeFileNames(byte[] h1, byte[] h2, byte[] h3){
        for(int i = 2; i < h1.length;i++){
            if(h1[i]==0){
                break;
            }
            fileName1 = fileName1 + (char)h1[i];
        }
        for(int i = 2; i < h2.length;i++){
            if(h2[i]==0){
                break;
            }
            fileName2 = fileName2 + (char)h2[i];
        }
        for(int i = 2; i < h3.length;i++){
            if(h3[i]==0){
                break;
            }
            fileName3 = fileName3 + (char)h3[i];
        }
        System.out.println(fileName1+", "+fileName2+", "+fileName3);
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



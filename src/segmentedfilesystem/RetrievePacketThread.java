package segmentedfilesystem;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;



//main thread class
public class RetrievePacketThread extends Thread {
    // Initializes the size of the stack where all packets will initially be pushed to
    int stackSize = 0;
    int upperLimit = Integer.MAX_VALUE;
    Stack<byte[]> packetStruct = new Stack<>();
    DatagramSocket socket;
    int footCount = 0;
    int[] footInts = new int[3];

    // Hashmap data structure which stores a Byte (ID# of packet) as a key, and an integer (representing the
    // number of packets in a file) as the key value.
    HashMap<Byte, Integer> IDandSizePair = new HashMap<Byte, Integer>();
    //storage for header packets in byte arrays to be used for writing file names.
    private static byte[] head1 = new byte[128];
    private static byte[] head2 = new byte[128];
    private static byte[] head3 = new byte[128];
    //Initializing file names.
    private static String fileName1 = "";
    private static String fileName2 = "";
    private static String fileName3 = "";

    // Array storing the IDs for each file.
    byte[] IDs = new byte[3];
    // socket reference created to be used in run() method.
    public RetrievePacketThread(DatagramSocket socket) throws IOException, SocketException {
        this.socket = socket;
    }
    // Main operation of RetrievePacketThread.
    public void run() {
        try {
            // pushing all packets onto stack, and writing IDs and associated file sizes
            // onto hash table.
            while (stackSize <= upperLimit) {
                if (footCount == 3) {
                    // Because upperLimit is initially INTEGER.MAX_VALUE, it needs to be changed eventually
                    // This happens here. When we get all three footer packets, it sets upperLimit to the
                    // amount of all packets we will receive. It does this by adding their packet numbers together
                    upperLimit = 0;
                    for (int i = 0; i < footCount; i++) {
                        upperLimit += footInts[i];
                    }
                    footCount++;
                }
                // setting the packet buffer to the maximum packet size and receiving it from the packet
                byte[] buf = new byte[1028];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                int packetNumber = ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
                if (buf[0] % 4 == 3) {
                    // conditional for footer packet; adds it to footInts, stores it in the map (so that we can know all three file IDs)
                    // and pushes it to the packet stack
                    footInts[footCount] = packetNumber;
                    IDandSizePair.put(buf[1], (packetNumber + 1));
                    IDs[footCount] = buf[1];
                    footCount++;
                    packetStruct.push(buf);
                    stackSize++;
                } else if (buf[0] % 2 == 0) {
                    // Because we don't want to deal with headers until we know all of the file IDs,
                    // for now those are just pushed to the stack
                    packetStruct.push(buf);
                    stackSize++;
                } else {
                    // This is for normal data packets, we simply push them to the stack
                    packetStruct.push(buf);
                    stackSize++;
                }
            }

            //The following are matrices for each file in which, each row is the number of packets in the associated file
            // (determined by the ID), and the column is associated with the max size of each packet.
            byte[][] file1Matrix = new byte[IDandSizePair.get(IDs[0]) + 1][1028];
            byte[][] file2Matrix = new byte[IDandSizePair.get(IDs[1]) + 1][1028];
            byte[][] file3Matrix = new byte[IDandSizePair.get(IDs[2]) + 1][1028];

            // The following loop traverses through each packet on the stack and pops each value into its associated
            // data packet, by ID.  If the item is a header packet the previously initiated header packets for the
            // associated file are set to the value of the packet on the stack.  The result is 3 seperate header packets
            // for the files, and 3 file matrices, one for each file.
            while (!packetStruct.empty()) {

                int packetNumber = ((packetStruct.peek()[2] & 0xff) << 8) | (packetStruct.peek()[3] & 0xff);
                if (packetStruct.peek()[1] == IDs[0]) {
                    if(packetStruct.peek()[0]%2==0){
                        head1 = packetStruct.pop();
                    }
                    else {
                        file1Matrix[packetNumber] = packetStruct.pop();
                    }
                } else if (packetStruct.peek()[1] == IDs[1]) {
                    if(packetStruct.peek()[0]%2==0){
                        head2 = packetStruct.pop();
                    }
                    else {
                        file2Matrix[packetNumber] = packetStruct.pop();
                    }
                } else if (packetStruct.peek()[1] == IDs[2]) {
                    if(packetStruct.peek()[0]%2==0){
                        head3 = packetStruct.pop();
                    }
                    else {
                        file3Matrix[packetNumber] = packetStruct.pop();
                    }

                }
            }
            // write names for files from given header packets.
            writeFileNames(head1,head2,head3);

            // OutputStreams initialized for writing files.
            FileOutputStream f1 = new FileOutputStream(fileName1);
            FileOutputStream f2 = new FileOutputStream(fileName2);
            FileOutputStream f3 = new FileOutputStream(fileName3);
            //write files to project directory.
            writeFiles(f1,file1Matrix);
            writeFiles(f2,file2Matrix);
            writeFiles(f3,file3Matrix);
            // System out to confirm the files were successfully retrieved.
            System.out.println("Retrieved three files: "+fileName1 + ", " + fileName2 + ", " + fileName3);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Takes the three header packets and writes the data portion to a string (which is why
    // the loops begin at 2)
    public static void writeFileNames(byte[] h1, byte[] h2, byte[] h3){
        for(int i = 2; i < h1.length;i++){
            if(h1[i]==0){
                // There were excess zeroes at the end of the data portion of the packet,
                // and this way simply breaks out of the loop when it encounters them.
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
    }
    // This writes the file matrices to the output stream.
    public static void writeFiles(FileOutputStream file, byte[][] fileMatrix){
        try {

            for (int i = 0; i < fileMatrix.length;i++) {
                // We needed a way to differentiate the zeroes within the data we were writing,
                // from excess ones at the end of the packet.
                // This continuously counts every zero it sees, but only writes them if there are non-zeroes afterwards.
                int zeroCount = 0;
                for (int j = 4; j < fileMatrix[i].length; j++) {
                    if (fileMatrix[i][j] != 0) {
                        for (int z = 0; z < zeroCount; z++) {
                            file.write((byte)0);
                        }
                        zeroCount = 0;
                        file.write(fileMatrix[i][j]);
                    }
                    else{
                        zeroCount++;
                    }
                }
            }
            file.flush();
            file.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}







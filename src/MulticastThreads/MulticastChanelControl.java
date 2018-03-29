package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.DiskReclaimHandle;
import InitiatorCommunication.GetChunkHandle;
import InitiatorCommunication.GetChunkRequest;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;
import com.sun.xml.internal.ws.api.model.MEP;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MulticastChanelControl extends MulticastChanel {


    public MulticastChanelControl(String mcc_address, String mcc_port, String mcb_address,
                                  String mcb_port, String mcr_address, String mcr_port, int peerID) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port, peerID);
        multicast_control_socket = new MulticastSocket(Integer.parseInt(multicast_control_port));

        //joining group
        multicast_control_socket.joinGroup(InetAddress.getByName(multicast_control_address));

    }

    @Override
    public void run() {
        // listen on control


        byte[] raw_message = new byte[FileHandler.MAX_SIZE_MESSAGE];
        DatagramPacket packet_received = new DatagramPacket(raw_message, FileHandler.MAX_SIZE_MESSAGE);

        while(true){
            try {
                multicast_control_socket.receive(packet_received);
                ProtocolMessage message = ProtocolMessageParser.parseMessage(packet_received.getData());

                if(message == null || message.getSenderId().equals(String.valueOf(Peer.peerID)))
                    continue;

                switch (message.getMsgType()){
                    case STORED:
                        int size_message = message.body.length;
                        VolatileDatabase.add_chunk(message.getFileId(),Short.valueOf(message.getChunkNo()),message.getReplicationDeg(), Integer.parseInt(message.getSenderId()),size_message);
                        break;
                    case GETCHUNK:
                        System.out.println("RECEIVED GETCHUNK");
                        System.out.println("FILEiD: "+ message.getFileId());
                        System.out.println("ChunkNo: "+ message.getChunkNo());
                        if (!FileHandler.hasChunk(message.getFileId(),Short.valueOf(message.getChunkNo()))){
                            System.out.println("GETCHUNK file is not backed up, ignoring...");
                            continue;
                        }
                        VolatileDatabase.getChunkMemory.add(message.getFileId()+message.getChunkNo());
                        int delay = new Random().nextInt(400);
                        Peer.threadPool.schedule(new GetChunkHandle(message),delay, TimeUnit.MILLISECONDS);
                        break;
                    case DELETE:
                        System.out.println(new String(message.toCharArray()));
                        Peer.threadPool.submit(() -> {
                            FileHandler.removeFolder(new File(FileHandler.savePath + message.getFileId()));
                        });
                        break;
                    case REMOVED:
                        System.out.println("REMOVED RECEIVED");
                        Peer.threadPool.submit(new DiskReclaimHandle(message.getFileId(),Short.valueOf(message.getChunkNo())));
                        break;
                    default:
                        System.out.println("Unknown message type received on data channel");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("MCC+" + peerID +": There was an error reading from the socket!");
            }
        }
    }
}

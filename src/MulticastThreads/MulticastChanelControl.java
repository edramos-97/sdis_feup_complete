package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.*;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;

import java.io.IOException;
import java.net.*;
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
                ProtocolMessage message = ProtocolMessageParser.parseMessage(packet_received.getData(),packet_received.getLength());

                if(message == null || message.getSenderId().equals(String.valueOf(Peer.peerID)))
                    continue;

                switch (message.getMsgType()){
                    case STORED:
                        System.out.println("RECEIVED STORED CHUNK:"+message.getChunkNo() + "FROM PEER: "+ message.getSenderId());
                        VolatileDatabase.add_chunk_stored(message.getFileId(),Short.valueOf(message.getChunkNo()),Integer.parseInt(message.getSenderId()));
                        break;
                    case GETCHUNK:

                        if (!FileHandler.hasChunk(message.getFileId(),Short.valueOf(message.getChunkNo()))){
                            System.out.println("GETCHUNK file is not backed up, ignoring...");
                            continue;
                        }
                        VolatileDatabase.getChunkMemory.add(message.getFileId()+message.getChunkNo());
                        int delay = new Random().nextInt(400);
                        Peer.threadPool.schedule(new GetChunkHandle(message, packet_received),delay, TimeUnit.MILLISECONDS);
                        break;
                    case DELETE:
                        System.out.println("DELETE fileID: \""+message.getFileId()+"\"");
                        Peer.threadPool.submit(new DeleteHandle(message));
                        break;
                    case DELETECONF:
                        System.out.println("DELETE confirming fileID:\""+message.getFileId()+"\" from peer:\""+message.getSenderId()+"\"");
                        VolatileDatabase.confirmDelete(message.getFileId(),message.getSenderId());
                        break;
                    case BACKEDUP:
                        System.out.println("BACKEDUP fileID:\""+message.getFileId()+"\" from peer:\""+message.getSenderId()+"\"");
                        Peer.threadPool.submit(new BackedupHandle(message));
                        break;
                    case REMOVED:
                        System.out.println("REMOVED RECEIVED");
                        Peer.threadPool.submit(new DiskReclaimHandle(message.getFileId(),Short.valueOf(message.getChunkNo()),Integer.parseInt(message.getSenderId())));
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

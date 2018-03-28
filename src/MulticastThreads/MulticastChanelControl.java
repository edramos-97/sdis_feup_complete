package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.PutChunkHandle;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;

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
                        VolatileDatabase.add_chunk(message.getFileId(),Short.valueOf(message.getChunkNo()),(short)message.getReplicationDeg());
                        //TODO update local chunk count
                        break;
                    case GETCHUNK:
                        System.out.println("RECEIVED CHUNK, ignoring for now");
                        break;
                    case DELETE:
                        //TODO run delete protocol
                        break;
                    case REMOVED:
                        //TODO run reclaim protocol
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

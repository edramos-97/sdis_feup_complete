package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.PutChunkHandle;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MulticastChanelData extends MulticastChanel {

    public MulticastChanelData(String mcc_address, String mcc_port, String mcb_address,
                               String mcb_port, String mcr_address, String mcr_port, int peerID) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port, peerID);
        multicast_data_socket = new MulticastSocket(Integer.parseInt(multicast_data_port));

        //joining group
        multicast_data_socket.joinGroup(InetAddress.getByName(multicast_data_address));

    }

    @Override
    public void run() {
        // listen on data
        //System.out.println("STARTING DATA CHANEL");


        byte[] raw_message = new byte[FileHandler.MAX_SIZE_MESSAGE];
        DatagramPacket packet_received = new DatagramPacket(raw_message, FileHandler.MAX_SIZE_MESSAGE);

        while(true){
            try {
                multicast_data_socket.receive(packet_received);

                ProtocolMessage message = ProtocolMessageParser.parseMessage(packet_received.getData());

                if(message == null || message.getSenderId().equals(String.valueOf(Peer.peerID)))
                    continue;

                int delay = new Random().nextInt(400);// TODO-CheckEnhancement 10x this to get better results

                switch (message.getMsgType()){
                    case PUTCHUNK:

                        if(!FileHandler.canSave()){
                            System.out.println("Back up space is full! Not saving putChunk");
                            break;
                        }

                        // Checking if chunk is already saved by this peer.
                        /*
                        if(VolatileDatabase.check_has_chunk(message.getFileId(), Short.valueOf(message.getChunkNo()))){
                            break;
                        }*/

                        int size_message = message.body.length;
                        VolatileDatabase.add_chunk_putchunk(message.getFileId(),Short.valueOf(message.getChunkNo()),message.getReplicationDeg(), size_message);

                        Peer.threadPool.schedule(new PutChunkHandle(message),delay, TimeUnit.MILLISECONDS);
                        break;
                    default:
                        System.out.println("Unknown message type received on data channel");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("MCD+" + peerID +": There was an error reading from the socket!");
            }
        }
    }
}

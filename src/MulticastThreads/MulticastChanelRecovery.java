package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.RestoreEnhancement;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastChanelRecovery extends MulticastChanel {

    public MulticastChanelRecovery(String mcc_address, String mcc_port, String mcb_address,
                                   String mcb_port, String mcr_address, String mcr_port, int peerID) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port, peerID);
        multicast_recover_socket = new MulticastSocket(Integer.parseInt(multicast_recover_port));

        //joining group
        multicast_recover_socket.joinGroup(InetAddress.getByName(multicast_recover_address));

    }

    @Override
    public void run() {
        // listen on recover

        byte[] raw_message = new byte[FileHandler.MAX_SIZE_MESSAGE];
        DatagramPacket packet_received = new DatagramPacket(raw_message, FileHandler.MAX_SIZE_MESSAGE);


        while(true){
            try {
                multicast_recover_socket.receive(packet_received);

                ProtocolMessage message = ProtocolMessageParser.parseMessage(packet_received.getData(),packet_received.getLength());

                if (message == null || Integer.parseInt(message.getSenderId()) == Peer.peerID)continue;

                switch (message.getMsgType()){
                    case CHUNK:
                        if(VolatileDatabase.restoreMemory.get(message.getFileId())==null) {
                            VolatileDatabase.getChunkMemory.remove(message.getFileId() + message.getChunkNo());
                        }else{
                            if(message.getVersion().equals("1.1")){
                                if(RestoreEnhancement.can_save_these.containsKey(message.getFileId()+message.getChunkNo())){
                                    message = RestoreEnhancement.can_save_these.remove(message.getFileId()+message.getChunkNo());
                                    VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                                    FileHandler.saveChunk(message,"restore");
                                }
                                // launch chunk handle
                            }else{
                                VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                                FileHandler.saveChunk(message,"restore");
                            }
                        }
                        break;
                    default:
                        System.out.println(new String(message.toCharArray()));
                        System.out.println("mcr - wrong type of message");
                        break;
                }

            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("MCR+" + peerID +": There was an error reading from the socket!");
            }
        }
    }

}

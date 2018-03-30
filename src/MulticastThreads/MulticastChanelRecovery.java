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
import java.time.Period;

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

                ProtocolMessage message = ProtocolMessageParser.parseMessage(packet_received.getData());

                if (message == null || Integer.parseInt(message.getSenderId()) == Peer.peerID)continue;

                switch (message.getMsgType()){
                    case CHUNK:
                        System.out.println("RECEIVED CHUNK");
                        if(VolatileDatabase.restoreMemory.get(message.getFileId())==null) {
                            System.out.println("RECEIVED CHUNK CONTROL CHUNK");
                            VolatileDatabase.getChunkMemory.remove(message.getFileId() + message.getChunkNo());
                        }else{
                            System.out.println("SAVING MESSAGE");
                            if(message.getVersion().equals("1.1")){
                                // launch chunk handle
                            }else{
                                System.out.println("SAVED CHUNK SIZE: "+message.getBody().length);
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
                e.printStackTrace();
                System.out.println("MCR+" + peerID +": There was an error reading from the socket!");
            }
        }
    }

}

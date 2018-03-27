package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.PutChunkHandle;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;

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

                System.out.println("received something");

                ProtocolMessage message = ProtocolMessageParser.parseMessage(packet_received.getData());

                if (message == null)continue;

                switch (message.getMsgType()){

                    case CHUNK:

                        break;
                    default:
                        System.out.println("wrong type of message");
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("MCR+" + peerID +": There was an error reading from the socket!");
            }
        }
    }

}

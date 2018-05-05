package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.BackedupHandle;
import InitiatorCommunication.DeleteHandle;
import InitiatorCommunication.DiskReclaimHandle;
import InitiatorCommunication.GetChunkHandle;
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
        DatagramPacket packet_received;

        while(true){
            packet_received = new DatagramPacket(raw_message, FileHandler.MAX_SIZE_MESSAGE);
            try {
                multicast_control_socket.receive(packet_received);

                Peer.threadPool.submit(new Receiver(packet_received));

            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("MCC+" + peerID +": There was an error reading from the socket!");
            }
        }
    }
}

package MulticastThreads;

import Utilities.FileHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastChanelData extends MulticastChanel {

    public MulticastChanelData(String mcc_address, String mcc_port, String mcb_address,
                               String mcb_port, String mcr_address, String mcr_port, int peerID) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port, peerID);
        multicast_data_socket = new MulticastSocket(Integer.parseInt(multicast_data_port));

        //joining group
        multicast_data_socket.joinGroup(InetAddress.getByName(multicast_data_address));
        System.out.println(multicast_data_address);

    }

    @Override
    public void run() {
        // listen on data
        System.out.println("STARTING DATA CHANEL");


        byte[] raw_message = new byte[5];
        DatagramPacket packet_received = new DatagramPacket(raw_message, 5);


        while(true){
            try {
                multicast_data_socket.receive(packet_received);

                // take care of package
                System.out.println(new String(packet_received.getData()));

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("MCD+" + peerID +": There was an error reading from the socket!");
            }
        }
    }
}

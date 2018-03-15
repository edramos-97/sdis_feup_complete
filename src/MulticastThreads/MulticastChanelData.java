package MulticastThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MulticastChanelData extends MulticastChanel {

    public MulticastChanelData(String mcc_address, String mcc_port, String mcb_address,
                               String mcb_port, String mcr_address, String mcr_port, int peerID) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port, peerID);

        //joining group
        multicast_data_socket.joinGroup(InetAddress.getByName(multicast_data_address));

    }

    @Override
    public void run() {
        // listen on data


        byte[] raw_message = new byte[100];
        DatagramPacket packet_received = new DatagramPacket(raw_message, 100);


        while(true){
            try {
                multicast_data_socket.receive(packet_received);

                // take care of package

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("MCD+" + peerID +": There was an error reading from the socket!");
            }
        }
    }
}

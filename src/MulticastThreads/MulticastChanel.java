package MulticastThreads;

import java.io.IOException;
import java.net.MulticastSocket;

class MulticastChanel extends Thread {

    protected String multicast_control_address;
    protected String multicast_control_port;
    protected MulticastSocket multicast_control_socket;
    protected String multicast_data_address;
    protected String multicast_data_port;
    protected MulticastSocket multicast_data_socket;
    protected String multicast_recover_address;
    protected String multicast_recover_port;
    protected MulticastSocket multicast_recover_socket;
    protected int peerID;

    MulticastChanel(String mcc_address, String mcc_port, String mcb_address,
                    String mcb_port, String mcr_address, String mcr_port, int peerID) throws IOException {
        multicast_control_address = mcc_address;
        multicast_control_port = mcc_port;
        multicast_data_address = mcb_address;
        multicast_data_port = mcb_port;
        multicast_recover_address = mcr_address;
        multicast_recover_port = mcr_port;
        this.peerID = peerID;
        // creating sockets
        multicast_control_socket = new MulticastSocket(Integer.parseInt(multicast_control_port));
        multicast_data_socket = new MulticastSocket(Integer.parseInt(multicast_data_port));
        multicast_recover_socket = new MulticastSocket(Integer.parseInt(multicast_recover_port));

    }

    @Override
    public void run(){

        // not needed here(?)

    }
}

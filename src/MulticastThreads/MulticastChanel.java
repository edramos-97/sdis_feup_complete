package MulticastThreads;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketAddress;

public class MulticastChanel extends Thread {

    public static String multicast_control_address;
    public static String multicast_control_port;
    public static MulticastSocket multicast_control_socket;
    public static String multicast_data_address;
    public static String multicast_data_port;
    public static MulticastSocket multicast_data_socket;
    public static String multicast_recover_address;
    public static String multicast_recover_port;
    public static MulticastSocket multicast_recover_socket;

    /* TODO RESTORE-ENHANCEMENT
    public static String tcp_socket_address;
    public static int tcp_socket_port;
    public static ServerSocket tcp_ss;
    */



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
    }

    @Override
    public void run(){
        // not needed here(?)
    }
}

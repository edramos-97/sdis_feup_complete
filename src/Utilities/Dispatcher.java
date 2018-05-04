package Utilities;

import java.net.MulticastSocket;

import MulticastThreads.MulticastChanel;
import MulticastThreads.MulticastChanelRecovery;

class Dispatcher {
    
    public static void send(byte[] message_bytes, String address, String port, MulticastSocket data_socket) {
        DatagramPacket packet;
        try {
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
                    InetAddress.getByName(address),
                    Integer.parseInt(port));
            data_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("Error");
        } catch (IOException e) {
            System.out.println("Error");
        }
    }

    public static void sendData(byte[] message_bytes) {
        send(message_bytes, MulticastChanel.multicast_data_address, MulticastChanel.multicast_data_port, MulticastChanel.multicast_data_socket);
    }

    public static void sendRecover(byte[] message_bytes) {
        send(message_bytes, MulticastChanel.multicast_recover_address, MulticastChanel.multicast_recover_port, MulticastChanel.multicast_recover_socket);
    }

    public static void sendControl(byte[] message_bytes) {
        send(message_bytes, MulticastChanel.multicast_control_address, MulticastChanel.multicast_control_port, MulticastChanel.multicast_control_socket);
    }

    public static byte[] encrypt(byte[] data) {
        
        return data;
    }

    public static byte[] decrypt(byte[] data) {
        return data;
    }
}
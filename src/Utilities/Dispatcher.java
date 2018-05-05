package Utilities;

import MulticastThreads.MulticastChanel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher extends Thread{

    private final static LinkedBlockingQueue<DispatcherMessage> dispatcherQueue = new LinkedBlockingQueue<>(100);
    
    private static void send(byte[] message_bytes, String address, String port, MulticastSocket data_socket) {
        DatagramPacket packet;
        try {
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
                    InetAddress.getByName(address),
                    Integer.parseInt(port));
            data_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("Error in hostname");
        } catch (IOException e) {
            System.out.println("Error in IOException");
        }
    }

    public static void sendData(byte[] message_bytes) {
        try {
            dispatcherQueue.put(new DispatcherMessage(message_bytes, MulticastChanel.multicast_data_address, MulticastChanel.multicast_data_port, MulticastChanel.multicast_data_socket));
        } catch (InterruptedException e) {
            System.out.println("Error in adding Message to queue");
        }
        //send(message_bytes, MulticastChanel.multicast_data_address, MulticastChanel.multicast_data_port, MulticastChanel.multicast_data_socket);
    }

    public static void sendRecover(byte[] message_bytes) {
        try {
            dispatcherQueue.put(new DispatcherMessage(message_bytes, MulticastChanel.multicast_recover_address, MulticastChanel.multicast_recover_port, MulticastChanel.multicast_recover_socket));
        } catch (InterruptedException e) {
            System.out.println("Error in adding Message to queue");
        }
        //send(message_bytes, MulticastChanel.multicast_recover_address, MulticastChanel.multicast_recover_port, MulticastChanel.multicast_recover_socket);
    }

    public static void sendControl(byte[] message_bytes) {
        try {
            dispatcherQueue.put(new DispatcherMessage(message_bytes, MulticastChanel.multicast_control_address, MulticastChanel.multicast_control_port, MulticastChanel.multicast_control_socket));
        } catch (InterruptedException e) {
            System.out.println("Error in adding Message to queue");
        }
        //send(message_bytes, MulticastChanel.multicast_control_address, MulticastChanel.multicast_control_port, MulticastChanel.multicast_control_socket);
    }

    public static byte[] encrypt(byte[] data) {
        return data;
    }

    public static byte[] decrypt(byte[] data) {
        return data;
    }

    @Override
    public void run() {
        try {
            while (true) {
                DispatcherMessage message = dispatcherQueue.take();
                send(message.data, message.address,message.port,message.socket);
                //System.out.println("Sent:"+new String(Arrays.copyOfRange(message.data,0,100)));
            }
        } catch (InterruptedException e){
            System.out.println("Dispatcher was interrupted...");
        }
    }


}
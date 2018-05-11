package Utilities;

import Executables.Peer;
import MulticastThreads.MulticastChanel;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher extends Thread{

    private final static LinkedBlockingQueue<DispatcherMessage> dispatcherQueue = new LinkedBlockingQueue<>(100);
    
    private static void send(byte[] message_bytes, String address, String port, MulticastSocket data_socket) {
        DatagramPacket packet;

        message_bytes = MessageCipher.groupCipher(message_bytes);
        
        message_bytes = sign(message_bytes);

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

    private static byte[] sign(byte[] data) {
        try {
            SecretKeySpec hks = new SecretKeySpec(Peer.peerKeyStore.getKey("clientRSA", "123456".toCharArray()).getEncoded(), "HmacSHA256");
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(hks);
            byte[] hmac = m.doFinal(data);

            byte[] os = new byte[data.length + 32];
            System.arraycopy(data, 0, os, 0, data.length);
            System.arraycopy(hmac, 0, os, data.length, 32);

            return os;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] authenticate(byte[] data, int size) {
        byte[] chmac = null;
        byte[] message = Arrays.copyOfRange(data, 0, size - 32);
        byte[] hmac = Arrays.copyOfRange(data, size - 32, size);
        try {
            SecretKeySpec hks = new SecretKeySpec(Peer.peerKeyStore.getKey("clientRSA", "123456".toCharArray()).getEncoded(), "HmacSHA256");
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(hks);
            chmac = m.doFinal(message);
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (MessageDigest.isEqual(hmac, chmac)) {
            //System.out.println("Foi autenticado");
            return message;
        } else {
            System.out.println("Autenticacão falhou");
            return null;
        }
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
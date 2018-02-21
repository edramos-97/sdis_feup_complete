package Xpac;

import java.net.DatagramPacket;

public class Xpac {

    private int size = 200;

    String message;
    DatagramPacket data_packet;


    public Xpac(String message){
        encapsulate(message);
    }

    public Xpac(DatagramPacket dpacket){
        data_packet = dpacket;
        message = new String(dpacket.getData()).trim();
    }

    public void encapsulate(String message_to_encapsulate){
        message = message_to_encapsulate;

        byte[] response = new byte[size];
        byte[] message_to_encapsulateBytes = message_to_encapsulate.getBytes();
        for (int i = 0; i < response.length; i++) {
            if(i < message_to_encapsulateBytes.length){
                response[i] = message_to_encapsulateBytes[i];
            }
            else {
                response[i] = 0;
            }
        }

        data_packet = new DatagramPacket(response, size);
    }



}

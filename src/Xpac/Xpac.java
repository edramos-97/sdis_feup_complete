package Xpac;

import java.net.DatagramPacket;

public class Xpac {

    public int size = 200;

    private String message;
    private DatagramPacket data_packet;
    private byte[] message_bytes;


    public Xpac(String message){
        encapsulate(message);
    }

    public Xpac(DatagramPacket dpacket){
        setData_packet(dpacket);
        setMessage(new String(dpacket.getData()).trim());
    }

    public void encapsulate(String message_to_encapsulate){
        setMessage(message_to_encapsulate);

        setMessage_bytes(new byte[size]);
        byte[] message_to_encapsulateBytes = message_to_encapsulate.getBytes();
        for (int i = 0; i < getMessage_bytes().length; i++) {
            if(i < message_to_encapsulateBytes.length){
                getMessage_bytes()[i] = message_to_encapsulateBytes[i];
            }
            else {
                getMessage_bytes()[i] = 0;
            }
        }

        setData_packet(new DatagramPacket(getMessage_bytes(), size));
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DatagramPacket getData_packet() {
        return data_packet;
    }

    public void setData_packet(DatagramPacket data_packet) {
        this.data_packet = data_packet;
    }

    public byte[] getMessage_bytes() {
        return message_bytes;
    }

    public void setMessage_bytes(byte[] message_bytes) {
        this.message_bytes = message_bytes;
    }
}

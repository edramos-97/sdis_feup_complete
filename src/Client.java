import java.net.*;
import java.io.*;
import Xpac.Xpac;

public class Client {

    public static void main(String[] args) throws IOException {
        // java Client <host_name> <port_number> <oper> <opnd>*

        DatagramSocket dsocket = new DatagramSocket();
        dsocket.setSoTimeout(4*1000);
        String message = new String();

        //Arrays.copyOfRange(args,2,args.length);
        //String joined2 = String.join(",", args);

        try {

            for (int i = 2; i < args.length; i++) {
                message = message.concat(args[i]);
                if (i != (args.length - 1))
                    message = message.concat(":");
            }

            byte[] to_send = new byte[100];
            byte[] message_bytes = message.getBytes();
            for (int i = 0; i < to_send.length; i++) {
                if (i < message_bytes.length) {
                    to_send[i] = message_bytes[i];
                } else {
                    to_send[i] = 0;
                }
            }
            DatagramPacket dpacket = new DatagramPacket(
                    to_send,
                    100,
                    InetAddress.getByName(args[0]),
                    Integer.parseInt(args[1]));

            dsocket.send(dpacket);
            System.out.println("Sent:");
            System.out.println(message);

            dsocket.receive(dpacket);
            String response = new String(dpacket.getData()).trim();
            System.out.println("Received:");
            System.out.println(response);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

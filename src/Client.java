import java.net.*;
import java.io.*;
import Xpac.Xpac;

public class Client {

    public static void main(String[] args) throws IOException {
        // java Client <host_name> <port_number> <oper> <opnd>*
        // ***
        // java client <mcast_addr> <mcast_port> <oper> <opnd> *

        String mcast_addr = args[0];
        String mcast_port = args[1];

        // creating socket and joining group
        MulticastSocket msocket = new MulticastSocket(Integer.parseInt(mcast_port));
        msocket.joinGroup(InetAddress.getByName(mcast_addr));

        // receiving message
        byte[] raw_message = new byte[Xpac.size];
        DatagramPacket packet_server = new DatagramPacket(raw_message, Xpac.size);
        msocket.receive(packet_server);
        Xpac info_server = new Xpac(packet_server);

        msocket.leaveGroup(InetAddress.getByName(mcast_addr));

        String[] info = info_server.getMessage().split("/");
        String address = info[1]; // could send through server
        int port = Integer.parseInt(info[0]);




        DatagramSocket dsocket = new DatagramSocket();
        dsocket.setSoTimeout(10*1000);
        String message = new String();

        //Arrays.copyOfRange(args,2,args.length);
        //String joined2 = String.join(",", args);

        try {

            for (int i = 2; i < args.length; i++) {
                message = message.concat(args[i]);
                if (i != (args.length - 1))
                    message = message.concat(":");
            }

            Xpac xpac_message = new Xpac(message);
            DatagramPacket dpacket = new DatagramPacket(
                    xpac_message.getMessage_bytes(),
                    xpac_message.size,
                    InetAddress.getByName(address),
                    port);

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

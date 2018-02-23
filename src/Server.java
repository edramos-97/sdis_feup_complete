import java.net.*;
import java.io.*;
import java.util.Hashtable;
import Xpac.Xpac;

public class Server {
    public static Hashtable<String, String> database = new Hashtable<>();

    public class MyThread extends Thread {

    }

    public static void main(String[] args) throws IOException {
        // java Server <port_number>
        // ***
        // java Server <srvc_port> <mcast_addr> <mcast_port>

        (new Thread(){
            @Override
            public void run(){
                String mcast_addr = args[1];
                String mcast_port = args[2];

                try {
                    MulticastSocket mcast_socket = new MulticastSocket();
                    Xpac message_to_broadc = new Xpac(args[0]);
                    DatagramPacket packet_to_broad = new DatagramPacket(
                            message_to_broadc.getMessage_bytes(),
                            message_to_broadc.size,
                            InetAddress.getByName(mcast_addr),
                            Integer.parseInt(mcast_port));

                    while(true){
                        mcast_socket.send(packet_to_broad);
                        System.out.println("Broadcasted my PORT");
                        Thread.sleep(4000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("SLEEP WAS INTERRUPTED");
                }


            }
        }).start();

        int port = Integer.parseInt(args[0]);
        DatagramSocket dsocket = new DatagramSocket(port);

        while (true){

            byte[] raw_message = new byte[Xpac.size];

            DatagramPacket dpacket = new DatagramPacket(raw_message, Xpac.size);
            dsocket.receive(dpacket);
            Xpac xpac_message = new Xpac(dpacket);

            System.out.println("Received:");
            System.out.println(xpac_message.getMessage());
            String[] request = xpac_message.getMessage().split(":");
            String response_message;

            switch (request[0]){
                case "lookup":
                    response_message = "NOT IN DATABASE";
                    if(database.containsKey(request[1])){
                        response_message = database.get(request[1]);
                    }

                    break;
                case "register":
                    response_message = "ADDED";
                    if(database.containsKey(request[1])){
                        response_message = "ALREADY IN DATABASE";
                    }
                    else {
                        database.put(request[1], request[2]);
                    }
                    break;
                default:
                    response_message = "ERROR PROCESSING";
                    break;
            }

            Xpac xpac_response = new Xpac(response_message);
            dpacket.setData(xpac_response.getMessage_bytes());
            dsocket.send(dpacket);
            System.out.println("Responded:");
            System.out.println(response_message);
        }
    }
}

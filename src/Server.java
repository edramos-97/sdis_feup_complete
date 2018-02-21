import java.net.*;
import java.io.*;
import java.util.Hashtable;
import Xpac.Xpac;

public class Server {
    public static Hashtable<String, String> database = new Hashtable<>();

    public static void main(String[] args) throws IOException {
        // java Server <port_number>

        int port = Integer.parseInt(args[0]);
        DatagramSocket dsocket = new DatagramSocket(port);

        while (true){

            byte[] raw_message = new byte[100];

            DatagramPacket dpacket = new DatagramPacket(raw_message, 100);
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

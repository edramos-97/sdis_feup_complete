import java.net.*;
import java.io.*;
import java.util.Hashtable;

public class Server {
    public static Hashtable<String, String> database = new Hashtable<>();

    public static void main(String[] args) throws IOException {
        // java Server <port_number>

        int port = Integer.parseInt(args[0]);
        DatagramSocket dsocket = new DatagramSocket(port);

        while (true){

            byte[] raw_message = new byte[100];
            String message;
            DatagramPacket dpacket = new DatagramPacket(raw_message, 100);
            dsocket.receive(dpacket);
            message = new String(dpacket.getData()).trim();

            System.out.println("Received:");
            System.out.println(message);
            String[] request = message.split(":");
            String response_message;
            byte[] response_bytes;
            byte[] response;
            switch (request[0]){
                case "lookup":
                    response_message = "NOT IN DATABASE";
                    if(database.containsKey(request[1])){
                        response_message = database.get(request[1]);
                    }
                    response = new byte[100];
                    response_bytes = response_message.getBytes();
                    for (int i = 0; i < response.length; i++) {
                        if(i < response_bytes.length){
                            response[i] = response_bytes[i];
                        }
                        else {
                            response[i] = 0;
                        }
                    }

                    dpacket.setData(response);
                    dsocket.send(dpacket);
                    System.out.println("Responded:");
                    System.out.println(response_message);
                    break;
                case "register":
                    response_message = "ADDED";
                    if(database.containsKey(request[1])){
                        System.out.println("damn");
                        response_message = "ALREADY IN DATABASE";
                    }
                    else {
                        database.put(request[1], request[2]);
                    }
                    response = new byte[100];
                    response_bytes = response_message.getBytes();
                    for (int i = 0; i < response.length; i++) {
                        if(i < response_bytes.length){
                            response[i] = response_bytes[i];
                        }
                        else {
                            response[i] = 0;
                        }
                    }

                    dpacket.setData(response);
                    dsocket.send(dpacket);
                    System.out.println("Responded:");
                    System.out.println(response_message);
                    break;
                default:
                    response_message = "ERROR PROCESSING";
                    response = new byte[100];
                    response_bytes = response_message.getBytes();
                    for (int i = 0; i < response.length; i++) {
                        if(i < response_bytes.length){
                            response[i] = response_bytes[i];
                        }
                        else {
                            response[i] = 0;
                        }
                    }

                    dpacket.setData(response);
                    dsocket.send(dpacket);
                    System.out.println("Responded:");
                    System.out.println(response_message);
                    break;
            }
        }
    }
}

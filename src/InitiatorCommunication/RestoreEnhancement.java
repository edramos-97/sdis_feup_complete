package InitiatorCommunication;

import MulticastThreads.MulticastChanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class RestoreEnhancement implements Runnable {

    private String fileID;
    private String name;

    public RestoreEnhancement(String fileId, String name) {
        this.fileID = fileId;
        this.name = name;

    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket receiving_socket = MulticastChanel.tcp_ss.accept();

                BufferedReader inFromOtherPeer = new BufferedReader(new InputStreamReader(receiving_socket.getInputStream()));

                System.out.println(inFromOtherPeer.readLine());


            } catch (IOException e) {
                System.out.println("There was an error accepting socket...");
                e.printStackTrace();
                break;
            }
        }
    }
}

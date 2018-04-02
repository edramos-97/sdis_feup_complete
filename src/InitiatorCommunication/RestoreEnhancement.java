package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class RestoreEnhancement implements Runnable {
    // TODO RESTORE-ENHANCEMENT
    private ServerSocket tcp_ss;

    public static ConcurrentHashMap<String, ProtocolMessage> can_save_these = new ConcurrentHashMap<>();

    public RestoreEnhancement(ServerSocket tcp_ss) {
        this.tcp_ss = tcp_ss;
    }

    @Override
    public void run() {
        while(true) {
            try {

                System.out.println("Running and reading from socket");
                Socket receiving_socket = tcp_ss.accept();

                BufferedReader inFromOtherPeer = new BufferedReader(new InputStreamReader(receiving_socket.getInputStream()));

                char[] read_chars = new char[FileHandler.MAX_SIZE_MESSAGE];
                int actually_read = inFromOtherPeer.read(read_chars, 0, FileHandler.MAX_SIZE_MESSAGE);
                //System.out.println("READ SOME BYTES YE");
                //System.out.println(read_chars);
                //System.out.println(actually_read);
                //System.out.println("READ SOME BYTES YEND");

                byte[] read_bytes = new byte[actually_read];

                for(int i = 0; i < actually_read; i++) {
                    read_bytes[i] = (byte) read_chars[i];
                }


                ProtocolMessage message = ProtocolMessageParser.parseMessage(read_bytes,read_bytes.length);

                // Not getting here so not really receiving stuff on tcp connection
                System.out.println("I RECEIVED --" + message.getFileId()+message.getChunkNo());

                can_save_these.put(message.getFileId()+message.getChunkNo(), message);

                //VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                //FileHandler.saveChunk(message,"restore");


            } catch (IOException e) {
                System.out.println("There was an error accepting socket...");
                e.printStackTrace();
                break;
            }
        }
    }
}

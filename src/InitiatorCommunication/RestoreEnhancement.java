package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class RestoreEnhancement implements Runnable {
    // TODO RESTORE-ENHANCEMENT
    public static ConcurrentHashMap<String, ProtocolMessage> can_save_these = new ConcurrentHashMap<>();

    public RestoreEnhancement() {

    }

    @Override
    public void run() {
        while(true) {
            try {
                ServerSocket tcp_ss = new ServerSocket(5678);
                System.out.println("Running and reading from socket");

                Socket receiving_socket = tcp_ss.accept();

                DataInputStream in = new DataInputStream(receiving_socket.getInputStream());

                byte[] read_chars = new byte[FileHandler.MAX_SIZE_MESSAGE];
                int actually_read = in.read(read_chars, 0, FileHandler.MAX_SIZE_MESSAGE);

                byte[] read_bytes = Arrays.copyOfRange(read_chars, 0, actually_read);

                ProtocolMessage message = ProtocolMessageParser.parseMessage(read_bytes,read_bytes.length);

                //System.out.println("I RECEIVED --" + message.getFileId()+message.getChunkNo());

                can_save_these.put(message.getFileId()+message.getChunkNo(), message);

                //VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                //FileHandler.saveChunk(message,"restore");

                receiving_socket.close();
                tcp_ss.close();

            } catch (IOException e) {
                System.out.println("RestoreEnhancement - There was an error accepting socket.");
                //e.printStackTrace();
                break;
            }
        }
    }
}

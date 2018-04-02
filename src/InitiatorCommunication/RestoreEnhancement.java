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
                //ServerSocket tcp_ss = new ServerSocket()
                Socket receiving_socket = tcp_ss.accept();

                //BufferedReader inFromOtherPeer = new BufferedReader(new InputStreamReader(receiving_socket.getInputStream()));

                DataInputStream in = new DataInputStream(receiving_socket.getInputStream());

                byte[] read_chars = new byte[FileHandler.MAX_SIZE_MESSAGE];
                int actually_read = in.read(read_chars, 0, FileHandler.MAX_SIZE_MESSAGE);
                //System.out.println("READ SOME BYTES YE");
                //System.out.println(read_chars);
                //System.out.println(actually_read);
                //System.out.println("READ SOME BYTES YEND");

                byte[] read_bytes = Arrays.copyOfRange(read_chars, 0, actually_read);
                
                ProtocolMessage message = ProtocolMessageParser.parseMessage(read_bytes,read_bytes.length);

                // Not getting here so not really receiving stuff on tcp connection
                System.out.println("I RECEIVED --" + message.getFileId()+message.getChunkNo());

                can_save_these.put(message.getFileId()+message.getChunkNo(), message);

                //VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                //FileHandler.saveChunk(message,"restore");

                receiving_socket.close();

            } catch (IOException e) {
                System.out.println("There was an error accepting socket...");
                e.printStackTrace();
                break;
            }
        }
    }
}

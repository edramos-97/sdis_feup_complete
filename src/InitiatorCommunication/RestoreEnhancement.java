package InitiatorCommunication;

import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;
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
                inFromOtherPeer.read(read_chars, 0, FileHandler.MAX_SIZE_MESSAGE);
                System.out.println("READ SOME BYTES YE");
                System.out.println(read_chars);
                System.out.println(read_chars.length);
                System.out.println("READ SOME BYTES YEND");

                ArrayList<Byte> read_bytes_w = new ArrayList<>();

                for(int i = 0; i < read_chars.length; i++){
                    if(read_chars[i] == ' ')
                        break;
                    read_bytes_w.add((byte) read_chars[i]);
                }

                byte[] read_bytes = new byte[read_bytes_w.size()];
                for(int i = 0; i < read_bytes_w.size(); i++){
                    read_bytes[i] = read_bytes_w.get(i);
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

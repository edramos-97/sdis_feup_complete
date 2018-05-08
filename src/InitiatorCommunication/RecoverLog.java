package InitiatorCommunication;

import StateRecovery.RecoveryInitiator;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class RecoverLog implements Runnable {

    private File logChunk;

    public RecoverLog(File file) {
        this.logChunk = file;
    }

    @Override
    public void run() {
        ProtocolMessage m = new ProtocolMessage(ProtocolMessage.PossibleTypes.CHUNKLOG);
        try {
            m.setFileId(RecoveryInitiator.fileID);
            m.setChunkNo(logChunk.getName().split("\\.")[0]);
            m.setBody(Files.readAllBytes(logChunk.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Dispatcher.sendRecover(m.toCharArray());

        /*
        ServerSocket s = null;
        ProtocolMessage m = new ProtocolMessage(ProtocolMessage.PossibleTypes.CHUNKLOG);
        try {
            s = new ServerSocket(0);
            System.out.println("here; " + RecoveryInitiator.chunkNumber);
            m.setFileId(RecoveryInitiator.fileID);
            m.setChunkNo(logChunk.getName().split("\\.")[0]);

            s.setSoTimeout(450);
            System.out.println("Sent address is:" + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Sent port is:" + s.getLocalPort());
            m.setBody((InetAddress.getLocalHost().getHostAddress() + ":" + s.getLocalPort()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Dispatcher.sendData(m.toCharArray());
        try {
            Socket dataSocket = s.accept();
            DataOutputStream dataStream = new DataOutputStream(dataSocket.getOutputStream());
            byte[] data = FileHandler.getChunk(m.getFileId(),Short.valueOf(m.getChunkNo()));
            dataStream.write(data);
            dataSocket.close();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("No connection established to send chunk data");
        }*/
    }
}

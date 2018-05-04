package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.ProtocolMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class GetChunkRequest implements Callable<String>{
    private String fileId;
    private short chunkNo;
    private String fileName;
    private String version;

    public GetChunkRequest(String fileId, short chunkNo,String fileName,String version){
        this.fileId=fileId;
        this.chunkNo = chunkNo;
        this.fileName = fileName;
        this.version = version;
    }

    @Override
    public String call() {
        ProtocolMessage message;

        // BUILD MESSAGE
        message = new ProtocolMessage(ProtocolMessage.PossibleTypes.GETCHUNK);
        try {
            message.setFileId(fileId);
            message.setVersion(version);
            message.setChunkNo(String.valueOf(chunkNo));
        } catch (Exception e) {
            //e.printStackTrace();
            return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage()+"\n";
        }

        //send Getchunk
        Dispatcher.sendControl(message.toCharArray());

        Peer.threadPool.schedule(new GetChunkVerification(0,message,fileName),450, TimeUnit.MILLISECONDS);
        return "";
    }
}

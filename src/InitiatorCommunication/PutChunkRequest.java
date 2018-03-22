package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

public class PutChunkRequest implements Callable<String>{

    private String filePath;
    private short chunkNo;
    private char replicationDeg;
    private ProtocolMessage message;
    public static short TIMEOUT = 1000;
    public static short MAX_TRIES = 5;

    public PutChunkRequest(String filePath, short chunkNo, char replicationDeg) throws Exception {
        if(new File(filePath).isDirectory()){
            throw new Exception("File path specified in PUTCHUNK request is a directory.\nFile Path: "+filePath);
        }
        this.filePath = filePath;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
    }

    @Override
    public String call() {
        this.message = new ProtocolMessage(ProtocolMessage.PossibleTypes.PUTCHUNK);
        try {
            message.setFileId(FileHandler.getFileId(filePath));
            message.setChunkNo(String.valueOf(chunkNo));
            message.setReplicationDeg(this.replicationDeg);
            Path path = Paths.get(filePath);
            AsynchronousFileChannel file = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            file.read(message.body,chunkNo*FileHandler.CHUNK_SIZE,message,new PutChunkReadComplete());
            System.out.println("AFTER FILE READ");
        } catch (Exception e) {
            e.printStackTrace();
            //return "PutChunk for fileID:\""+filePath+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage();
        }
        return "Success!";
    }
}

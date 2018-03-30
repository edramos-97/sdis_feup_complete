package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.File_IO_Wrapper;
import Utilities.VolatileDatabase;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

public class PutChunkRequest implements Callable<String>{

    private String filePath;
    private short chunkNo;
    private char replicationDeg;
    private int threadNo;

    public PutChunkRequest(String filePath, short chunkNo, char replicationDeg, int threadNo) throws Exception {
        if(new File(filePath).isDirectory()){
            throw new Exception("File path specified in PUTCHUNK request is a directory.\nFile Path: "+filePath);
        }
        //TODO check if putChunk is from RECLAIM sub protocol
        this.filePath = filePath;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.threadNo = threadNo;
    }

    @Override
    public String call() {
        ProtocolMessage message;
        message = new ProtocolMessage(ProtocolMessage.PossibleTypes.PUTCHUNK);
        message.setThreadNo(threadNo);
        try {
            String fileID = FileHandler.getFileId(new File(filePath));

            if(fileID == null){
                System.out.println("PUTCHUNK filepath points to a directory, stopping protocol...");
                return "";
            }

            message.setFileId(fileID);

            if(!VolatileDatabase.backed2fileID.containsKey(fileID))
                VolatileDatabase.backed2fileID.put(fileID, filePath);

            message.setChunkNo(String.valueOf(chunkNo));
            message.setReplicationDeg(this.replicationDeg);
            message.setThreadNo(this.threadNo);
            message.setFile(filePath);

            Path path = Paths.get(filePath);
            AsynchronousFileChannel file = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            file.read(ByteBuffer.wrap(message.body),chunkNo*FileHandler.CHUNK_SIZE,new File_IO_Wrapper(message,file),new PutChunkReadComplete());

        } catch (Exception e) {
            e.printStackTrace();
            return "PutChunk for fileID:\""+filePath+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage();
        }
        return "";
    }
}

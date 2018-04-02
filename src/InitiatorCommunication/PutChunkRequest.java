package InitiatorCommunication;

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
    private String fileID = "";
    private String version;

    public PutChunkRequest(String filePath, short chunkNo, char replicationDeg, int threadNo, String version) throws Exception {
        if(new File(filePath).isDirectory()){
            throw new Exception("File path specified in PUTCHUNK request is a directory.\nFile Path: "+filePath);
        }
        this.filePath = filePath;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.threadNo = threadNo;
        this.version = version;
    }

    PutChunkRequest(String filePath, short chunkNo, char replicationDeg, int threadNo, String fileID, String version) throws Exception {
        if(new File(filePath).isDirectory()){
            throw new Exception("File path specified in PUTCHUNK request is a directory.\nFile Path: "+filePath);
        }
        this.filePath = filePath;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.threadNo = threadNo;
        this.fileID = fileID;
        this.version = version;
    }

    @Override
    public String call() {
        ProtocolMessage message;
        message = new ProtocolMessage(ProtocolMessage.PossibleTypes.PUTCHUNK);
        message.setThreadNo(threadNo);
        try {
            message.setVersion(version);
            String fileID;
            if(this.fileID.equals(""))
                fileID = FileHandler.getFileId(new File(filePath));
            else
                fileID = this.fileID;

            if(fileID == null){
                System.out.println("PUTCHUNK filepath points to a directory, stopping protocol...");
                return "";
            }

            message.setFileId(fileID);

            if(!VolatileDatabase.backed2fileID.containsKey(fileID) && this.fileID.equals(""))
                VolatileDatabase.backed2fileID.put(fileID, filePath);
            VolatileDatabase.restoreDelete(fileID);

            message.setChunkNo(String.valueOf(chunkNo));
            message.setReplicationDeg(this.replicationDeg);
            message.setThreadNo(this.threadNo);
            message.setFile(filePath);

            Path path = Paths.get(filePath);
            AsynchronousFileChannel file = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            if(!this.fileID.equals("")){
                file.read(ByteBuffer.wrap(message.body),0,new File_IO_Wrapper(message,file),new PutChunkReadComplete());
            }else{
                file.read(ByteBuffer.wrap(message.body),chunkNo*FileHandler.CHUNK_SIZE,new File_IO_Wrapper(message,file),new PutChunkReadComplete());
            }

        } catch (Exception e) {
            System.out.println("PutChunkRequest - Error:" + e.toString());
            //e.printStackTrace();
            return "PutChunk for fileID:\""+filePath+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage();
        }
        return "";
    }
}

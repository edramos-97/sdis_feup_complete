package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import java.io.File;
import java.util.concurrent.Callable;

public class PutChunkRequest implements Callable<String>{

    private String filePath;
    private short chunkNo;
    private char replicationDeg;
    private ProtocolMessage message;

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
        short i = 0;
        this.message = new ProtocolMessage(ProtocolMessage.PossibleTypes.PUTCHUNK);
        try {
            message.setFileId(FileHandler.getFileId(filePath));
            message.setChunkNo(String.valueOf(chunkNo));
            message.setReplicationDeg(this.replicationDeg);
            message.setBody(FileHandler.splitFile(filePath,chunkNo));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        while (i<5){
            //TODO - send putchunk command to MDB
            try {
                Thread.sleep(1000);//wait for answers
                int effReplicationDegree = 5;//TODO - hashMap.get(message.fileId+message.chunkNo).effReplicationDegree
                if(effReplicationDegree >= message.getReplicationDeg()){
                    return "Putchunk for fileID:"+message.getFileId()+" chunkNo:"+message.getChunkNo()+" finished successfully";
                }else{
                    i++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

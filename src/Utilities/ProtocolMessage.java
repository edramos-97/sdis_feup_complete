package Utilities;

import Executables.Peer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

public class ProtocolMessage {

    public enum PossibleTypes {PUTCHUNK,PUTLOGCHUNK,STORED,GETCHUNK,CHUNK,DELETE,DELETECONF,REMOVED,BACKEDUP,RECOVERASKMAX,RECOVERMAX, CHUNKLOG, GETLOGS}

    private PossibleTypes msgType;
    private String version;
    private String senderId;
    private String fileId;
    private String ChunkNo;
    private char ReplicationDeg;
    private int threadNo;
    private String filePath;
    public byte[] body = new byte[FileHandler.CHUNK_SIZE];
    boolean hasBody;

    public ProtocolMessage(PossibleTypes msgType){
        setMsgType(msgType);
        this.version = Peer.VERSION;
        this.senderId = String.valueOf(Peer.peerID);
    }

    public PossibleTypes getMsgType() {
        return msgType;
    }

    public void setMsgType(PossibleTypes type) {
        this.msgType = type;
        switch (msgType){
            case PUTLOGCHUNK:
            case PUTCHUNK:
            case CHUNK:
            case CHUNKLOG:
                this.hasBody = true; break;
            case DELETE:
            case STORED:
            case REMOVED:
            case GETCHUNK:
            case DELETECONF:
            case RECOVERASKMAX:
            case RECOVERMAX:
            case BACKEDUP:
            case GETLOGS:
                this.hasBody = false; break;
            default:
                System.out.println("Invalid data type in Message Constructor");
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) throws Exception {
        if (!version.matches("[0-9]\\.[0-9]")){
            throw new Exception("Invalid data field received: version="+version);
        }
        this.version = version;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) throws Exception {
        if(!senderId.matches("[0-9]+")){
            throw new Exception("Invalid data field received: senderId="+senderId);
        }
        this.senderId = senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) throws InvalidParameterException{
        if (!fileId.matches("[0-9a-fA-F]{64}")){
            throw new InvalidParameterException("Invalid data field received: fileId=\""+fileId+"\". Format must be [0-9a-fA-F]{64}");
        }
        this.fileId = fileId;
    }

    public String getChunkNo() {
        return ChunkNo;
    }

    public void setChunkNo(String chunkNo) throws Exception {
        if(!chunkNo.matches("[0-9]{1,6}")){
            throw new Exception("Invalid data field received: chunkNo="+chunkNo);
        }
        ChunkNo = chunkNo;
    }

    public short getReplicationDeg() {
        return (short) Character.getNumericValue(ReplicationDeg);
    }

    public void setReplicationDeg(char replicationDeg) throws Exception {
        if(!Character.isDigit(replicationDeg)||replicationDeg=='0'){
            throw new Exception("Invalid data field received: replicationDegree="+replicationDeg);
        }
        ReplicationDeg = replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
        //System.out.println("Body Length: "+body.length);
        //System.out.println("Message Body length: "+ this.body.length);
    }

    public void setThreadNo(int threadNo) {
        this.threadNo = threadNo;
    }

    public int getThreadNo() {
        return threadNo;
    }

    public String getFile() {
        return filePath;
    }

    public void setFile(String filePath) {
        this.filePath = filePath;
    }

    public byte[] toCharArray(){
        byte[] common = String.format("%s %s %s %s",msgType,version,senderId,fileId).getBytes(StandardCharsets.ISO_8859_1);

        // PUTCHUNK <version> <senderId> <fileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
        // STORED <version> <senderId> <fileId> <ChunkNo> <CRLF><CRLF>
        // GETCHUNK <version> <senderId> <fileId> <ChunkNo> <CRLF><CRLF>
        // CHUNK <version> <senderId> <fileId> <ChunkNo> <CRLF><CRLF><Body>
        // DELETE <version> <senderId> <fileId> <CRLF><CRLF>
        // REMOVED <version> <senderId> <fileId> <ChunkNo> <CRLF><CRLF>

        byte[] complement = new byte[]{};

        switch (msgType){
            case PUTLOGCHUNK:
            case PUTCHUNK:
                complement = String.format(" %s %s\r\n\r\n", getChunkNo(),getReplicationDeg()).getBytes();
                break;
            case CHUNK:
            case STORED:
            case GETCHUNK:
            case REMOVED:
            case RECOVERMAX:
            case CHUNKLOG:
                complement = String.format(" %s\r\n\r\n",getChunkNo()).getBytes();
                break;
            case DELETE:
            case DELETECONF:
            case BACKEDUP:
            case RECOVERASKMAX:
            case GETLOGS:
                complement = "\r\n\r\n".getBytes();
                break;
        }

        byte[] header = new byte[common.length + complement.length];
        System.arraycopy(common,0,header,0,common.length);
        System.arraycopy(complement,0,header,common.length,complement.length);

        if (hasBody){
            byte[] result = new byte[header.length + body.length];
            System.arraycopy(header,0,result,0,header.length);
            body = MessageCipher.privateCipher(body);
            System.arraycopy(body,0,result,header.length,body.length);
            return result;
        }else{
            return header;
        }
    }
}

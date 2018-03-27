package Utilities;

import Executables.Peer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

public class ProtocolMessage {

    public enum PossibleTypes {PUTCHUNK,STORED,GETCHUNK,CHUNK,DELETE,REMOVED}

    private PossibleTypes msgType;
    private String version;
    private String senderId;
    private String fileId;
    private String ChunkNo;
    private char ReplicationDeg;
    public ByteBuffer body;
    boolean hasBody;

    public ProtocolMessage(PossibleTypes msgType){
        this.msgType = msgType;
        this.version = Peer.VERSION;
        this.senderId = String.valueOf(Peer.peerID);
        switch (msgType){
            case PUTCHUNK:
            case CHUNK:
                this.hasBody = true; break;
            case DELETE:
            case STORED:
            case REMOVED:
            case GETCHUNK:
                this.hasBody = false; break;
            default:
                System.out.println("Invalid message type in Message Constructor");
        }
        this.body = ByteBuffer.allocate(FileHandler.CHUNK_SIZE);
    }

    public PossibleTypes getMsgType() {
        return msgType;
    }

    public void setMsgType(PossibleTypes type) {
        this.msgType = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) throws Exception {
        if (!version.matches("[0-9]\\.[0-9]")){
            throw new Exception("Invalid message field received: version="+version);
        }
        this.version = version;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) throws Exception {
        if(!senderId.matches("[0-9]+")){
            throw new Exception("Invalid message field received: senderId="+senderId);
        }
        this.senderId = senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) throws InvalidParameterException{
        if (!fileId.matches("[0-9a-fA-F]{64}")){
            throw new InvalidParameterException("Invalid message field received: fileId=\""+fileId+"\". Format must be [0-9a-fA-F]{64}");
        }
        this.fileId = fileId;
    }

    public String getChunkNo() {
        return ChunkNo;
    }

    public void setChunkNo(String chunkNo) throws Exception {
        if(!chunkNo.matches("[0-9]{1,6}")){
            throw new Exception("Invalid message field received: chunkNo="+chunkNo);
        }
        ChunkNo = chunkNo;
    }

    public int getReplicationDeg() {
        return Character.getNumericValue(ReplicationDeg);
    }

    public void setReplicationDeg(char replicationDeg) throws Exception {
        if(!Character.isDigit(replicationDeg)||replicationDeg=='0'){
            throw new Exception("Invalid message field received: replicationDegree="+replicationDeg);
        }
        ReplicationDeg = replicationDeg;
    }

    public ByteBuffer getBody() {
        return body;
    }

    public void setBody(ByteBuffer body) {
        this.body = body;
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
            case PUTCHUNK:
                complement = String.format(" %s %s\r\n\r\n", getChunkNo(),getReplicationDeg()).getBytes();
                break;
            case CHUNK:
            case STORED:
            case GETCHUNK:
            case REMOVED:
                complement = String.format(" %s\r\n\r\n",getChunkNo()).getBytes();
            case DELETE:
                break;
        }

        byte[] header = new byte[common.length + complement.length];
        System.arraycopy(common,0,header,0,common.length);
        System.arraycopy(complement,0,header,common.length,complement.length);


        if (hasBody){
            byte[] temp = new byte[body.position()];
            body.flip();
            body.get(temp);
            byte[] result = new byte[header.length + temp.length];
            System.arraycopy(header,0,header,0,header.length);
            System.arraycopy(temp,0,result,header.length,temp.length);
            return result;
        }else{
            return header;
        }
    }
}

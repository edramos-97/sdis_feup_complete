package Utilities;

public class ProtocolMessage {

    enum PossibleTypes {PUTCHUNK,STORED,GETCHUNK,CHUNK,DELETE,REMOVED}

    private PossibleTypes msgType;
    private String Version;
    private String SenderId;
    private String FileId;
    private String ChunkNo;
    private char ReplicationDeg;
    private String body;
    boolean hasBody;

    ProtocolMessage(PossibleTypes msgType){
        this.msgType = msgType;
        this.hasBody = false;
        switch (msgType){
            case GETCHUNK:
            case CHUNK:
                this.hasBody = true; break;
            case DELETE:
            case STORED:
            case REMOVED:
            case PUTCHUNK:
                this.hasBody = true; break;
            default:
                System.out.println("Invalid message type in Message Constructor");
        }
    }

    public PossibleTypes getMsgType() {
        return msgType;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) throws Exception {
        if (!version.matches("[0-9]/.[0-9]")){
            throw new Exception("Invalid message field received: version="+version);
        }
        Version = version;
    }

    public String getSenderId() {
        return SenderId;
    }

    public void setSenderId(String senderId) throws Exception {
        if(!senderId.matches("[0-9]+")){
            throw new Exception("Invalid message field received: senderId="+senderId);
        }
        SenderId = senderId;
    }

    public String getFileId() {
        return FileId;
    }

    public void setFileId(String fileId) throws Exception {
        if (!fileId.matches("[0-9a-zA-Z]{64}")){
            throw new Exception("Invalid message field received: fileId="+fileId);
        }
        FileId = fileId;
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

    public char getReplicationDeg() {
        return ReplicationDeg;
    }

    public void setReplicationDeg(char replicationDeg) throws Exception {
        if(!Character.isDigit(replicationDeg)){
            throw new Exception("Invalid message field received: replicationDegree="+replicationDeg);
        }
        ReplicationDeg = replicationDeg;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public byte[] toCharArray(){
        String result = "hey";

        // PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
        // STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        // GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        // CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        // DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
        // REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>


        return result.getBytes();
    }
}

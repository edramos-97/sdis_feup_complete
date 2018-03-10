package Utilities;

public class ProtocolMessageParser {

    public ProtocolMessage parseMessage(String receivedMessage){
        String[] msgFields = receivedMessage.split("\r\n\r\n");
        String[] headerFields = msgFields[0].split(" ");
        ProtocolMessage.PossibleTypes tempType = verifyType(headerFields[0]);
        ProtocolMessage tempMessage;
        if (tempType==null){
            return null;
        }else {
            tempMessage = new ProtocolMessage(tempType);
        }
        setFields(tempMessage,headerFields);

        try{
            if (tempMessage.hasBody){
                tempMessage.setBody(msgFields[1]);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return tempMessage;
    }

    private ProtocolMessage.PossibleTypes verifyType(String value){
        for (ProtocolMessage.PossibleTypes c : ProtocolMessage.PossibleTypes.values()) {
            if (c.name().equals(value)) {
                return c;
            }
        }
        return null;
    }

    private boolean setFields(ProtocolMessage tempMessage,String[] headerFields) {

        try {
            tempMessage.setVersion(headerFields[1]);
            tempMessage.setSenderId(headerFields[2]);
            tempMessage.setFileId(headerFields[3]);
            switch (tempMessage.getMsgType()) {
                case PUTCHUNK:
                    tempMessage.setChunkNo(headerFields[4]);
                    tempMessage.setReplicationDeg(headerFields[5].charAt(0));
                    break;
                case GETCHUNK:
                case STORED:
                case CHUNK:
                case REMOVED:
                    tempMessage.setChunkNo(headerFields[4]);
                    break;
                case DELETE:
                    break;

                default:
                    throw new Exception("Invalid message type received!");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

}

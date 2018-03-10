public class MessageParser {

    public Message parseMessage(String receivedMessage){
        String[] msgFields = receivedMessage.split("\r\n\r\n");
        String[] headerFields = msgFields[0].split(" ");
        Message.PossibleTypes tempType = verifyType(headerFields[0]);
        Message tempMessage;
        if (tempType==null){
            return null;
        }else {
            tempMessage = new Message(tempType);
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

    public Message.PossibleTypes verifyType(String value){
        for (Message.PossibleTypes c : Message.PossibleTypes.values()) {
            if (c.name().equals(value)) {
                return c;
            }
        }
        return null;
    }

    private boolean setFields(Message tempMessage,String[] headerFields){

        try {
            tempMessage.setVersion(headerFields[1]);
            tempMessage.setSenderId(headerFields[2]);
            tempMessage.setFileId(headerFields[3]);
            switch(tempMessage.getMsgType()){
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
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}

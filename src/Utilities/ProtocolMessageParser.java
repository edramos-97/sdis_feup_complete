package Utilities;

import java.nio.ByteBuffer;

public class ProtocolMessageParser {

    public static ProtocolMessage parseMessage(byte[] receivedMessage){
        byte[][] msgFields = splitParts(receivedMessage);
        String[] headerFields = new String(msgFields[0]).split("[ ]+");
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
                tempMessage.setBody(msgFields[1],msgFields[1].length);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return tempMessage;
    }

    /**
     * Function to verify if the received message type is a valid one
     * @param value - Received data as a String
     * @return Returns a PossibleType value or null
     */
    private static ProtocolMessage.PossibleTypes verifyType(String value){
        for (ProtocolMessage.PossibleTypes c : ProtocolMessage.PossibleTypes.values()) {
            if (c.name().equals(value)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Function to set message fields from a string array
     * @param tempMessage - Message to be set
     * @param headerFields - String array with information about a message
     * @return False if an invalid message field is received, true otherwise
     */
    private static boolean setFields(ProtocolMessage tempMessage,String[] headerFields) {
        try {
            tempMessage.setVersion(headerFields[1]);
            tempMessage.setSenderId(headerFields[2]);
            tempMessage.setFileId(headerFields[3]);
            switch (tempMessage.getMsgType()) {
                case PUTCHUNK:
                    tempMessage.setChunkNo(headerFields[4]);
                    int temp = Integer.parseInt(headerFields[5]);
                    if (headerFields[5].length()==1 && temp!=0){
                        tempMessage.setReplicationDeg(headerFields[5].charAt(0));
                    }else if(temp<=0){
                        System.out.println("Assuming replication degree 1");
                        throw new Exception("Invalid message field received: replicationDegree="+temp);
                    }else if (temp>9){
                        System.out.println("Assuming replication degree 9");
                        throw new Exception("Invalid message field received: replicationDegree="+temp);
                    }
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

    private static byte[][] splitParts(byte[] message){
        byte[] key = "\r\n\r\n".getBytes();
        //System.out.println("this is a key: " + new String(key));
        int i = 0;
        int k = message.length-1;
        while (message[k] == 0){
            k--;
        }
        for (; i <= message.length - key.length; i++) {
            int j = 0;
            while (j < key.length && message[i + j] == key[j]) {
                j++;
            }
            if (j == key.length) {
                break;
            }
        }
        byte[][] result = new byte[2][];
        result[0] = new byte[i];
        result[1] = new byte[k-i-3];
        System.arraycopy(message,0,result[0],0,i);
        System.arraycopy(message,i+4,result[1],0,k-i-3);

        return result;
    }
    
}

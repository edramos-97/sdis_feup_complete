package Utilities;

public class ProtocolMessageParser {

    public static ProtocolMessage parseMessage(byte[] receivedMessage,int length){
        byte[][] msgFields = splitParts(receivedMessage,length);
        String[] headerFields = new String(msgFields[0]).split("[ ]+");
        ProtocolMessage.PossibleTypes tempType = verifyType(headerFields[0]);
        ProtocolMessage tempMessage;
        if (tempType==null){
            return null;
        }else {
            tempMessage = new ProtocolMessage(tempType);
        }
        if(!setFields(tempMessage,headerFields)){
            return null;
        }

        try{
            if (tempMessage.hasBody){
                tempMessage.setBody(msgFields[1]);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return tempMessage;
    }

    /**
     * Function to verify if the received data type is a valid one
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
     * Function to set data fields from a string array
     * @param tempMessage - Message to be set
     * @param headerFields - String array with information about a data
     * @return False if an invalid data field is received, true otherwise
     */
    private static boolean setFields(ProtocolMessage tempMessage,String[] headerFields) {
        try {
            tempMessage.setVersion(headerFields[1]);
            tempMessage.setSenderId(headerFields[2]);
            tempMessage.setFileId(headerFields[3]);
            switch (tempMessage.getMsgType()) {
                case PUTLOGCHUNK:
                case PUTCHUNK:
                    tempMessage.setChunkNo(headerFields[4]);
                    int temp = Integer.parseInt(headerFields[5]);
                    if (headerFields[5].length()==1 && temp!=0){
                        tempMessage.setReplicationDeg(headerFields[5].charAt(0));
                    }else if(temp<=0){
                        System.out.println("Assuming replication degree 1");
                        throw new Exception("Invalid data field received: replicationDegree="+temp);
                    }else if (temp>9){
                        System.out.println("Assuming replication degree 9");
                        throw new Exception("Invalid data field received: replicationDegree="+temp);
                    }
                    break;
                case GETCHUNK:
                case STORED:
                case CHUNK:
                case REMOVED:
                    tempMessage.setChunkNo(headerFields[4]);
                    break;
                case DELETECONF:
                case DELETE:
                case BACKEDUP:
                    break;
                default:
                    throw new Exception("Invalid data type received!");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private static byte[][] splitParts(byte[] message,int length){
        byte[] key = "\r\n\r\n".getBytes();
        int i = 0;
        int k = length-1;
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

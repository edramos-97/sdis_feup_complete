package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.*;
import StateRecovery.RecoveryInitiator;
import Utilities.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Receiver extends Thread {

    private DatagramPacket packet;

    Receiver(DatagramPacket packet){
        this.packet = packet;
    }

    @Override
    public void run() {
        //System.out.println("Before:"+new String(Arrays.copyOfRange(packet.getData(),0,100)));

        byte[] packet_data = MessageCipher.groupDecipher(packet.getData());
        
        ProtocolMessage message = ProtocolMessageParser.parseMessage(Dispatcher.authenticate(packet_data, packet.getLength()),packet.getLength() - 32); //32 is hmac size

        //System.out.println("After:"+new String(Arrays.copyOfRange(packet.getData(),0,500)));


        if(message == null ){
            System.out.println("null message");
            return;
        }
        if(message.getSenderId().equals(String.valueOf(Peer.peerID))){
            return;
        }

        int delay = new Random().nextInt(400);
        //System.out.println(message.getMsgType());
        switch (message.getMsgType()) {
            case STORED:
                VolatileDatabase.add_chunk_stored(message.getFileId(), Short.valueOf(message.getChunkNo()), Integer.parseInt(message.getSenderId()));
                break;
            case GETCHUNK:
                if (!FileHandler.hasChunk(message.getFileId(), Short.valueOf(message.getChunkNo()))) {
                    System.out.println("GETCHUNK file is not backed up, ignoring...");
                    return;
                }
                VolatileDatabase.getChunkMemory.add(message.getFileId() + message.getChunkNo());
                Peer.threadPool.schedule(new GetChunkHandle(message, packet), delay, TimeUnit.MILLISECONDS);
                break;
            case DELETE:
                System.out.println("DELETE fileID: \"" + message.getFileId() + "\"");
                Peer.threadPool.submit(new DeleteHandle(message));
                break;
            case DELETECONF:
                System.out.println("DELETE confirming fileID:\"" + message.getFileId() + "\" from peer:\"" + message.getSenderId() + "\"");
                VolatileDatabase.confirmDelete(message.getFileId(), message.getSenderId());
                break;
            case BACKEDUP:
                System.out.println("BACKEDUP fileID:\"" + message.getFileId() + "\" from peer:\"" + message.getSenderId() + "\"");
                Peer.threadPool.submit(new BackedupHandle(message));
                break;
            case REMOVED:
                System.out.println("REMOVED fileID:\"" + message.getFileId() + "\" chunkNo:\""+message.getChunkNo()+"\" from peer:\"" + message.getSenderId() + "\"");
                Peer.threadPool.submit(new DiskReclaimHandle(message.getFileId(), Short.valueOf(message.getChunkNo()), Integer.parseInt(message.getSenderId())));
                break;
            case RECOVERASKMAX:
                String fileId = message.getFileId();
                try {
                    message.setChunkNo("" + FileHandler.getMaxChunkNo(fileId));
                    message.setSenderId(Peer.peerID + "");
                    message.setMsgType(ProtocolMessage.PossibleTypes.RECOVERMAX);
                    Dispatcher.sendControl(message.toCharArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case RECOVERMAX:
                if(!RecoveryInitiator.active){
                    return;
                }
                int messageNumber = Integer.parseInt(message.getChunkNo()) - 1;
                //System.out.println(messageNumber);
                if(messageNumber > RecoveryInitiator.chunkNumber) {
                    RecoveryInitiator.chunkNumber = messageNumber;
                }
                break;
            case GETLOGS:
                Peer.threadPool.submit(new GetLogsHandle());
                break;
            case CHUNKLOG:
                if(!FileHandler.canSave(message.getBody().length)) {
                    System.out.println("Back up space is full! Not saving putChunk");
                    break;
                }
                //VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                VolatileDatabase.add_chunk_putchunk(message.getFileId(), Short.valueOf(message.getChunkNo()), (short)1,message.body.length);
                VolatileDatabase.add_chunk_stored(message.getFileId(), Short.valueOf(message.getChunkNo()), Integer.valueOf(message.getSenderId()));
                FileHandler.saveChunk(message,"backup");
                /*
                try {
                    String body = new String(message.getBody());
                    String[] socketInfo = body.split(":",2);

                    Socket dataSocket = new Socket(socketInfo[0],Integer.valueOf(socketInfo[1]));
                    DataInputStream dataInput = new DataInputStream(dataSocket.getInputStream());
                    message.setBody(new byte[64000]);
                    int readBytes = dataInput.read(message.body);
                    System.out.println("read bytes:"+readBytes);
                    message.setBody(Arrays.copyOfRange(message.body,0,readBytes));
                    FileHandler.saveChunk(message,"backup");
                    dataSocket.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println("Couldn't create socket for chunk reception.");
                }*/
                break;
            case PUTLOGCHUNK:
            case PUTCHUNK:
                VolatileDatabase.removedChunk.remove(message.getFileId()+Short.valueOf(message.getChunkNo()));

                if(VolatileDatabase.backed2fileID.containsKey(message.getFileId())){
                    System.out.println("PUTCHUNK not saving file I backed up");
                    break;
                }
                if(!FileHandler.canSave(message.body.length)) {
                    System.out.println("Back up space is full! Not saving putChunk");
                    break;
                }
                int size_message = message.body.length;
                VolatileDatabase.add_chunk_putchunk(message.getFileId(),Short.valueOf(message.getChunkNo()),message.getReplicationDeg(), size_message);
                Peer.threadPool.schedule(new PutChunkHandle(message),delay, TimeUnit.MILLISECONDS);
                break;
            case CHUNK:
                if(VolatileDatabase.restoreMemory.get(message.getFileId())==null) {
                    System.out.println("restore memory does not contain fileId");
                    VolatileDatabase.getChunkMemory.remove(message.getFileId() + message.getChunkNo());
                }else{
                    if(message.getVersion().equals("1.1")){
                        String body = new String(message.getBody());
                        String[] socketInfo = body.split(":",2);
                        System.out.println("Received address is:" + socketInfo[0]);
                        System.out.println("Received port is:" + socketInfo[1]);
			            int readBytes = 0;
                        int readBytesAux = 0;
                        try {
                            Socket dataSocket = new Socket(socketInfo[0],Integer.valueOf(socketInfo[1]));
                            dataSocket.setSoTimeout(1000);
                            DataInputStream dataInput = new DataInputStream(dataSocket.getInputStream());
                            message.setBody(new byte[64000]);
                            byte[] temp = new byte[64000];
                            while (true){
                                readBytesAux = dataInput.read(temp);
                                System.arraycopy(temp,0,message.body,readBytes,readBytesAux);
                                readBytes = readBytesAux + readBytes;
                                if(readBytes == 64000)
                                break;
                            }
                        } catch (SocketTimeoutException e){
                            message.setBody(Arrays.copyOfRange(message.body,0,readBytes));
                            System.out.println("Read timeout");
                        } catch (IOException e) {
                            //e.printStackTrace();
                            //System.out.println("body="+new String(message.body));
                            System.out.println("Couldn't create socket for chunk reception.");
                        }
                        VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                        FileHandler.saveChunk(message,"restore");
                    }else{
                        VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                        FileHandler.saveChunk(message,"restore");
                    }
                }
                break;
            default:
                System.out.println("Unknown message type received on data channel");
                break;
        }
    }
}

package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.*;
import StateRecovery.RecoveryInitiator;
import Utilities.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
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

        ProtocolMessage message = ProtocolMessageParser.parseMessage(Dispatcher.authenticate(packet.getData(), packet.getLength()),packet.getLength() - 32); //32 is hmac size

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
                System.out.println("REMOVED RECEIVED");
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
                int messageNumber = Integer.parseInt(message.getChunkNo()) - 1;
                System.out.println(messageNumber);
                if(messageNumber > RecoveryInitiator.chunkNumber) {
                    RecoveryInitiator.chunkNumber = messageNumber;
                }
                break;
            case GETLOGS:
                Peer.threadPool.submit(new GetLogsHandle());
                break;
            case CHUNKLOG:
                VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
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
                if(!FileHandler.canSave()) {
                    System.out.println("Back up space is full! Not saving putChunk");
                    break;
                }
                int size_message = message.body.length;
                VolatileDatabase.add_chunk_putchunk(message.getFileId(),Short.valueOf(message.getChunkNo()),message.getReplicationDeg(), size_message);
                Peer.threadPool.schedule(new PutChunkHandle(message),delay, TimeUnit.MILLISECONDS);
                break;
            case CHUNK:
                if(VolatileDatabase.restoreMemory.get(message.getFileId())==null) {
                    VolatileDatabase.getChunkMemory.remove(message.getFileId() + message.getChunkNo());
                }else{
                    if(message.getVersion().equals("1.1")){
                        /*if(RestoreEnhancement.can_save_these.containsKey(message.getFileId()+message.getChunkNo())){
                            message = RestoreEnhancement.can_save_these.remove(message.getFileId()+message.getChunkNo());
                        }*/
                        String body = new String(message.getBody());
                        String[] socketInfo = body.split(":",2);
                        System.out.println("Sent address is:" + socketInfo[0]);
                        System.out.println("Sent port is:" + socketInfo[1]);
                        try {
                            Socket dataSocket = new Socket(socketInfo[0],Integer.valueOf(socketInfo[1]));
                            DataInputStream dataInput = new DataInputStream(dataSocket.getInputStream());
                            message.setBody(new byte[64000]);
                            int readBytes = dataInput.read(message.body);
                            System.out.println("read bytes:"+readBytes);
                            message.setBody(Arrays.copyOfRange(message.body,0,readBytes));
                            dataSocket.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
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

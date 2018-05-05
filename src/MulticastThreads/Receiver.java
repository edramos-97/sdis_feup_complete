package MulticastThreads;

import Executables.Peer;
import InitiatorCommunication.*;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.ProtocolMessageParser;
import Utilities.VolatileDatabase;

import java.net.DatagramPacket;
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

        ProtocolMessage message = ProtocolMessageParser.parseMessage(packet.getData(),packet.getLength());

        //System.out.println("After:"+new String(Arrays.copyOfRange(packet.getData(),0,500)));


        if(message == null ){
            System.out.println("null message");
            return;
        }
        if(message.getSenderId().equals(String.valueOf(Peer.peerID))){
            return;
        }

        int delay = new Random().nextInt(400);
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
            case PUTCHUNK:
                System.out.println("chunkNo: "+message.getChunkNo());
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
                        if(RestoreEnhancement.can_save_these.containsKey(message.getFileId()+message.getChunkNo())){
                            message = RestoreEnhancement.can_save_these.remove(message.getFileId()+message.getChunkNo());
                            VolatileDatabase.restoreMemory.put(message.getFileId(),new Integer[]{Integer.parseInt(message.getChunkNo()),message.getBody().length});
                            FileHandler.saveChunk(message,"restore");
                        }
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

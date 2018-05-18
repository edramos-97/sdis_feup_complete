package InitiatorCommunication;

import Executables.Peer;
import StateRecovery.RecoveryInitiator;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.util.concurrent.TimeUnit;

public class PutChunkHandle extends Thread {

    private ProtocolMessage message;

    public PutChunkHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run() {
        if(message.getVersion().equals("1.1")){
            System.out.println("RUNNING ENHANCED BACKUP");
            //TODO-Enhancement Backup Checking if already have enough replication degree then its meaningless to store any more
            short current_rep_degree = VolatileDatabase.get_rep_degree(message.getFileId(), Short.valueOf(message.getChunkNo()));

            if (current_rep_degree >= message.getReplicationDeg() && !FileHandler.hasChunk(message.getFileId(),Short.valueOf(message.getChunkNo()))){
                Peer.threadPool.schedule(()-> VolatileDatabase.deleteChunkEntry(message.getFileId(), Short.valueOf(message.getChunkNo())),1000, TimeUnit.MILLISECONDS);
                return;
            }
        }
        if(!FileHandler.hasChunk(message.getFileId(),Short.valueOf(message.getChunkNo())))
            FileHandler.saveChunk(this.message,"backup");
        if(message.getMsgType() == ProtocolMessage.PossibleTypes.PUTCHUNK){
            RecoveryInitiator.addStored(message.getFileId(), Integer.valueOf(message.getChunkNo()));
        }
        message.setMsgType(ProtocolMessage.PossibleTypes.STORED);
        try {
            message.setSenderId(String.valueOf(Peer.peerID));
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolatileDatabase.add_chunk_stored(message.getFileId(), Short.valueOf(message.getChunkNo()), Peer.peerID);
        Dispatcher.sendControl(message.toCharArray());

    }
}

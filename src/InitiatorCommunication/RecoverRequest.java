package InitiatorCommunication;

import Executables.Peer;
import StateRecovery.RecoveryInitiator;
import Utilities.VolatileDatabase;

public class RecoverRequest implements Runnable {
    @Override
    public void run() {
        RecoveryInitiator.active = false;
        String fileName = "peer"+ Peer.peerID+"recoveryLog";
        VolatileDatabase.restoreMemory.put(RecoveryInitiator.fileID,new Integer[]{-1,-1});
        if(RecoveryInitiator.chunkNumber > -1) {
            Peer.threadPool.submit(new GetChunkRequest(RecoveryInitiator.fileID,(short)0, fileName, "1.1"));
        }

    }
}

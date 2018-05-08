package InitiatorCommunication;

import Executables.Peer;
import StateRecovery.RecoveryInitiator;
import Utilities.FileHandler;
import java.io.File;
import java.nio.file.Paths;

public class GetLogsHandle implements Runnable {

    @Override
    public void run() {
        File f = Paths.get(FileHandler.savePath + RecoveryInitiator.fileID).toFile();

        if(f == null) {
            return;
        }
        if(!f.isDirectory()) {
            return;
        }
        for(File logChunk : f.listFiles()) {
            Peer.threadPool.submit(new RecoverLog(logChunk));
        }
    }
}

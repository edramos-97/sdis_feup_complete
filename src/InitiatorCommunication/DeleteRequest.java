package InitiatorCommunication;

import StateRecovery.RecoveryInitiator;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;

public class DeleteRequest implements Runnable{

    private File file;
    private String version;

    public DeleteRequest(String path, String version){
        file = new File(path);
        this.version = version;
    }

    @Override
    public void run() {
        if (file.isDirectory()){
            System.out.println("DELETE request path cannot point to a directory, terminating request...");
        }else{
            ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.DELETE);

            message.setFileId(FileHandler.getFileId(file));

            Dispatcher.sendControl(message.toCharArray());

            RecoveryInitiator.addDeleteBackup(file.getName(), file.lastModified());

            if(version.equals("1.1"))
                VolatileDatabase.deleteFile(message.getFileId());

            if(FileHandler.removeFolder(file)){
                System.out.println("DELETE could not delete local copy of the requested file, terminating request...");
            }
        }
    }
}

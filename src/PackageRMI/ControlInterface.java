package PackageRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControlInterface extends Remote {

    //TODO: fill these

    boolean putChunk(String filePath, char replicationDeg) throws RemoteException;

    // restore

    // delete

    // reclaim

    // state


}

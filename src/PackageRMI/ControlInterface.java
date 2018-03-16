package PackageRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControlInterface extends Remote {

    //TODO: fill these

    boolean say_this(String text) throws RemoteException;

    // backup

    // restore

    // delete

    // reclaim

    // state


}

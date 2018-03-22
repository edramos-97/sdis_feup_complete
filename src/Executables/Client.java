package Executables;

import PackageRMI.ControlInterface;
import Utilities.FileHandler;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
//        java Executables.Client <peer_access_point> <sub_protocol> <operand_1> <operand_2>
//        <peer_access_point> depends on the implementation
        //System.out.println("It works");

        if(args.length < 2){
            System.out.println("usage: java Executables.Client <peer_access_point> <sub_protocol> <operand_1> <operand_2>");
            return;
        }

        String[] access_point_info = args[0].split(":");
        String host = access_point_info[0];
        String name = access_point_info[1];

        try {
            Registry registry = LocateRegistry.getRegistry(); // TODO: change this to go with other hosts
            ControlInterface control_rmi_stub = (ControlInterface) registry.lookup(name);

            send_messages(args, control_rmi_stub);

            //boolean response = control_rmi_stub.putChunk(FileHandler.savePath+"1.txt",'1');
            //boolean response2 = control_rmi_stub.putChunk(FileHandler.savePath+"2.txt",'1');
            //boolean response3 = control_rmi_stub.getChunk("51c36c365ca845a0c509c74d88fe6c06ac830bf06832b2a832bd6eee3e310627",(short)0);
            //System.out.println("returned: " + response);
            //System.out.println("returned2: " + response2);
            //System.out.println("returned2: " + response3);
        } catch (Exception e) {
            System.err.println("Executables.Client exception: " + e.toString());
            e.printStackTrace();
        }


    }

    private static void send_messages(String[] arguments, ControlInterface rmi_stub) throws RemoteException {

        String sub_protocol = arguments[1];

        if(sub_protocol.equals("STATE")){
            // Send message to check on state

            rmi_stub.say_this("STATE CALLED");
            System.out.println("This is the state...");
            return;
        }

        if(arguments.length < 3){
            System.out.println("Missing some arguments");
            return;
        }

        if(sub_protocol.equals("RECLAIM")){
            String size_to_reclaim = arguments[2];
            // Send message to reclaim some space

            rmi_stub.say_this("RECLAIM CALLED");
            System.out.println("Reclaimed...");
            return;
        }

        String filepath = System.getProperty("user.dir") + File.separator + arguments[2];
        System.out.println(filepath);
        String fileID = FileHandler.getFileId(filepath);
        System.out.println(fileID);

        if(sub_protocol.equals("DELETE")){
            // Send message to delete the given fileID

            rmi_stub.say_this("DELETE CALLED");
            System.out.println("Deleted...");
            return;
        }

        if(sub_protocol.equals("RESTORE")){
            // Send message to restore a given fileID

            rmi_stub.say_this("RESTORE CALLED");
            System.out.println("Restored..");
            return;
        }

        if(arguments.length < 4){
            System.out.println("Missing some arguments");
            return;
        }

        String replication_degree = arguments[3];
        if(sub_protocol.equals("BACKUP")){
            // Send message to backup a given file

            rmi_stub.say_this("BACKUP CALLED");
            System.out.println("Backed up...");
            return;
        }

        System.out.println("Wrong name for sub_protocol");
    }
}

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

        // localhost:peer10 BACKUP teste.txt 3
        // TODO: change address:name -> to -> //host:port/name in args[0]
        String[] access_and_name = args[0].split("/+");
        String[] host_and_port = access_and_name[1].split(":");

        String host = host_and_port[0];
        String name = access_and_name[2];
        int port = 0;
        if(host_and_port.length > 1)
            port  = Integer.parseInt(host_and_port[1]);

        System.out.print("HOST:");
        System.out.println(host);
        System.out.print("PORT:");
        System.out.println(port);
        System.out.print("NAME:");
        System.out.println(name);

        try {
            Registry registry;
            if(port == 0){
                registry = LocateRegistry.getRegistry(host);
            }
            else{
                registry = LocateRegistry.getRegistry(host, port);
            }
            ControlInterface control_rmi_stub = (ControlInterface) registry.lookup(name);

            send_messages(args, control_rmi_stub);


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
            System.out.println(replication_degree.charAt(0));
            rmi_stub.putChunk(filepath, replication_degree.charAt(0));
            System.out.println("Backed up...");
            return;
        }

        System.out.println("Wrong name for sub_protocol");
    }
}

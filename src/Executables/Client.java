package Executables;

import PackageRMI.ControlInterface;
import Utilities.FileHandler;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
//        java Executables.Client <peer_access_point> <sub_protocol> <operand_1> <operand_2>
//        <peer_access_point> depends on the implementation
        //System.out.println("It works");

        if(args.length < 4){
            System.out.println("usage: java Executables.Client <peer_access_point> <sub_protocol> <operand_1> <operand_2>");
            return;
        }

        String[] access_point_info = args[0].split(":");
        String host = access_point_info[0];
        String name = access_point_info[1];

        try {
            Registry registry = LocateRegistry.getRegistry(); // TODO: change this to go with other hosts
            ControlInterface control_rmi_stub = (ControlInterface) registry.lookup(name);
            boolean response = control_rmi_stub.putChunk(FileHandler.savePath+"1.txt",'1');
            //boolean response2 = control_rmi_stub.putChunk(FileHandler.savePath+"2.txt",'1');
            //boolean response3 = control_rmi_stub.getChunk("51c36c365ca845a0c509c74d88fe6c06ac830bf06832b2a832bd6eee3e310627",(short)0);
            System.out.println("returned: " + response);
            //System.out.println("returned2: " + response2);
            //System.out.println("returned2: " + response3);
        } catch (Exception e) {
            System.err.println("Executables.Client exception: " + e.toString());
            e.printStackTrace();
        }


    }
}

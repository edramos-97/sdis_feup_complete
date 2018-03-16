import PackageRMI.ControlInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
//        java Client <peer_access_point> <sub_protocol> <operand_1> <operand_2>
//        <peer_access_point> depends on the implementation
        //System.out.println("It works");

        if(args.length < 4){
            System.out.println("usage: java Client <peer_access_point> <sub_protocol> <operand_1> <operand_2>");
            return;
        }

        String[] access_point_info = args[0].split(":");
        String host = access_point_info[0];
        String name = access_point_info[1];

        try {
            Registry registry = LocateRegistry.getRegistry(); // TODO: change this to go with other hosts
            ControlInterface control_rmi_stub = (ControlInterface) registry.lookup(name);
            boolean response = control_rmi_stub.say_this("heyyy");
            System.out.println("returned: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }


    }
}

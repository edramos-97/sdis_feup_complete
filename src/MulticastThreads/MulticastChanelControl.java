package MulticastThreads;

import java.io.IOException;
import java.net.InetAddress;

public class MulticastChanelControl extends MulticastChanel {


    public MulticastChanelControl(String mcc_address, String mcc_port, String mcb_address,
                                  String mcb_port, String mcr_address, String mcr_port) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port);

        //joining group
        multicast_control_socket.joinGroup(InetAddress.getByName(multicast_control_address));

    }

    @Override
    public void run() {

        // listen on control

    }
}

package Utilities;

import java.net.MulticastSocket;

public class DispatcherMessage {
    byte[] data;
    String address;
    String port;
    MulticastSocket socket;

    public DispatcherMessage(byte[] data, String address, String port, MulticastSocket socket) {
        this.data = data;
        this.address = address;
        this.port = port;
        this.socket = socket;
    }
}

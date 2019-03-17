package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;

public class Sending {

    private LinkedList<String> IoTUsers = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        MulticastSocket socket = new MulticastSocket();
        byte mex[] = {'h', 'e', 'l', 'l', 'o'};
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        DatagramPacket packet = new DatagramPacket( mex, mex.length, address, 7777 );
        socket.send( packet );
    }
}

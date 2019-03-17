package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {

    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        // Get the address that we are going to connect to.
        InetAddress addr = InetAddress.getByName( INET_ADDR );
        while (true) {
            // Open a new DatagramSocket, which will be used to send the data.
            try (DatagramSocket serverSocket = new DatagramSocket()) {
                String msg = "Still Alive? ";
                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket( msg.getBytes(),
                        msg.getBytes().length, addr, PORT );
                serverSocket.send( msgPacket );

                System.out.println( "Server sent packet with msg: " + msg );
                Thread.sleep( 10000 );


            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
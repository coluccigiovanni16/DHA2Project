import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

public class Server {
    private LinkedList<InetSocketAddress> IotUsers;


    public Server() throws IOException, InterruptedException {
        this.IotUsers = new LinkedList<>();
        byte mex[] = "Someone online?".getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, InetAddress.getByName( "224.0.0.1" ), 7777 );
        MulticastSocket sender = new MulticastSocket();
        sender.send( packetToSend );
        sender.close();

        byte[] mexRCV = new byte[65507];
        DatagramSocket socket1 = new DatagramSocket( 7776 );
        DatagramPacket packetReceived = new DatagramPacket( mexRCV, mexRCV.length );
        socket1.receive( packetReceived );
        String modifiedSentence = new String( packetReceived.getData() );
        System.out.println( modifiedSentence );

        Thread.sleep( 1000 );
        DatagramSocket socket2 = new DatagramSocket();
        DatagramPacket packetToSend1 = new DatagramPacket( "ciao".getBytes(), "ciao".getBytes().length, packetReceived.getAddress(), 7775 );
        socket2.send( packetToSend1);


    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server s = new Server();
    }
}

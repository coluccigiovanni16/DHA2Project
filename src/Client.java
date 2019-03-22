import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Client {

    public Client() throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        socket.joinGroup( address );
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence = new String( packet.getData() );
        System.out.println( modifiedSentence );
        socket.leaveGroup( address );
        socket.close();

        Thread.sleep( 1000 );
        DatagramSocket socket1 = new DatagramSocket();
        DatagramPacket packetToSend = new DatagramPacket( "ciao".getBytes(), "ciao".getBytes().length, packet.getAddress(), 7776 );
        socket1.send( packetToSend );

        byte[] mexRCV = new byte[65507];
        DatagramSocket socket2 = new DatagramSocket( 7775 );
        DatagramPacket packetReceived = new DatagramPacket( mexRCV, mexRCV.length );
        socket2.receive( packetReceived );
        modifiedSentence = new String( packetReceived.getData() );
        System.out.println( modifiedSentence );


        socket1.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Client c = new Client();
    }
}
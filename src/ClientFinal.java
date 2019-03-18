import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ClientFinal {

    public ClientFinal(String i) throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        socket.joinGroup( address );
        while (true) {
            InetAddress serveAddr = receiveFromServer( socket );
            Thread.sleep( 250 );
            sendToServer( "Alive" + i, serveAddr );
        }
    }

    private InetAddress receiveFromServer(MulticastSocket socket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
        return packet.getAddress();
    }


    private static void sendToServer(String message, InetAddress serverAddress) throws IOException, InterruptedException {
        byte[] mex = message.getBytes();
        new DatagramSocket().send( new DatagramPacket( mex, mex.length, serverAddress, 7776 ) );
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ClientFinal c1 = new ClientFinal( "0" );
    }
}

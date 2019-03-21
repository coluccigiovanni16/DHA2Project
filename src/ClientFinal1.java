import java.io.IOException;
import java.net.*;


public class ClientFinal1 {

    public ClientFinal1(String i) throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        socket.joinGroup( address );
        while (true) {
            InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( socket );
            Thread.sleep( 500 );
            sendToServer( "Alive" + i, serveAddr );
        }
    }

    private SocketAddress receiveFromServer(MulticastSocket socket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
        return packet.getSocketAddress();
    }


    private static void sendToServer(String message, InetSocketAddress serverAddress) throws IOException, InterruptedException {
        byte[] mex = message.getBytes();
        new DatagramSocket().send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ClientFinal1 c1 = new ClientFinal1( "1" );
    }
}

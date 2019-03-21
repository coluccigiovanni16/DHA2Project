import java.io.IOException;
import java.net.*;


public class ClientFinal1 {

    public ClientFinal1() throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        socket.joinGroup( address );
        InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( socket );
        socket.leaveGroup( address );
        socket.close();
        Thread.sleep( 100 );
        sendToServer( "New", serveAddr );
        while (true) {
            receiveFromServerLoop();
            sendToServer( "Alive", serveAddr );
        }
    }

    private void receiveFromServerLoop() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
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


    private static void sendToServer(String message, InetSocketAddress serverAddress) throws IOException {
        byte[] mex = message.getBytes();
        DatagramSocket socket = new DatagramSocket();
        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
        socket.close();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ClientFinal1 c1 = new ClientFinal1();
    }
}

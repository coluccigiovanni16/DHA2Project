import java.io.IOException;
import java.net.*;


public class ClientFinal {

    public ClientFinal() throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        socket.joinGroup( address );
        InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( socket );
        socket.leaveGroup( address );
        socket.close();
        Thread.sleep( 100 );
        sendToServer( "New", serveAddr );
        while (true) {
            InetSocketAddress add=receiveFromServerLoop(serveAddr);
            sendToServer( "Alive", add );
        }
    }

    private InetSocketAddress receiveFromServerLoop(SocketAddress s) throws IOException {
        DatagramSocket socket = new DatagramSocket(7776);
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
        socket.close();
        return (InetSocketAddress) packet.getSocketAddress();
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
        ClientFinal c1 = new ClientFinal();
    }
}

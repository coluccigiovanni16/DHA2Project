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
            InetSocketAddress add=receiveFromServerLoop(serveAddr.getAddress());
            sendToServer( "Alive", add );
        }
    }

    private InetSocketAddress receiveFromServerLoop(InetAddress servAddr) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setReuseAddress(true);
        System.out.println(socket.getReuseAddress());
        socket.bind(new InetSocketAddress(servAddr,7776));
        System.out.println(socket.getReuseAddress());
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
        ClientFinal1 c1 = new ClientFinal1();
    }
}

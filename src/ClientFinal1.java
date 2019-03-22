import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class ClientFinal1 {

    public ClientFinal1() throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        selectNetInterface( socket );
        socket.joinGroup( address );
        InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( socket );
        socket.leaveGroup( address );
        socket.close();
        Thread.sleep( 1000 );
        //apro socket client
        DatagramSocket socket1 = new DatagramSocket();
        //invio risposta server senza chiudere socket
        sendToServerUnicast( "New", serveAddr, socket1 );
        InetSocketAddress add = new InetSocketAddress( serveAddr.getAddress(), 7776 );

        while (true) {
            receiveFromServerLoop( socket1 );
            sendToServerUnicast( "Alive", add, socket1 );
        }
    }

    private void sendToServerUnicast(String message, InetSocketAddress serverAddress, DatagramSocket socket) throws IOException, InterruptedException {
        byte[] mex = message.getBytes();
        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
        Thread.sleep( 1000 );
    }

    private void receiveFromServerLoop(DatagramSocket socket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
        System.out.println( packet.getSocketAddress() );
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
        DatagramSocket socket = new DatagramSocket();

        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
        Thread.sleep( 1000 );
        socket.close();
    }

    private void selectNetInterface(MulticastSocket socket) throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                if (i.isSiteLocalAddress() && !i.isAnyLocalAddress() && !i.isLinkLocalAddress() && !i.isLoopbackAddress() && !i.isMulticastAddress()) {
                    socket.setNetworkInterface( NetworkInterface.getByName( n.getName() ) );
                }
            }
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new ClientFinal1();
    }
}

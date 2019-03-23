import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class ClientFinal1 {

    private int myPort;

    public ClientFinal1() throws IOException, InterruptedException {

        while (true) {
            MulticastSocket socket = new MulticastSocket( 7777 );
            InetAddress address = InetAddress.getByName( "224.0.0.1" );
            selectNetInterface( socket );
            socket.joinGroup( address );
            InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( socket );
            socket.leaveGroup( address );
            socket.close();
            Thread.sleep( 1000 );
            //apro socket client
            DatagramSocket socket1 = new DatagramSocket(this.myPort);
            socket1.setReuseAddress( true );
            //invio risposta server senza chiudere socket
            sendToServerUnicast( "New", serveAddr, socket1 );
            InetSocketAddress add = new InetSocketAddress( serveAddr.getAddress(), 7776 );
            boolean serverSendedWell = true;
            while (serverSendedWell) {
                serverSendedWell = receiveFromServerLoop( socket1 );
                if (serverSendedWell) {
                    sendToServerUnicast( "Alive", add, socket1 );
                    this.myPort = socket1.getLocalPort();
                    System.out.println( this.myPort );
                }
            }
            socket1.close();
        }
    }

    private void sendToServerUnicast(String message, InetSocketAddress serverAddress, DatagramSocket socket) throws IOException, InterruptedException {
        byte[] mex = message.getBytes();
        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
    }

    private boolean receiveFromServerLoop(DatagramSocket socket) throws IOException {
        try {
            byte[] mex = new byte[65507];
            DatagramPacket packet = new DatagramPacket( mex, mex.length );
            socket.setSoTimeout( 60000 );
            socket.receive( packet );
            String modifiedSentence =
                    new String( packet.getData() );
            System.out.println( modifiedSentence );
            System.out.println( packet.getSocketAddress() );
        } catch (SocketTimeoutException timeout) {
            return false;
        }
        return true;
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



}

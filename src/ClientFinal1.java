import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class ClientFinal1 {
    private static int localPort;

    public ClientFinal1() throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        selectNetInterface(socket);
        socket.joinGroup( address );
        InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( socket );
        socket.leaveGroup( address );
        socket.close();
        Thread.sleep( 100 );

        //apro socket client
        DatagramSocket socket1 = new DatagramSocket();
        //invio risposta server senza chiudere socket
        sendToServerUnicast( "New", serveAddr,socket1 );
        InetSocketAddress add = new InetSocketAddress(serveAddr .getAddress(),7776);

        while (true) {
            //DatagramSocket socket1 = new DatagramSocket( localPort );
            receiveFromServerLoop( socket1 );
            sendToServerUnicast( "Alive", add,socket1 );
           // sendToServer( "Alive", add );
        }
    }

    private void sendToServerUnicast(String message, InetSocketAddress serverAddress,DatagramSocket socket) throws IOException, InterruptedException {
        byte[] mex = message.getBytes();
        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
        Thread.sleep( 1000 );
        localPort = socket.getLocalPort();
    }

    private void receiveFromServerLoop(DatagramSocket socket) throws IOException {
//        socket.setReuseAddress(true);
//        System.out.println(socket.getReuseAddress());
//        socket.bind(new InetSocketAddress(servAddr,7776));
//        System.out.println(socket.getReuseAddress());


        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
        System.out.println( (InetSocketAddress) packet.getSocketAddress() );
        //return (InetSocketAddress) packet.getSocketAddress();
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
        localPort = socket.getLocalPort();
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
                    socket.setNetworkInterface(NetworkInterface.getByName(n.getName()));
                }
            }
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ClientFinal1 c1 = new ClientFinal1();
    }
}

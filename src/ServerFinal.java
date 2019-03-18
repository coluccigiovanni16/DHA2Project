import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class ServerFinal {

    public ServerFinal() throws IOException {
        sendMulticast( "Someone online?" );
        receiving();
        while (true) {
            sendMulticast( "Still alive?" );
            receiving();
        }
    }

    private void receiving() throws IOException {
        DatagramSocket socket = new DatagramSocket( 7776 );
        byte[] mex = new byte[64000];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.setSoTimeout( 5000 );
        try {
            socket.receive( packet );
            String modifiedSentence = new String( packet.getData() );
            System.out.println( modifiedSentence );
        } catch (SocketTimeoutException timeOut) {
            System.out.println( "timeout" );
        }
        socket.close();

    }


    private static void sendMulticast(String message) throws IOException {
        byte mex[] = message.getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, listAllBroadcastAddresses().get( 0 ), 7777 );
        DatagramSocket sender = new DatagramSocket();
        sender.send( packetToSend );
    }

    public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map( a -> a.getBroadcast() )
                    .filter( Objects::nonNull )
                    .forEach( broadcastList::add );
        }
        return broadcastList;
    }

    public static void main(String[] args) throws IOException {
        ServerFinal server = new ServerFinal();
    }
}



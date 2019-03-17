import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class ClientFinal {

    public static void main(String[] args) throws IOException, InterruptedException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        DatagramSocket socket1 = new DatagramSocket();
        InetAddress address = InetAddress.getByName( "224.0.0.1" );
        socket.joinGroup( address );
        while (true) {

            byte[] mex = new byte[64000];
            DatagramPacket packet = new DatagramPacket( mex, mex.length );
            socket.receive( packet );
            System.out.println( "Ricevuti: " + packet.getLength() + "bytes" );
            System.out.println( packet.getData() );
            String modifiedSentence =
                    new String( packet.getData() );
            System.out.println( modifiedSentence );
            mex = "Alive".getBytes();
            System.out.println( listAllBroadcastAddresses().get( 0 ) );
            packet = new DatagramPacket( mex, mex.length, listAllBroadcastAddresses().get( 0 ), 7776 );
            Thread.sleep( 1000 );
            socket1.send( packet );

        }
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
}

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServerFinal {
    HashMap<InetSocketAddress, Integer> IotUsers;

    public ServerFinal() throws IOException {
        this.IotUsers = new HashMap<>();
        while (true) {
            discoveryClient();
            long endTime = System.currentTimeMillis() + 10000;
            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
                for (SocketAddress s : IotUsers.keySet()) {
                    endTime = System.currentTimeMillis() + 500;
                    while (System.currentTimeMillis() < endTime) {
                        DatagramSocket loopSocket = sendUnicastLoop( "Still alive?", (InetSocketAddress) s );
                        receivingLoop( loopSocket );
                    }
                    if (IotUsers.get( s ) == 0) {
                        IotUsers.remove( s );
                    }
                    System.out.println( this.IotUsers );
                    System.out.println( "------------------" );

                }
            }
        }
    }

    private void discoveryClient() throws IOException {
        MulticastSocket discoverySocket = sendMulticastInit( "Someone online?" );
        long endTime = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < endTime) {
            receivingInit( discoverySocket );
        }
        discoverySocket.close();
        System.out.println( "------------------" );
    }


    private void receivingLoop(DatagramSocket socket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.setSoTimeout( 100 );
        try {
            socket.receive( packet );
            System.out.println( new String( packet.getData() ) );
            System.out.println( packet.getSocketAddress() );
            this.IotUsers.put( (InetSocketAddress) packet.getSocketAddress(), this.IotUsers.get( packet.getSocketAddress() ) + 1 );
        } catch (SocketTimeoutException timeOut) {
            System.out.println( "timeout" );
        }
        socket.close();
    }

    private void receivingInit(MulticastSocket discoverySocket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        discoverySocket.setSoTimeout( 500 );
        try {
            discoverySocket.receive( packet );
            System.out.println( new String( packet.getData() ) );
            System.out.println( packet.getSocketAddress() );
            this.IotUsers.put( (InetSocketAddress) packet.getSocketAddress(), 0 );
        } catch (SocketTimeoutException timeOut) {
            System.out.println( "timeout" );
        }

    }

    private DatagramSocket sendUnicastLoop(String s, InetSocketAddress socketIot) throws IOException {
        byte mex[] = s.getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, socketIot.getAddress(), socketIot.getPort() );
        DatagramSocket sender = new DatagramSocket();
        sender.send( packetToSend );
        return sender;

    }

    private static MulticastSocket sendMulticastInit(String message) throws IOException {
        byte mex[] = message.getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, InetAddress.getByName( "224.0.0.1" ), 7777 );
        MulticastSocket sender = new MulticastSocket();
        sender.send( packetToSend );
        return sender;
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



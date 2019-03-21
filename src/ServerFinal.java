import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServerFinal {
    HashMap<InetSocketAddress, Integer> IotUsers;

    public ServerFinal() throws IOException, InterruptedException {
        this.IotUsers = new HashMap<>();
        while (true) {
            discoveryClient();
            Thread.sleep( 5000 );
            long endTime = System.currentTimeMillis() + 30000;
            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
                unicast();
            }
        }
    }

    private void unicast() throws IOException {
        for (InetSocketAddress s : this.IotUsers.keySet()) {
            boolean vivo = false;
            byte mexSend[] = "Still Alive".getBytes();
            DatagramPacket packetToSend = new DatagramPacket( mexSend, mexSend.length, s.getAddress(), s.getPort() );
            DatagramSocket socket = new DatagramSocket();
//            scegliere il numero di pacchetti da inviare
            for (int i = 0; i < 5; i++) {
                socket.send( packetToSend );
                byte[] mexRecv = new byte[65507];
                DatagramPacket packetReceived = new DatagramPacket( mexRecv, mexRecv.length );
                try {
                    socket.setSoTimeout( 1000 );
                    socket.receive( packetReceived );
                    vivo = true;
                    break;
                } catch (SocketTimeoutException timeout) {
                    //packetcount--;
                }
            }
            if (!vivo) {
                this.IotUsers.remove( s );
            }
            socket.close();
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


    private boolean receivingLoop(DatagramSocket socket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        try {
            socket.setSoTimeout( 500 );
            socket.receive( packet );
            System.out.println( new String( packet.getData() ) );
            System.out.println( packet.getSocketAddress() );
            this.IotUsers.put( (InetSocketAddress) packet.getSocketAddress(), 1 );
        } catch (SocketTimeoutException timeOut) {
            System.out.println( "timeout" );
            return false;
        }
        socket.close();
        return true;
    }

    private void receivingInit(MulticastSocket discoverySocket) throws IOException {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        discoverySocket.setSoTimeout( 6000 );
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

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerFinal server = new ServerFinal();
    }
}



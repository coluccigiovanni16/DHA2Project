import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServerFinal {
    LinkedList<InetSocketAddress> IotUsers;

    public ServerFinal() throws IOException, InterruptedException {
        this.IotUsers = new LinkedList<>();
        DatagramSocket socket=new DatagramSocket();
        socket.setReuseAddress(true);
        socket.close();
        while (true) {
            discoveryClient();
            Thread.sleep( 5000 );
            System.out.println(IotUsers.toString());
            long endTime = System.currentTimeMillis() + 30000;
            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
                unicast();
                Thread.sleep(5000);
            }
        }
    }

    private void unicast() throws IOException {
        for (int i=0;i< this.IotUsers.size();i++) {
            System.out.println(i);
            InetSocketAddress s=this.IotUsers.get(i);
            boolean vivo = false;
            byte[] mexSend = "Still Alive?".getBytes();
            DatagramPacket packetToSend = new DatagramPacket( mexSend, mexSend.length, s.getAddress(), 7776);
            DatagramSocket socket = new DatagramSocket();
            socket.setReuseAddress(true);
            //System.out.println(socket.getReuseAddress());
//            scegliere il numero di pacchetti da inviare
            for (int j = 0; j < 5; j++) {
                System.out.println(j);
                socket.send( packetToSend );
                byte[] mexRecv = new byte[65507];
                DatagramPacket packetReceived = new DatagramPacket( mexRecv, mexRecv.length );
                try {
                    socket.setSoTimeout( 1000 );
                    socket.receive( packetReceived );
                    System.out.println(new String(packetReceived.getData()));
                    vivo = true;
                    //ricevo una risposta
                    break;
                } catch (SocketTimeoutException timeout) {
                    //pacchetto perso
                    System.out.println("perso");

                }
            }
            if (!vivo) {
                this.IotUsers.remove( s );
                i--;
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
            this.IotUsers.add( (InetSocketAddress) packet.getSocketAddress());
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
            this.IotUsers.add( (InetSocketAddress) packet.getSocketAddress() );
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


    public static void main(String[] args) throws IOException, InterruptedException {
        ServerFinal server = new ServerFinal();
    }
}



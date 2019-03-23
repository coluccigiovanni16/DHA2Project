import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

public class ServerFinal {
    private LinkedList<InetSocketAddress> IotUsers;
    private DatagramSocket socketUni;

    public ServerFinal() throws IOException, InterruptedException {
        this.IotUsers = new LinkedList<>();
        while (true) {
            discoveryClient();
            Thread.sleep( 5000 );
            System.out.println( IotUsers.toString() );
//            long endTime = System.currentTimeMillis() + 10000 * IotUsers.size();
            //apro socket unica del server 7776
            this.socketUni = new DatagramSocket( 7776 );

//            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
            unicast();
//                Thread.sleep( 1000 );
//            }
            this.socketUni.close();
            Thread.sleep( 5000 );
        }
    }

    private void unicast() throws IOException {
        for (int i = 0; i < this.IotUsers.size(); i++) {
            InetSocketAddress s = this.IotUsers.get( i );
            boolean vivo = false;
            byte[] mexSend = "Still Alive?".getBytes();
            DatagramPacket packetToSend = new DatagramPacket( mexSend, mexSend.length, s.getAddress(), s.getPort() );
//            scegliere il numero di pacchetti massimo da inviare
            for (int j = 0; j < 5; j++) {
                this.socketUni.send( packetToSend );
                byte[] mexRecv = new byte[65507];
                DatagramPacket packetReceived = new DatagramPacket( mexRecv, mexRecv.length );
                try {
                    this.socketUni.setSoTimeout( 100 );
                    this.socketUni.receive( packetReceived );
                    System.out.println( new String( packetReceived.getData() ) );
                    vivo = true;
                    //ricevo una risposta
                    break;
                } catch (SocketTimeoutException timeout) {
                    //pacchetto perso
                    System.out.println( "perso" );
                }
            }
            if (!vivo) {
                this.IotUsers.remove( s );
                i--;
            }
        }
    }

    private void discoveryClient() throws IOException {
        String message = "Someone online?";
        byte mex[] = message.getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, InetAddress.getByName( "224.0.0.1" ), 7777 );
        MulticastSocket multicastSocket = new MulticastSocket();
        multicastSocket.setTimeToLive( 32 );
        multicastSocket.send( packetToSend );
        long endTime = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < endTime) {
            mex = new byte[65507];
            DatagramPacket packet = new DatagramPacket( mex, mex.length );
            multicastSocket.setSoTimeout( 6000 );
            try {
                multicastSocket.receive( packet );
                System.out.println( new String( packet.getData() ) );
                System.out.println( packet.getSocketAddress() );
                this.IotUsers.add( (InetSocketAddress) packet.getSocketAddress() );
            } catch (SocketTimeoutException timeOut) {
                System.out.println( "timeout" );
            }
        }
        multicastSocket.close();
        System.out.println( "------------------" );
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new ServerFinal();
    }
}



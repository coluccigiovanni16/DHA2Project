import java.io.IOException;
import java.net.*;


public class ClientFinal1 {

    private final int serverMulticastPORT = 7777;
    private final InetAddress serverMulticastAddress = InetAddress.getByName( "224.0.0.1" );


    public ClientFinal1() throws IOException, InterruptedException {

        while (true) {
            MulticastSocket multiSocket = new MulticastSocket( serverMulticastPORT );
            multiSocket.joinGroup( serverMulticastAddress );
            InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( multiSocket );
            multiSocket.leaveGroup( serverMulticastAddress );
            multiSocket.close();
//            Attento 1 di secondo in quanto il server deve mettersi in scolto dopo
//            aver mandato un messaggio in multicast.
            Thread.sleep( 1000 );
            //apro socket client
            DatagramSocket unicastSocket = new DatagramSocket();
            unicastSocket.setReuseAddress( true );
            //invio risposta server senza chiudere socket
            sendToServerUnicast( "New", serveAddr, unicastSocket );
            InetSocketAddress add = new InetSocketAddress( serveAddr.getAddress(), 7776 );
            boolean serverSendedWell = true;
//          dopo aver mandato un messaggio di registrazione al server si mette in modalità watchdog
//          in attesa di messaggi di controllo di stato(online nonOnline)
            while (serverSendedWell) {
                serverSendedWell = receiveFromServerLoop( unicastSocket );
                if (serverSendedWell) {
                    sendToServerUnicast( "Alive", add, unicastSocket );
                }
            }
//          in caso di errori (server off o altro) il client si resetta, ripendendo il ciclo
//          dalla fase di 'log-in'
            unicastSocket.close();
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



}

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class ClientFinal1 {

    private final int serverMulticastPORT = 7777;
    private final InetAddress serverMulticastAddress = InetAddress.getByName( "224.0.0.1" );
    private final static int TIMETORESTART = 20000;


    public ClientFinal1() throws IOException, InterruptedException {

        while (true) {
            MulticastSocket multiSocket = new MulticastSocket( serverMulticastPORT );
            selectNetInterface( multiSocket );
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

    private void sendToServerUnicast(String message, InetSocketAddress serverAddress, DatagramSocket socket) throws IOException {
        byte[] mex = message.getBytes();
        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
    }

    private boolean receiveFromServerLoop(DatagramSocket socket) {
        DatagramPacket packet = receive( socket );
        if (packet != null) {
            String modifiedSentence =
                    new String( packet.getData() );
            System.out.println( modifiedSentence );
            System.out.println( packet.getSocketAddress() );
            return true;
        } else {
            return false;
        }
    }

    private DatagramPacket receive(DatagramSocket socket) {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        try {
            if (!(socket instanceof MulticastSocket)) {
                System.out.println( "unicast" );
                socket.setSoTimeout(TIMETORESTART);
            }
            socket.receive( packet );
            String modifiedSentence =
                    new String( packet.getData() );
            System.out.println( modifiedSentence );
            System.out.println( packet.getSocketAddress() );
        } catch (SocketTimeoutException timeout) {
            return null;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }

    private SocketAddress receiveFromServer(MulticastSocket socket) {
        DatagramPacket packet = receive( socket );
        return packet.getSocketAddress();
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
        ClientFinal1 c = new ClientFinal1();
    }

}

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class Client {

    private final static int TIMETORESTART = 20000; //tempo di attesa del messaggio dal server in unicast
    private final int serverMulticastPORT = 7777;
    private final InetAddress serverMulticastAddress = InetAddress.getByName( "224.0.0.1" );
    private final int serverUnicastPORT = 7776;
    private JFrame frame;
    private JPanel rootPanel;
    private JLabel stateOfConn;


    /**
     * @throws IOException
     * @throws InterruptedException
     */
    public Client() throws IOException, InterruptedException {
        setGuivisible();
        //ciclo vita del client
        while (true) {
            MulticastSocket multiSocket = new MulticastSocket( serverMulticastPORT );
            stateOfConn.setText( "IN ATTESA DI RICEZIONE DEL MESSAGGIO MULTICAST DAL SERVER" );
            selectNetInterface( multiSocket );
            multiSocket.joinGroup( serverMulticastAddress );
            InetSocketAddress serveAddr = (InetSocketAddress) receiveFromServer( multiSocket );
            multiSocket.leaveGroup( serverMulticastAddress );
            multiSocket.close();
//            Attento 1 di secondo in quanto il server deve mettersi in ascolto dopo
//            aver mandato un messaggio in multicast.
            Thread.sleep( 1000 );
            //apro socket client
            DatagramSocket unicastSocket = new DatagramSocket();
            unicastSocket.setReuseAddress( true );
            //invio risposta server senza chiudere socket
            stateOfConn.setText( "INVIO MESSAGGIO DI RISPOSTA MULTICAST AL SERVER" );
            sendToServerUnicast( "New", serveAddr, unicastSocket );
            InetSocketAddress add = new InetSocketAddress( serveAddr.getAddress(), serverUnicastPORT );
            boolean serverSendedWell = true;
//          dopo aver mandato un messaggio di registrazione al server si mette in modalità watchdog
//          in attesa di messaggi di controllo di stato(online nonOnline)
            while (serverSendedWell) {
                stateOfConn.setText( "ONLINE" );
                serverSendedWell = receiveFromServerLoop( unicastSocket );
                if (serverSendedWell) {
                    stateOfConn.setText( "INVIO MESSAGGIO DI STATO AL SERVER" );
                    sendToServerUnicast( "Alive", add, unicastSocket );
                }
            }
//          in caso di errori (server off o altro) il client si resetta, ripendendo il ciclo
//          dalla fase di 'log-in'
            unicastSocket.close();
        }
    }

    /**
     *
     */
    private void setGuivisible() {
        this.frame = new JFrame( "CLIENT" );
        this.frame.setContentPane( rootPanel );
        this.frame.pack();
        this.frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
//        this.frame.addWindowListener( new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                System.exit( 0 );
//            }
//        } );
        this.frame.setVisible( true );
    }


    /**
     * @param message messaggio da inserire nel pacchetto
     * @param serverAddress indirizzio a cui inviare il messaggio (IP/PORT)
     * @param socket socket da usare per la comunicazione
     * @throws IOException
     */
    private void sendToServerUnicast(String message, InetSocketAddress serverAddress, DatagramSocket socket) throws IOException {
        byte[] mex = message.getBytes();
        socket.send( new DatagramPacket( mex, mex.length, serverAddress.getAddress(), serverAddress.getPort() ) );
    }

    /**
     * @param socket socket sulla quale restare in ascolto
     * @return boolean true se ricevo un pacchetto false se supero il timeout della socket
     */
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

    /**
     * @param socket socket sulla quale restare in ascolto
     * @return packet ricevuto sulla socket,null in caso di timeout
     */
    private DatagramPacket receive(DatagramSocket socket) {
        byte[] mex = new byte[65507];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        try {
            // controllo sul tipo di socket per settare il timeout solo in caso di comunicazione unicast
            // in caso di multicast è stato preferito la receive bloccante essendo la discovery effettuata dal server
            if (!(socket instanceof MulticastSocket)) {
                System.out.println( "unicast" );
                socket.setSoTimeout( TIMETORESTART );
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
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return packet;
    }

    /**
     * @param socket socket sulla quale restare in ascolto
     * @return SocketAddress mittente del pacchetto
     */
    private SocketAddress receiveFromServer(MulticastSocket socket) {
        DatagramPacket packet = receive( socket );
        return packet.getSocketAddress();
    }

    /**
     * @param socket
     * @throws SocketException
     */
    // consente di settare l'intefaccia di rete per rendere il programma utilizzabile anche da Mac
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

    // main test per controllare il comportamento della classe
    public static void main(String[] args) throws IOException, InterruptedException {
        new Client();
    }
}

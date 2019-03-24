import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

public class Server {
    private final static int DISCOVERYTIME = 10000; // intervallo di tempo tra una discovery e la successiva
    private final static int WAITMULTICAST = 5000; // tempo di attesa di risposta sulla socket multicast
    private final static int WAITRESPONSE = 100; // attesa di risposta nella comunicazione unicast prima di considerare il pacchettp perso
    private final static int NUMBEROFPACKET = 5; // numero di pacchetti da inviare basato sul LOSS RATE della rete
    private final static int TTL = 32; // time to live del pacchetto in multicast
    private final int serverMulticastPORT = 7777;
    private final InetAddress serverMulticastAddress = InetAddress.getByName( "224.0.0.1" );
    private final int serverUnicastPORT = 7776;
    private JTextArea listIotUser;
    private JButton shoutDownButton;
    private JPanel rootPanel;
    private JPanel statePanel;
    private JLabel stateServer;
    private LinkedList<InetSocketAddress> IotUsers;
    private DatagramSocket socketUni;
    private JFrame frame;


    /**
     * @throws IOException
     * @throws InterruptedException
     */
    public Server() throws IOException, InterruptedException {
        this.IotUsers = new LinkedList<>();
        setGuivisible();
        //ciclo vita del server
        while (true) {
            // discovery dei client
            discoveryClient();
            aggiornaListaIot();
//          attendi 1 secondi prima di iniziare il ciclo di controllo di stato
            Thread.sleep( 1000 );
            System.out.println( IotUsers.toString() );
            long endTime = System.currentTimeMillis() + DISCOVERYTIME;// timeout per rieseguire la discovery
            //apro socket unica del server 7776
            this.socketUni = new DatagramSocket( serverUnicastPORT );
            // ciclo comunicazioni unicat
            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
                unicast();
                aggiornaListaIot();
                //          attendi 1 secondi prima di iniziare il ciclo di controllo di stato
                Thread.sleep( 1000 );
            }
            this.socketUni.close();
            //          attendi 1 secondi prima di iniziare il ciclo di controllo di stato
            Thread.sleep( 1000 );
        }
    }


    /**
     * aggiornamento interfaccia server con la lista degli utenti
     */
    private void aggiornaListaIot() {
        listIotUser.setText( "Utenti : " + IotUsers.size() + "\n\n" );
        for (int i = 0; i < IotUsers.size(); i++) {
            listIotUser.append( "Iot device #" + i + 1 + "\n" );
            listIotUser.append( IotUsers.get( i ).toString() + "\n\n" );
        }
    }

    /**
     *
     */
    private void setGuivisible() {
        this.frame = new JFrame( "SERVER" );
        this.frame.setContentPane( rootPanel );
        listIotUser.setEditable( false );
        this.frame.pack();
        this.frame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        this.frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit( 0 );
            }
        } );
        this.shoutDownButton.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit( 0 );
            }
        } );
        this.frame.setVisible( true );
    }

    /**
     * comunicazione unicast
     * @throws IOException
     */
    private void unicast() throws IOException {
        stateServer.setText( "UNICAST" );
        statePanel.setBackground( Color.CYAN );
        // iterazione su tutti i device IoT presenti nella lista
        for (int i = 0; i < this.IotUsers.size(); i++) {
            InetSocketAddress s = this.IotUsers.get( i );
            boolean vivo = false; // bool stato client settato a morto
            byte[] mexSend = "Still Alive?".getBytes();
            DatagramPacket packetToSend = new DatagramPacket( mexSend, mexSend.length, s.getAddress(), s.getPort() );
//          invio di messaggi multiplo per evitare di disconettere un client in caso di pacchetto perso
            for (int j = 0; j < NUMBEROFPACKET; j++) {
                this.socketUni.send( packetToSend );
                byte[] mexRecv = new byte[65507];
                DatagramPacket packetReceived = new DatagramPacket( mexRecv, mexRecv.length );
                try {
                    this.socketUni.setSoTimeout( WAITRESPONSE );
                    this.socketUni.receive( packetReceived );
                    System.out.println( new String( packetReceived.getData() ) );
                    vivo = true; // client vivo
                    //ricevo una risposta, stoppo il ciclo, client vivo
                    break;
                } catch (SocketTimeoutException timeout) {
                    //pacchetto perso
                    System.out.println( "perso" );
                }
            }
            // controllo sullo stato del client
            if (!vivo) {
                //rimozione client
                this.IotUsers.remove( s );
                i--; // aggioramento posizione considerando client eliminato
            }
        }
    }

    /**
     * comunicazione multicast
     * @throws IOException
     */
    private void discoveryClient() throws IOException {
        stateServer.setText( "MULTICAST" );
        statePanel.setBackground( Color.GREEN );
        String message = "Someone online?";
        byte mex[] = message.getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, serverMulticastAddress, serverMulticastPORT );
        MulticastSocket multiSocket = new MulticastSocket();
        multiSocket.setTimeToLive( TTL );
        multiSocket.send( packetToSend );
        long endTime = System.currentTimeMillis() + WAITMULTICAST; // timeout attesa risposta multicast
        // ciclo per consentire di accreditare più client in una singola discovery
        while (System.currentTimeMillis() < endTime) {
            mex = new byte[65507];
            DatagramPacket packet = new DatagramPacket( mex, mex.length );
            // receive timeout settato per permettere di uscire in caso di nessuna risposta
            multiSocket.setSoTimeout( WAITMULTICAST + 1 );
            try {
                multiSocket.receive( packet );
                System.out.println( new String( packet.getData() ) );
                System.out.println( packet.getSocketAddress() );
                // aggiornamento lista device IoT
                this.IotUsers.add( (InetSocketAddress) packet.getSocketAddress() );
            } catch (SocketTimeoutException timeOut) {
                System.out.println( "timeout" );
            }
        }
        multiSocket.close();
        System.out.println( "------------------" );
    }

    // main di test per controllare il comportamento della classe
    public static void main(String[] args) throws IOException, InterruptedException {
        new Server();

    }
}




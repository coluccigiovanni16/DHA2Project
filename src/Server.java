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
    private final static int WAITMULTICAST = 5000; // tempo di attesa di risposta della socket multicast
    private final static int WAITRESPONSE = 500; // attesa di risposta nella comunicazione unicast prima di considerare il pacchettp perso
    private final static int NUMBEROFPACKET = 5; // numero di pacchetti da inviare basato sul LOSS RATE della rete
    private final static int TTL = 32; // time to live del pacchetto in multicast
    private final int serverMulticastPORT = 7777;
    private final InetAddress serverMulticastAddress = InetAddress.getByName( "224.0.0.1" );
    private final int serverUnicastPORT = 7776;
    private JTextArea listIotUser;
    private JButton shoutDownButton;
    private JPanel rootPanel;
    private JPanel statePanel;
    private JLabel stateLabel;
    private JLabel stateServer;
    private LinkedList<InetSocketAddress> IotUsers;
    private DatagramSocket socketUni;
    private JFrame frame;


    /**
     * @throws IOException
     * @throws InterruptedException
     */
    public Server() throws InterruptedException, IOException {
        this.IotUsers = new LinkedList<>();
        setGuivisible();
        //ciclo vita del server
        while (true) {
            // discovery dei client
            try {
                discoveryClient();
//           in caso di errore si apre un pop-up di errore che porta alla chiusura del programma
            } catch (IOException e) {
                int input = JOptionPane.showOptionDialog( null, "Problema di connessione del server, premi OK per chiudere!", "ERROR", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null );
                if (input == JOptionPane.OK_OPTION) {
                    System.exit( -1 );
                }

            }
            aggiornaListaIot();
//          attendi 1 secondi prima di iniziare il ciclo di controllo di stato
            Thread.sleep( 1000 );
            System.out.println( IotUsers.toString() );
            long endTime = System.currentTimeMillis() + DISCOVERYTIME;// timeout per rieseguire la discovery
            //apro socket unica del server 7776
            this.socketUni = new DatagramSocket( serverUnicastPORT );
            // ciclo comunicazioni unicat
            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
                try {
                    unicast();
                } catch (IOException e) {
                    int input = JOptionPane.showOptionDialog( null, "Problema di connessione del server, premi OK per chiudere!", "ERROR", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null );
                    if (input == JOptionPane.OK_OPTION) {
                        System.exit( -1 );
                    }

                }
                aggiornaListaIot();
                //          attendi 1 secondo prima di iniziare il ciclo di controllo di stato
                Thread.sleep( 1000 );
            }
            this.socketUni.close();
            //          attendi 1 secondo prima di iniziare il ciclo di controllo di stato
            Thread.sleep( 1000 );
        }
    }


    /**
     * aggiornamento interfaccia server relativa la parte della lista utenti
     */
    private void aggiornaListaIot() {
        listIotUser.setText( "Utenti : " + IotUsers.size() + "\n\n" );
        for (int i = 0; i < IotUsers.size(); i++) {
            listIotUser.append( "Iot device #" + (i + 1) + "\n" );
            listIotUser.append( IotUsers.get( i ).toString() + "\n\n" );
        }
    }

    /**
     * viene inizializzata l'interfaccia grafica e resa visibile
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
     * Viene impostata la comunicazione di tipo Unicast, cioè con un Iot Client alla volta,
     * è esguito un ciclo sulla lista dei utenti,caratterizzati dal proprio indirizzo ip + porta,
     * viene inviato un datagramma al client in questione che in caso di stato attivo risponde con un messaggio che
     * viene intercettato attraverso il metodo receive(con timeout settato ), si passa poi al client successivo
     * nella lista; nel caso non ci sia risposta dal client per 5 volte consecutive, quest'ultimo viene rimosso
     * dalla lista in quanto considerato spento.
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
                    System.out.println( "Timeout - Datagram response non received" );
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
     * Viene impostata la comunicazione di tipo Multicast in cui è inviato un unico pacchetto sulla rete che è
     * intercettato e eleborato solo dai client che si sono messi in ascolto su quell'indirizzo(ip+porta) multicast;
     * in seguito il server si mette in ricezione attendendo i pacchetti dai client per registrarli la prima volta.
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




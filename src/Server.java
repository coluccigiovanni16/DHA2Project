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
    private final static int DISCOVERYTIME = 10000;
    private final static int WAITMULTICAST = 5000;
    private final static int WAITRESPONSE = 100;
    private final static int NUMBEROFPACKET = 5;
    private final static int TTL = 32;
    private JTextArea listIotUser;
    private JButton shoutDownButton;
    private JPanel rootPanel;
    private JPanel statePanel;
    private JLabel stateServer;
    private LinkedList<InetSocketAddress> IotUsers;
    private DatagramSocket socketUni;
    private JFrame frame;
    private JLabel stateLabel;


    public Server() throws IOException, InterruptedException {
        this.IotUsers = new LinkedList<>();
        setGuivisible();
        while (true) {
            discoveryClient();
            aggiornaListaIot();
//          attendi 5 secondi prima di iniziare il ciclo di controllo di stato
            Thread.sleep( 1000 );
            System.out.println( IotUsers.toString() );
            long endTime = System.currentTimeMillis() + DISCOVERYTIME;// * IotUsers.size();
            //apro socket unica del server 7776
            this.socketUni = new DatagramSocket( 7776 );
            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
                unicast();
                Thread.sleep( 1000 );
            }
            aggiornaListaIot();
            this.socketUni.close();
            Thread.sleep( 1000 );
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server s = new Server();

    }

    private void aggiornaListaIot() {
        listIotUser.setText( "Utenti : " + IotUsers.size()+"\n\n" );
        for (int i = 0; i < IotUsers.size(); i++) {
            listIotUser.append( "Iot device #" + i + "\n" );
            listIotUser.append( IotUsers.get( i ).toString() + "\n\n" );
        }
    }

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

    private void unicast() throws IOException {
        stateServer.setText( "UNICAST" );
        statePanel.setBackground( Color.CYAN );
        for (int i = 0; i < this.IotUsers.size(); i++) {
            InetSocketAddress s = this.IotUsers.get( i );
            boolean vivo = false;
            byte[] mexSend = "Still Alive?".getBytes();
            DatagramPacket packetToSend = new DatagramPacket( mexSend, mexSend.length, s.getAddress(), s.getPort() );
//            scegliere il numero di pacchetti massimo da inviare
            for (int j = 0; j < NUMBEROFPACKET; j++) {
                this.socketUni.send( packetToSend );
                byte[] mexRecv = new byte[65507];
                DatagramPacket packetReceived = new DatagramPacket( mexRecv, mexRecv.length );
                try {
                    this.socketUni.setSoTimeout( WAITRESPONSE );
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
        stateServer.setText( "MULTICAST" );
        statePanel.setBackground( Color.GREEN );
        String message = "Someone online?";
        byte mex[] = message.getBytes();
        DatagramPacket packetToSend = new DatagramPacket( mex, mex.length, InetAddress.getByName( "224.0.0.1" ), 7777 );
        MulticastSocket multiSocket = new MulticastSocket();
        multiSocket.setTimeToLive( TTL );
        multiSocket.send( packetToSend );
        long endTime = System.currentTimeMillis() + WAITMULTICAST;
        while (System.currentTimeMillis() < endTime) {
            mex = new byte[65507];
            DatagramPacket packet = new DatagramPacket( mex, mex.length );
            multiSocket.setSoTimeout( WAITMULTICAST + 1 );
            try {
                multiSocket.receive( packet );
                System.out.println( new String( packet.getData() ) );
                System.out.println( packet.getSocketAddress() );
                this.IotUsers.add( (InetSocketAddress) packet.getSocketAddress() );
            } catch (SocketTimeoutException timeOut) {
                System.out.println( "timeout" );
            }
        }
        multiSocket.close();
        System.out.println( "------------------" );
    }


}




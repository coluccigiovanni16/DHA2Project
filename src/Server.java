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
    private JTextArea listIotUser;
    private JButton shoutDownButton;
    private JPanel rootPanel;
    private JPanel statePanel;
    private LinkedList<InetSocketAddress> IotUsers;
    private DatagramSocket socketUni;
    private int discoveryTime = 60000;
    private int timeClient = 500;
    private JFrame frame;


    public Server() throws IOException, InterruptedException {
        this.IotUsers = new LinkedList<>();
        setGuivisible();
        while (true) {
            discoveryClient();
//          attendi 5 secondi prima di iniziare il ciclo di controllo di stato
            Thread.sleep( 5000 );

            System.out.println( IotUsers.toString() );
            aggiornaListaIot();
//            long endTime = System.currentTimeMillis() + 10000 * IotUsers.size();
            //apro socket unica del server 7776
            this.socketUni = new DatagramSocket( 7776 );

//            while (System.currentTimeMillis() < endTime && !IotUsers.isEmpty()) {
            unicast();
//                Thread.sleep( 1000 );
//            }
            this.socketUni.close();
            Thread.sleep( 5000 );
            aggiornaListaIot();

        }
    }

    private void aggiornaListaIot() {
        listIotUser.setText( "" );
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
        MulticastSocket multiSocket = new MulticastSocket();
        multiSocket.setTimeToLive( 32 );
        multiSocket.send( packetToSend );
        statePanel.setBackground( Color.GREEN );
        long endTime = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < endTime) {
            mex = new byte[65507];
            DatagramPacket packet = new DatagramPacket( mex, mex.length );
            multiSocket.setSoTimeout( 6000 );
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


    public static void main(String[] args) throws IOException, InterruptedException {
        Server s = new Server();

    }


}




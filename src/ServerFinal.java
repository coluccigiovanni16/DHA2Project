import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerFinal {


    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        DatagramSocket socket1 = new DatagramSocket( 7776 );
        while (true) {

            byte mex[] = "Still alive?".getBytes();
            InetAddress address = InetAddress.getByName( "224.0.0.1" );
            DatagramPacket packet = new DatagramPacket( mex, mex.length, address, 7777 );
            socket.send( packet );
            mex = new byte[64000];
            packet = new DatagramPacket( mex, mex.length );
            socket1.receive( packet );
            String modifiedSentence =
                    new String( packet.getData() );
            System.out.println( modifiedSentence );

            Thread.sleep( 5000 );
        }
    }
}



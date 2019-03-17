package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Receiving {
    public Receiving() throws IOException {
        MulticastSocket socket = new MulticastSocket( 7777 );
        InetAddress address = InetAddress.getByName( "224.0.0.2" );
        socket.joinGroup( address );
        byte[] mex = new byte[64000];
        DatagramPacket packet = new DatagramPacket( mex, mex.length );
        socket.receive( packet );
        System.out.println( "Ricevuti: " + packet.getLength() + "bytes" );
        System.out.println( packet.getData() );
        String modifiedSentence =
                new String( packet.getData() );
        System.out.println( modifiedSentence );
    }

    public static void main(String[] args) throws IOException {
        Receiving receiving=new Receiving();
    }
}

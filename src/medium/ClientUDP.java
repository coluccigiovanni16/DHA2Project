package medium;//Importo i package necessari

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientUDP {
    public void avvio() throws IOException {
        InetAddress addr = InetAddress.getByName( "localhost" );
        BufferedReader stdIn = new BufferedReader( new InputStreamReader( System.in ) );
        byte[] msg = {0};
        //Creazione della Socket per l'invio del Datagramma con porta simple.Client dinamica
        DatagramSocket s = new DatagramSocket();
        //Creazione del pacchetto da inviare al simple.Server
        DatagramPacket hi = new DatagramPacket( msg, msg.length, addr, 7777 );
        //Ciclo infinito per inserimento testo del simple.Client
        while (true) {
            System.out.print( "Inserisci: " );
            String userInput = stdIn.readLine();
            System.out.println( "userInput: " + userInput );
            msg = userInput.getBytes();
            hi.setData( msg );
            hi.setLength( msg.length );
            //Invio del pacchetto sul Socket
            s.send( hi );
        }
    }

    public static void main(String[] args) throws IOException {
        ClientUDP udpClient = new ClientUDP();
        udpClient.avvio();
    }
}
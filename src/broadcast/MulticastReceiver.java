package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];

    public void run()  {
        try{
        socket = new MulticastSocket(4446);
        InetAddress group = InetAddress.getByName("224.0.0.0");
        socket.joinGroup(group);
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(
                    packet.getData(), 0, packet.getLength());
            if ("end".equals(received)) {
                break;
            }
        }
        socket.leaveGroup(group);
        socket.close();}
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MulticastReceiver multicastReceiver= new MulticastReceiver();
        multicastReceiver.start();
    }
}

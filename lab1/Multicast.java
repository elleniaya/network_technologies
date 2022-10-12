import java.io.IOException;
import java.net.*;
import java.util.*;

public class Multicast{
    private MulticastSocket multicastSocket;
    private DatagramSocket sendSocket;

    private byte[] buf = new byte[256];
    private final String ADDRESS;
    private final int PORT;
    private final int INTERVAL;
    private final long TIMEOUT;
    private final String MESSAGE = "Hello";

    public Multicast(String ADDRESS, int PORT, int INTERVAL, long TIMEOUT) {
        this.ADDRESS = ADDRESS;
        this.PORT = PORT;
        this.INTERVAL = INTERVAL;
        this.TIMEOUT = TIMEOUT;
    }

    private HashMap<String, Long> addressList = new HashMap<>();

    public void run () throws IOException {
        sendSocket = new DatagramSocket();

        multicastSocket = new MulticastSocket(PORT);
        InetAddress mcastaddr = InetAddress.getByName(ADDRESS);
        InetSocketAddress group = new InetSocketAddress(mcastaddr, PORT);
        NetworkInterface netIf = NetworkInterface.getByInetAddress(mcastaddr);
        System.out.println(netIf);
        multicastSocket.joinGroup(group, netIf);

        while (true) {
            DatagramPacket sendPacket = packetCreation(MESSAGE, mcastaddr, PORT);

            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                multicastSocket.setSoTimeout(INTERVAL);
                multicastSocket.receive(packet);
            } catch (SocketTimeoutException e) {
                sendSocket.send(sendPacket);
                continue;
            }

            String key = getStringKey(packet.getAddress());

            if (!addressList.containsKey(key)) {
                addressList.put(key, System.currentTimeMillis());
                printAddress();
            } else {
                addressList.put(key, System.currentTimeMillis());
            }

            deletingDisconnected();
        }
    }

    private String getStringKey(InetAddress address){
        return address.toString();
    }

    private DatagramPacket packetCreation (String message, InetAddress address, int port) {
        byte[] sendingDataBuffer = new byte[1024];
        sendingDataBuffer = message.getBytes();
        DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, address, port);
        return sendingPacket;
    }

    private void deletingDisconnected() {
        for (String key : addressList.keySet()) {
            if (System.currentTimeMillis() - addressList.get(key) > TIMEOUT) {
                addressList.remove(key);
                printAddress();
            }
        }
    }

    public void printAddress() {
        System.out.println("up-to-date list of connected copies of the program:");
        for (String key : addressList.keySet()) {
            System.out.println(key);
        }
    }
}

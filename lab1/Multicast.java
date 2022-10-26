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
    private boolean flag = true;

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

        InetAddress mAddr = InetAddress.getByName(ADDRESS);
        SocketAddress group = new InetSocketAddress(mAddr, PORT);
        NetworkInterface netInter = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        multicastSocket.joinGroup(group, netInter);

        multicastSocket.setSoTimeout(INTERVAL);
        DatagramPacket sendPacket = packetCreation(MESSAGE, mAddr, PORT);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        long time = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - time > INTERVAL) {
                sendSocket.send(sendPacket);
                time = System.currentTimeMillis();
            }

            try {
                multicastSocket.receive(packet);

                String key = getStringKey(packet.getAddress());

                if (!addressList.containsKey(key)) {
                    addressList.put(key, System.currentTimeMillis());
                    flag = true;
                } else {
                    addressList.put(key, System.currentTimeMillis());
                }
            } catch (SocketTimeoutException e){
                //System.out.println("no message");
                flag = false;
            } finally {
                deletingDisconnected();
                if (flag) printAddress();
            }
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
        ArrayList<String> rm = new ArrayList<String>();

        for (String key : addressList.keySet()) {
            if (System.currentTimeMillis() - addressList.get(key) > TIMEOUT) {
                rm.add(key);
            }
        }

        //System.out.println(rm.size());

        if (rm.size() != 0) {
            for (String key : rm) {
                addressList.remove(key);
            }
            flag = true;
        } 
    }

    public void printAddress() {
        System.out.println("up-to-date list of connected copies of the program:");
        for (String key : addressList.keySet()) {
            System.out.println(key);
        }
    }
}

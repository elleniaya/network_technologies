import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

public class UDPServer{
  DatagramSocket serverSocket;
  public final static int SERVICE_PORT = 956;
  static Logger logger = Logger.getLogger(UDPServer.class.getName());
 
  public UDPServer() throws SocketException {
    serverSocket = new DatagramSocket(SERVICE_PORT);
  }

  public void run() {
    try {
      boolean connection = check_connection(); //рукопожатие
      if (connection == false) {
        logger.info("connection failed");
        //System.out.println("connection failed");
        serverSocket.close();
        System.exit(0);
      } else {
        logger.info("connection setup was successful!");
        //System.out.println("connection setup was successful!");
      }
      message_exchange();
    } catch (IOException e){
      e.printStackTrace();
    } 
    serverSocket.close();
  } 

  public static DatagramPacket package_creation (String message, InetAddress senderAddress, int senderPort) {
    byte[] sendingDataBuffer = new byte[1024];
    sendingDataBuffer = message.getBytes();
    DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer,sendingDataBuffer.length, senderAddress, senderPort);
    return sendingPacket;
  }

  public boolean check_connection() throws IOException {
    byte[] SYN_Buffer = new byte[1024];

    DatagramPacket SYN_packet = new DatagramPacket(SYN_Buffer, SYN_Buffer.length);
    serverSocket.receive(SYN_packet);

    String SYN_receivedData = new String(SYN_packet.getData(), 0, SYN_packet.getLength());

    InetAddress senderAddress = SYN_packet.getAddress();
    int senderPort = SYN_packet.getPort();
      
    if (SYN_receivedData.equals("SYN")) {
      String SYN_ACK = "SYN_ACK";
      DatagramPacket syn_ack_package = package_creation(SYN_ACK, senderAddress, senderPort);
      serverSocket.send(syn_ack_package);
    } else return false;

    byte[] ACK_Buffer = new byte[1024];
    DatagramPacket ACK_packet = new DatagramPacket(ACK_Buffer, ACK_Buffer.length);
    serverSocket.receive(ACK_packet);

    String ACK_receivedData = new String(ACK_packet.getData(), 0, ACK_packet.getLength());

    if (!ACK_receivedData.equals("ACK")) return false;
      
    return true;
  }

  public void message_exchange() throws IOException {
    while (true) {
      byte[] Buffer = new byte[1024];
      DatagramPacket packet = new DatagramPacket(Buffer, Buffer.length);
      serverSocket.receive(packet);

      InetAddress senderAddress = packet.getAddress();
      int senderPort = packet.getPort();

      if (package_lost()) {
        logger.info("package was lost");
        //System.out.println("package was lost");
      } else {
        String ACK = "ACK";
        DatagramPacket ack_package = package_creation(ACK, senderAddress, senderPort);
        serverSocket.send(ack_package);
        
        String receivedData = new String(packet.getData(), 0, packet.getLength());
        System.out.println(receivedData);
      }
    }
  }

  public boolean package_lost() {
    return Math.random() > 0.5;
  } 
}

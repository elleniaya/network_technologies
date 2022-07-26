import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.*;
import java.util.logging.Logger;

public class UDPClient {
  DatagramSocket clientSocket;
  InetAddress IPAddress;
  static Logger logger = Logger.getLogger(UDPClient.class.getName());
  public final static int SERVICE_PORT = 956;
  public int time = 100;

  public UDPClient() throws SocketException, UnknownHostException {
    clientSocket = new DatagramSocket();
    IPAddress = InetAddress.getLocalHost();
  } 

  public void run() {
    try { 
      boolean connection = check_connection(IPAddress); 
      if (connection == false) {
        logger.info("Connection failed");
        clientSocket.close();
        System.exit(0);
      } else {
        logger.info("Connection setup was successful!");
      }
      message_exchange(IPAddress);
    } catch (IOException e){
      e.printStackTrace();
    } 
    clientSocket.close();
  }

  public boolean check_connection(InetAddress IPAddress) throws IOException {
    String SYN = "SYN";
    DatagramPacket syn_package = package_creation(SYN, IPAddress);
    clientSocket.send(syn_package);
    
    byte[] SYNACK_Buffer = new byte[1024];
    DatagramPacket SYN_ACK = new DatagramPacket(SYNACK_Buffer, SYNACK_Buffer.length);
    clientSocket.receive(SYN_ACK);
    
    String SYN_ACK_receivedData = new String(SYN_ACK.getData(), 0, SYN_ACK.getLength());

    if (SYN_ACK_receivedData.equals("SYN_ACK")) {
      String ACK = "ACK";
      DatagramPacket ack_package = package_creation(ACK, IPAddress);
      clientSocket.send(ack_package);
    } else return false;

    return true;
  }

  public void message_exchange(InetAddress IPAddress) throws IOException {
    String[] message = new String[5];
    message[0] = "Autumn";
    message[1] = "Autumn leaves are falling down,";
    message[2] = "Falling down, falling down.";
    message[3] = "Autumn leaves are falling down;";
    message[4] = "Red, yellow, orange and brown.";

    for (int i = 0; i < 5; i++) {
      DatagramPacket message_package = package_creation(message[i], IPAddress);
      for (int j = 0; j < 6; j++) {  
        clientSocket.send(message_package);
        if (delivery_check()) break;
        waiting_time_increase();
      }
    }
  }

  public static DatagramPacket package_creation (String message, InetAddress IPAddress) {
    byte[] sendingDataBuffer = new byte[1024];
    sendingDataBuffer = message.getBytes();
    DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer,sendingDataBuffer.length, IPAddress, SERVICE_PORT);
    return sendingPacket;
  }

  public boolean delivery_check() {
    byte[] ACK_Buff = new byte[1024];
    DatagramPacket ACK = new DatagramPacket(ACK_Buff, ACK_Buff.length);
    try {
      clientSocket.setSoTimeout(time);
      clientSocket.receive(ACK);
      return true;
    } catch (SocketTimeoutException e) {
      logger.info("Response timed out");
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } 
  }

  public void waiting_time_increase() {
    time *= 2;
  }
}

public class Main {
    public static void main(String[] args) throws Exception {
        String ADDRESS = "224.0.0.0";
        int PORT = 7676;
        int INTERVAL = 100;
        long TIMEOUT = 3 * INTERVAL;
        Multicast multicast = new Multicast(ADDRESS, PORT, INTERVAL, TIMEOUT);
        multicast.run();
    }
}

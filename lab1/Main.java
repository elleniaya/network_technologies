public class Main {
    public static void main(String[] args) throws Exception {
        //String ADDRESS = args[0];
        String ADDRESS = "230.0.0.0";
        int PORT = 7676;
        int INTERVAL = 1000;
        long TIMEOUT = 10 * INTERVAL;
        Multicast multicast = new Multicast(ADDRESS, PORT, INTERVAL, TIMEOUT);
        multicast.run();
    }
}

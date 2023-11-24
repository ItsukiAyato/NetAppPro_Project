import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UltraF_Server {
    private static final int port = 5656;
    public static int numThread = 4;
    private static ServerSocket server = null;

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThread);
        try {
            server = new ServerSocket(port);
            System.out.println("Server is running on PORT " + port);
            System.out.println("Waiting for client...");
            while(true) {
                Socket socket = server.accept();
                executor.execute(new Process2(socket));
            }
        } catch (IOException e) {
            System.out.println("Error: Internal Server Error");
            System.out.println("StatusCode: 500");
            System.out.println("Message: " + e.getMessage());
        } finally {
            if(server!=null)
                server.close();
        }
    }
}

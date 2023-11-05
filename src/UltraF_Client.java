import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class UltraF_Client {
    private static String host = "localhost";
    private static int port = 5656;
    private static Socket socket;
    private static BufferedReader inStream;
    private static BufferedWriter outStream;
    private static BufferedReader stdIn;

    public static void main(String[] args) throws Exception {
        try {
            socket = new Socket(host, port);
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            String input;

            while (true) {
                System.out.print("Enter Data: ");
                input = stdIn.readLine();
                if (input.equalsIgnoreCase("exit")) break;
                outStream.write(input + "\n");
                outStream.flush();

                String res = inStream.readLine();
                System.out.println("Response from Server:\n");
                while(!res.equals("-END-")) {
                    System.out.println(res);
                    res = inStream.readLine();
                }
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println("Error: Internal Server Error");
            System.out.println("StatusCode: 500");
            System.out.println("Message: " + e.toString());
        } finally {
            if(socket!=null) {
                stdIn.close();
                inStream.close();
                outStream.close();
                socket.close();
                System.out.println("Client is closed");
            }
        }
    }
}

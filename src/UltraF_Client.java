import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class UltraF_Client {
    private static final String Domain = "localhost";
    private static final int PORT = 6161;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(Domain, PORT);
            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("Enter Data: ");
                String clientData = stdIn.readLine();
                outStream.write(clientData + "\n");
                outStream.flush();

                String serverData = inStream.readLine();
                System.out.println("Response from Server:\n" + serverData);
                do {
                    serverData = inStream.readLine();
                    System.out.println(serverData);
                } while(!serverData.equals("-END-"));
                System.out.println();

                if (clientData.equalsIgnoreCase("exit")) break;
            }

            stdIn.close();
            inStream.close();
            outStream.close();
            socket.close();
            System.out.println("\nClient is closed!");
        } catch (UnknownHostException e) {
            System.out.println("Error: Bad Request");
            System.out.println("StatusCode: 400");
            System.out.println("Message: Url is not valid!");
        } catch (IOException e) {
            System.out.println("Error: Internal Server Error");
            System.out.println("StatusCode: 500");
            System.out.println("Message: " + e.getMessage());
        }
    }
}

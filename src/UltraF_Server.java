import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class UltraF_Server {
    private static final int PORT = 6161;


    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server is running on PORT " + PORT);

            Socket client = server.accept();
            System.out.println("Client " + client.getRemoteSocketAddress() + " connected");
            BufferedReader inStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

            while (true) {
                String clientData = inStream.readLine();
                System.out.println("Client data: " + clientData);
                outStream.write(clientData + "\n-END-\n");
                outStream.flush();
                System.out.println();

                if (clientData.equalsIgnoreCase("exit")) break;
            }

            inStream.close();
            outStream.close();
            client.close();
            server.close();
            System.out.println("\nServer is closed!");
        } catch (IOException e) {
            System.out.println("Error: Internal Server Error");
            System.out.println("StatusCode: 500");
            System.out.println("Message: " + e.getMessage());
        }
    }
}

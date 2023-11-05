import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class UltraF_Server {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 Edg/118.0.2088.69"
    private static final int PORT = 6161;

    private static String isOnline() {
        return null;
    }

    private static String getMusicInfo(String songName) {
        String dataSource = "https://www.google.com/search?q=" + songName + "+nhaccuatui";

        try {
            Document searchingLyric = Jsoup.connect(dataSource).method(Connection.Method.GET).timeout(30000).ignoreContentType(true).execute().parse();
            System.out.println("?");
            Document songLyric = Jsoup.connect(searchingLyric.select("div.MjjYud a").first().attr("href")).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            String lyrics = songLyric.select(".pd_lyric.trans").first().html();
            lyrics = lyrics.toString().replaceAll("<br>", "");
            System.out.println(lyrics);
            return lyrics + "\n-END-\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Internal Server Error\nStatus: 500\nMessage: " + e.toString();
        }
    }

    private static String getVietnameseSingerInfo(String singerName) {
        String dataSource1 = "https://www.google.com/search?q=wiki" + singerName;
        String dataSource2 = "";

        try {
            Document searchingArtist = Jsoup.connect(dataSource1).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            Document wiki = Jsoup.connect(searchingArtist.select("div.MjjYud a").first().attr("href")).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            String name = wiki.select("span.nickname").first().text();

//            String dateOfBirth = wiki.select("span.noprint").first().parent().text();
//            String birthPlace = wiki.select("");
//            System.out.println("ho ten: " + name + "\nElement: " + dateOfBirth);
        } catch (Exception e) {
            return "Error: Internal Server Error\nStatus: 500\nMessage: " + e.toString();
        }

        return null;
    }

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
                StringTokenizer token = new StringTokenizer(clientData, ";");
                String requestType = token.nextToken();
                String input = token.nextToken();
                System.out.println("Client request: " + clientData);
                if (requestType.equalsIgnoreCase("music")) {
                    outStream.write(getMusicInfo(input));
                    outStream.flush();
                }
                else {
                    outStream.write(getVietnameseSingerInfo(input));
                    outStream.flush();
                };

                if (clientData.equalsIgnoreCase("exit")) break;
            }

            inStream.close();
            outStream.close();
            client.close();
            server.close();
            System.out.println("\nServer is closed!");
        } catch (Exception e) {
            System.out.println("Error: Internal Server Error");
            System.out.println("StatusCode: 500");
            System.out.println("Message: " + e.getMessage());

        }
    }
}

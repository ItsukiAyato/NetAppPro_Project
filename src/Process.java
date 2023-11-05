import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public class Process implements Runnable {
    private BufferedReader inStream;
    private BufferedWriter outStream;
    private Socket client;

    public Process(Socket socket) { this.client = socket; }

    private static String getMusicInfo(String songName) {
//        String dataSource = "https://www.google.com/search?q=" + songName + "+nhaccuatui";
        String dataSource1 = "https://www.google.com/search?q=lời%20bài%20hát%20" + URLEncoder.encode(songName);
        String dataSource2 = "https://www.google.com/search?q=" + URLEncoder.encode(songName + " loibaihat.biz");

        try {
//            Document searchingLyric = Jsoup.connect(dataSource).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
//            Document songLyric = Jsoup.connect(searchingLyric.select("div.MjjYud a").first().attr("href")).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
//            String lyrics = songLyric.select(".pd_lyric.trans").first().html();
//            lyrics = lyrics.toString().replaceAll("<br>", "")
//            return lyrics + "\n-END-\n";

            Document searchingSong = Jsoup.connect(dataSource1).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            Element songInfoBlock = searchingSong.select("div[data-lyricid]").last();
            Elements lyricElements = songInfoBlock.select("div[jsname='U8S5sf'] > span");
            String musician = songInfoBlock.select(".auw0zb").first().text();
            if (musician.isBlank()) {
                Document searchingSong2 = Jsoup.connect(dataSource2).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
                String redirect = searchingSong2.select("div.MjjYud a").first().attr("href").toString();
                Document doc = Jsoup.connect(redirect).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
                musician = "Nhạc sĩ: " + doc.select(".lyric-des").select("a").text();
            }
            String lyrics = "";
            for(Element ele: lyricElements) {
                lyrics += ele.text() + "\n";
            }
            String songInfo = musician + "\nLời bài hát: " + lyrics;
            return songInfo + "\n-END-\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Internal Server Error\nStatus: 500\nMessage: " + e.toString() + "\n-END-\n";
        }
    }

    private static String getVietnameseSingerInfo(String singerName) {
        String dataSource1 = "https://www.google.com/search?q=wiki" + singerName;

        try {
            Document searchingArtist = Jsoup.connect(dataSource1).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            Document wiki = Jsoup.connect(searchingArtist.select("div.MjjYud a").first().attr("href")).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            String name = wiki.select("span.nickname").first().text();

            String dateOfBirth = wiki.select("span.noprint").first().parent().text();
//            String birthPlace = wiki.select("");
            System.out.println("ho ten: " + name + "\nElement: " + dateOfBirth);
        } catch (Exception e) {
            return "Error: Internal Server Error\nStatus: 500\nMessage: " + e.toString() + "\n-END-\n";
        }

        return null;
    }
    
    @Override
    public void run() {
        System.out.println("Client " + client.toString() + " is connected");
        try {
            inStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outStream = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            String clientData, requestType, input;

            while (true) {
                clientData = inStream.readLine();
                System.out.println("Client request: " + clientData);
                StringTokenizer token = new StringTokenizer(clientData, ";");
                requestType = token.nextToken();
                input = token.nextToken();

                if (requestType.equalsIgnoreCase("music")) {
                    outStream.write(getMusicInfo(input));
                    outStream.flush();
                }
                if (requestType.equalsIgnoreCase("singer")) {
                    outStream.write(getVietnameseSingerInfo(input));
                    outStream.flush();
                };
            }
        } catch (Exception e) {
            String error = "Status: 500\nError: Server Internal Error\nMessage: " + e.toString() + "\n-END-\n";
            try {
                outStream.write(error);
                outStream.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

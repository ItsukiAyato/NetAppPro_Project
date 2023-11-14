import org.json.JSONArray;
import org.json.JSONObject;
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
        String dataSource1 = "https://www.google.com/search?q=lời%20bài%20hát%20" + URLEncoder.encode(songName.trim());
        String dataSource2 = "https://www.google.com/search?q=" + URLEncoder.encode(songName.trim() + " loibaihat.biz");

        try {
            Document searchingSong = Jsoup.connect(dataSource1).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            Element songInfoBlock = null;
            Elements lyricElements = null;
            try {
                songInfoBlock = searchingSong.select("div[data-lyricid]").last();
                lyricElements = songInfoBlock.select("div[jsname='U8S5sf'] > span");
            } catch (Exception e) {
                return "Error: Bad Request\nStatus: 400\nMessage: Tên bài hát không hợp lệ\n-END-\n";
            }

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
        String dataSource1 = "http://www.google.com/search?q=wiki+ca+si+" + URLEncoder.encode(singerName.trim());

        try {
            //Search singer basic info
            String basicInfo = "", detailInfo = "";
            try {
                Document searchingArtist = Jsoup.connect(dataSource1).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
                Document wiki = Jsoup.connect(searchingArtist.select("div.MjjYud a").first().attr("href")).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
                Elements infoBlock = wiki.select(".infobox tr");

                if (infoBlock.isEmpty()) throw new Exception("Exception");
                boolean isName = infoBlock.select("th[scope=\"row\"]").first().text().equalsIgnoreCase("sinh");
                String name = !isName ? infoBlock.select("td.nickname").first().text() : "";
                String data = infoBlock.select("span.noprint").first().parent().text();
                basicInfo = !name.isBlank() ? "Thông tin cơ bản: " + name + " - " + data : "Thông tin cơ bản: " + data;
            } catch (Exception e) {
                return "Error: Bad Request\nStatus: 400\nMessage: Tên ca sĩ không hợp lệ\n-END-\n";
            }

            detailInfo = getSingerDetailInfo(singerName);
            if (detailInfo.contains("Error:")) throw new Exception(detailInfo);
            System.out.println(basicInfo + "\n" + detailInfo + "\n-END-\n");
            return basicInfo + "\n" + detailInfo + "\n-END-\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Internal Server Error\nStatus: 500\nMessage: " + e.toString() + "\n-END-\n";
        }
    }

    private static String getSingerDetailInfo(String singerName) {
        String dataSource2 = "https://www.last.fm/search?q=" + URLEncoder.encode(singerName.trim());
        //Search singer description and albums,songs
        try {
            Document searchInfo = Jsoup.connect(dataSource2).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            Elements allImages = searchInfo.select(".col-main section .grid-items-cover-image-image img");
            Element singerBlock = null;

            for (Element img : allImages) {
                String src = img.attr("src");
                if (!src.equals("https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.jpg")) {
                    singerBlock = img;
                    break; // Exit the loop once a match is found (first element)
                }
            }
            singerBlock = singerBlock == null ? allImages.first() : singerBlock;
            String imgUrl = singerBlock.attr("src");
            String singer = singerBlock.parent().parent().select("a").text().trim().replace(" ", "%20");
            //searching bio
            Document bioDoc = Jsoup.connect("https://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist="+singer+"&api_key=cba248d144ff32ac6045299e17006e33&format=json").method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            JSONObject bioJson = new JSONObject(bioDoc.text());
            String biography = bioJson.getJSONObject("artist").getJSONObject("bio").get("content").toString();
            biography = biography.replace("Read more on Last.fm. User-contributed text is available under the Creative Commons By-SA License; additional terms may apply.", "").replace("\n\n", "\n");
            //Translate to vietnamese if bio is english
            //not done yet

            //searching top albums
            String albums = "\nTop Albums: ";
            Document albumsDoc = Jsoup.connect("https://ws.audioscrobbler.com/2.0/?method=artist.gettopalbums&artist="+singer+"&api_key=cba248d144ff32ac6045299e17006e33&format=json").method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            JSONObject albumsJson = new JSONObject(albumsDoc.text());
            JSONArray albumArr = albumsJson.getJSONObject("topalbums").getJSONArray("album");
            for (int i=0; i < albumArr.length(); i++) {
                albums += albumArr.getJSONObject(i).get("name").toString();
                if (i != albumArr.length() - 1) albums += ", ";
            }

            //searching top tracks
            String tracks = "\nTop Songs: ";
            Document tracksDoc = Jsoup.connect("https://ws.audioscrobbler.com/2.0/?method=artist.gettoptracks&artist="+singer+"&api_key=cba248d144ff32ac6045299e17006e33&format=json").method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            JSONObject tracksJson = new JSONObject(tracksDoc.text());
            JSONArray trackArr = tracksJson.getJSONObject("toptracks").getJSONArray("track");
            for (int i=0; i < trackArr.length(); i++) {
                tracks += trackArr.getJSONObject(i).get("name").toString();
                if (i != trackArr.length() - 1) tracks += ", ";
            }
            return "\nHình ảnh: " + imgUrl + "\nTiểu sử: " + biography + albums + tracks;
        } catch (Exception e) {
            return "Error: " + e.toString();
        }
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

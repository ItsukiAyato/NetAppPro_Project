import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public class Process2 implements Runnable {
    private BufferedReader inStream;
    private BufferedWriter outStream;
    private Socket client;

    public Process2(Socket socket) { this.client = socket; }

    private static String getWeather(String locationName) {
        String weatherApi = "https://api.weatherapi.com/v1/current.json?key=85df1b955c87418d85d182741232111&q="+locationName;
        try {
            Document weather = Jsoup.connect(weatherApi).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            JSONObject obj2 =  new JSONObject(weather.text());
            JSONObject cur = obj2.getJSONObject("current");
            String tempc = cur.get("temp_c").toString();
            String condition = cur.getJSONObject("condition").get("text").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getHotelList() {
        return null;
    }

    private static String getTouristAttraction() {
        return null;
    }

    private static String getCountryInfo(String countryName) {
        String countryApi = "https://restcountries.com/v3.1/name/"+countryName+"?fullText=true";
        Document country = null;
        try {
            country = Jsoup.connect(countryApi).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            JSONArray ar1 =  new JSONArray(country.text());
            JSONObject name = ar1.getJSONObject(0).getJSONObject("name");
            String common = name.get("common").toString();
            JSONArray latlng = ar1.getJSONObject(0).getJSONArray("latlng");
            String pop = ar1.getJSONObject(0).get("population").toString();

            System.out.println(common);
            System.out.println(latlng.toString());
            System.out.println(pop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  "\n-END-\n";
    }

    private static JSONObject getCityInfo(String cityName) {
        String cityApi = "http://geodb-free-service.wirefreethought.com/v1/geo/places?limit=5&offset=0&namePrefix=" + URLEncoder.encode(cityName);
        JSONObject response = new JSONObject();
        try {
            Document cityDoc = Jsoup.connect(cityApi).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
            JSONObject jsonData = new JSONObject(cityDoc.text());
            JSONObject cityData = new JSONObject();
            try {
                cityData = jsonData.getJSONArray("data").getJSONObject(0);
            } catch (JSONException e) {
                response = new JSONObject().put("status", 400).put("message", "Not valid location!");
                return response;
            }
            response.put("latitude", cityData.get("latitude").toString());
            response.put("longitude", cityData.get("longitude").toString());
            response.put("name", cityData.get("name").toString());
            response.put("country", cityData.get("country").toString());
            response.put("population", cityData.get("population").toString());
            response.put("wikiDataId", cityData.get("wikiDataId").toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private static String getLocationInfo(String locationName) {
        try {
            return getCityInfo(locationName) + "\n-END-\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Internal Server Error\nStatus: 500\nMessage: " + e.toString() + "\n-END-\n";
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

                if (requestType.equalsIgnoreCase("location")) {
                    outStream.write(getLocationInfo(input));
                    outStream.flush();
                }
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

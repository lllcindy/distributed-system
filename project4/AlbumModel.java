package edu.cmu.task2server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The AlbumModel class runs as a model to process the logic
 * of the business. It connect to the 3rd party Api to get the JSON
 * from the api and do some operations.
 *
 */
public class AlbumModel {

    /**
     * GetAlbum method takes search term from client as parameter. It
     * splits the search term to find the singer name that will be used
     * in the url of api. And use the year to find the specific collection
     * names that clients want.
     *
     * @param searchTerm the search term user input
     * @return the String of the replies
     */
    public String[] GetAlbum(String searchTerm) throws IOException {
        String result="";
        String readLine = null;
        // Get the specific year
        String year = searchTerm.split("/")[0];
        // get the name of the singer and change the format
        String singer = searchTerm.split("/")[1].replace("-", "+").toLowerCase();
        // Get the name of the singer used for analysing
        String singer_s = searchTerm.split("/")[1].replace("-", " ").toLowerCase();
        String req = year + ", " + singer_s;
        //Get the search time
        String searchtime = searchTerm.split("/")[2];
        ArrayList<String> album = new ArrayList<>();
        // Get the time server fetch data
        Date date1 = new Date();
        Timestamp t1= new Timestamp(date1.getTime());
        String fetchtime = t1.toString();
        // connect to the 3rd party api to fetch the JSON data
        URL url = new URL("https://itunes.apple.com/search?term=" + singer + "&entity=album");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        String period="";
        int responseCode = conn.getResponseCode();
        //If the request is good, the process the JSON and get the needed information
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            Date date2 = new Date();
            // Get the time server used to get the JSON data
            long diff = date2.getTime() - date1.getTime();
            period = String.valueOf(diff);
            while ((readLine = in.readLine()) != null) {
                if ((readLine.contains("{")) && (readLine.contains("}"))) {
                    // Split to find the release date of the album
                    String date_string1 = readLine.split("\"releaseDate\":\"")[1];
                    String date_string2 = date_string1.split("\",")[0];
                    // If the year of the release date is the same as the user input, the get the collection as the result
                    if (date_string2.contains(year)) {
                        String album_string1 = readLine.split("\"collectionName\":\"")[1];
                        String album_string2 = album_string1.split("\",")[0];
                        album.add(album_string2);
                    }
                }
            }
            // If we cannot find the album, set result to ""
            if (album.size() == 0) {
                result = "";
            } else {
                for (int i = 0; i < album.size(); i++) {
                    result = result + album.get(i) + "//";
                }
                // Format the result in JSON format
                result = "{\"result\":\"" + result + "\"}";
            }
            in.close();
        } else {
            Date date2 = new Date();
            // Get the time server used to get the JSON data
            long diff = date2.getTime() - date1.getTime();
            period = String.valueOf(diff);
            // If the http request is bad, then set the result as ""
            result = "";
        }
        // Set the replies, request, search time, fetch time and search period into one array, and return the array.
        String[] reply = new String[5];
        reply[0]=result;
        reply[1]=req;
        reply[2]=searchtime;
        reply[3]=fetchtime;
        reply[4]=period;
        return reply;
    }
}

package edu.cmu.ds.musicalbum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The SearchAlbumModel class runs as a model to process the logic
 * of the business. It connect to the 3rd party Api to get the JSON
 * from the api and do some operations.
 *
 */

public class SearchAlbumModel {
    // The variable used for storing all the replies that will be displayed on result.jsp
    ArrayList<String> all_re = new ArrayList<>();

    /**
     * GetAlbum method takes search term from client as parameter. It
     * splits the search term to find the singer name that will be used
     * in the url of api. And use the year to find the specific collection
     * names that clients want.
     *
     * @param searchTerm the search term user input
     * @return the String of the replies
     */
    public String GetAlbum(String searchTerm) throws IOException {
        String result="";
        String readLine = null;
        // get the specific year
        String year = searchTerm.split("/")[0];
        // get the name of the singer and change the format
        String singer = searchTerm.split("/")[1].replace("-","+").toLowerCase();
        ArrayList<String> album = new ArrayList<>();
        // connect to the 3rd party api to fetch the JSON data
        URL url = new URL("https://itunes.apple.com/search?term="+singer+"&entity=album");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        //If the request is good, the process the JSON and get the needed information
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((readLine = in.readLine()) != null) {
                System.out.println(readLine);
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
            if (album.size()==0){
                result="";
            }
            else {
                for (int i = 0; i < album.size(); i++) {
                    result = result + album.get(i) + "//";
                }
                // Format the result in JSON format
                result="{\"result\":\""+result+"\"}";
            }
            in.close();
        } else {
            // If the http request is bad, then set the result as ""
            result="";
        }
        // record the every time result into the arraylist for display in .jsp
        all_re.add(result);
        return result;
    }

    /**
     * GetResult method return all the results that are replied to client as a String.
     *
     * @return the String of the all replies
     */
    public String GetResult(){
        String result = "";
        for (int i=0; i<all_re.size(); i++){
            result = result + all_re.get(i) + "<br>";
        }
        return result;
    }
}

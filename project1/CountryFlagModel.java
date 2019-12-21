package dsxindilan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author Xindi Lan
 * @date 19/09/2019
 *
 * This part models the business logic for the web application.
 * In this case, the business logic involves searching for
 * the specific flag and description of a given country. And
 * transferring the picture into the correct size.
 */

public class CountryFlagModel {

    /**
     *
     * @param searchTag The tag of the photo to be searched for.
     * @param picSize The string "mobile" or "desktop" indicating the size of
     * photo requested.
     * @return The list of flag url and description
     */
    public String[] doFlagSearch(String searchTag, String picSize)
            throws UnsupportedEncodingException  {
        String flagURL;
        String text;
        //If the search name is not a country, then return a error message string
        if (searchTag.equals("World") || searchTag.equals("European Union") || searchTag.equals("Hong Kong") || searchTag.equals("Macau") || searchTag.equals("Taiwan")){
            flagURL=null;
            text="";
            String[] results = {flagURL, text};
            return results;
        }
        //If the search name is found, remove all the " " , ( ) for convenient of matching
        else {
            searchTag=searchTag.replace(" ", "");
            searchTag=searchTag.replace(",", "");
            searchTag=searchTag.replace("(", "");
            searchTag=searchTag.replace(")", "");
            //Use this url and fetch method for scraping the list of country name.
            String URL = "https://www.cia.gov/library/publications/resources/the-world-factbook/docs/flagsoftheworld.html";
            String response = fetch(URL);

            // Create a hashmap to match the country name and the two-letters code
            String[] substring = response.split("data-place-code=\"");
            Map<String, String> map = new HashMap<>();
            int cutRight = 0, cutLeft = 0, cutMiddle = 0;

            /*In every line, the pattern of the two-letters code and the name
             * is the same. Just use "data-place-code=\"" and "\">" to find the
             * two-letters code, and use "\">" and "</option>" to find the country name.
             */
            for (int i = 0; i < substring.length - 2; i++) {
                cutRight += "</option>".length();
                cutLeft = response.indexOf("data-place-code=\"", cutRight);
                cutLeft += "data-place-code=\"".length();
                cutMiddle = response.indexOf("\">", cutLeft);
                String abbrv = response.substring(cutLeft, cutMiddle);
                cutMiddle += "\">".length();
                cutRight = response.indexOf("</option>", cutMiddle);
                //Also remove the " " , ( ) for convenient of matching
                String country = response.substring(cutMiddle, cutRight).strip().replace(" ","");
                country = country.replace(",", "");
                country = country.replace("(", "");
                country = country.replace(")", "");
                //Put country name and the code into map.
                map.put(country, abbrv);
            }
            //Search for the two-letters code by country name using hash map.
            String Tag = map.get(searchTag);
            //Use the two=letters code to generate countryURL
            String countryURL = "https://www.cia.gov/library/publications/resources/the-world-factbook/geos/" + Tag + ".html";
            //Use the fetch method to scraping the page source code.
            response = fetch(countryURL);
            String upperTag = Tag.toUpperCase();
            upperTag = URLEncoder.encode(upperTag, "UTF-8");

            /* The pattern of descriptions are all the same for all
             * the pages. Just use "<div class=\"photogallery_captiontext\">"
             * and "</div>" to locate the first appeared content, which is the description
             */
            int cutLeft1 = response.indexOf("<div class=\"photogallery_captiontext\">");
            cutLeft1 += "<div class=\"photogallery_captiontext\">".length();
            int cutRight1 = response.indexOf("</div>", cutLeft1);
            flagURL = "https://www.cia.gov/library/publications/resources/the-world-factbook/attachments/flags/" + upperTag + "-flag.gif";
            text = response.substring(cutLeft1, cutRight1).strip();
            //put the url and text into result list and return.
            //flagURL = FlagSize(flagURL, picSize);
            String[] results = {flagURL, text};
            System.out.println("pictureURL= " + flagURL);
            return results;
        }
    }

    /**
     * Return a URL of an image of appropriate size
     *
     * Arguments
     * @param picSize The string "mobile" or "desktop" indicating the size of
     * photo requested.
     * @return The URL an image of appropriate size.
     */
//    private String FlagSize(String pictureURL, String picSize) {
//        int finalDot = pictureURL.lastIndexOf(".");
//        String sizeLetter = (picSize.equals("mobile")) ? "m" : "z";
//        if (pictureURL.indexOf("_", finalDot-2) == -1) {
//            // If the URL currently did not have a _? size indicator, add it.
//            return (pictureURL.substring(0, finalDot) + "_" + sizeLetter
//                + pictureURL.substring(finalDot));
//        } else {
//            // Else just change it
//            return (pictureURL.substring(0, finalDot - 1) + sizeLetter
//                + pictureURL.substring(finalDot));
//        }
//    }

    /**
     * Make an HTTP request to a given URL
     * 
     * @param urlString The URL of the request
     * @return A string of the response from the HTTP GET.  This is identical
     * to what would be returned from using curl on the command line.
     */
    private String fetch(String urlString) {
        String response = "";
        try {
            URL url = new URL(urlString);
            // Create an HttpURLConnection.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
                //System.out.println(str);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Eeek, an exception");
            // Do something reasonable.  This is left for students to do.
        }
        return response;
    }
}

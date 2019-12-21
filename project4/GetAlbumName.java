package edu.cmu.ds.musicalbum;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.ArrayList;

import android.os.AsyncTask;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The Result class is to store the result value from server.
 *
 */
class Result {
	String value;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The GetAlbumName class can connect to server to search for specific album names.
 * Network operations cannot be done from the UI thread, therefore this class makes
 * use of an AsyncTask inner class that will do the network. onPostExecution runs
 * in the UI thread, and it calls the ImageView pictureReady method to do the update.
 *
 */
public class GetAlbumName {
    MusicAlbum ma = null;

    /**
     * search is the public GetAlbumName method. Its arguments are the search term,
     * and the MusicAlbum object that called it. This provides a callback path such
     * that the albumReady method in that object is called when the replies are
     * available from the search.
     *
     * @param searchTerm the String user input for search
     * @param ma MusicAlbum object
     */
    public void search(String searchTerm, MusicAlbum ma) {
        this.ma = ma;
        new AsyncSearch().execute(searchTerm);
    }

    /*
     * AsyncTask provides a simple way to use a thread separate from the UI thread
     * in which to do network operations. doInBackground is run in the helper thread.
     * onPostExecute is run in the UI thread, allowing for safe UI updates.
     */
    private class AsyncSearch extends AsyncTask<String, Void, String> {

        /**
         * doInBackground is running in the helper thread
         *
         * @param urls the search term from user
         * @return the search result
         */
        protected String doInBackground(String... urls) {
            return read(urls[0]);
        }

        /**
         * onPostExecute is run in the UI thread, allowing for safe UI updates.
         *
         * @param result the search result
         */
        protected void onPostExecute(String result) {
            ma.albumReady(result);
        }

        /**
         * read method return the corresponding replies according to the status code
         *
         * @param name the search term from user
         */
		private String read(String name) {
			Result r = new Result();
			int status = 0;
            // If the status is not 200, then return "". If the status is 200, the return the result from server
			if((status = search(name,r)) != 200) return "";
			return r.getValue();
		}

        /**
         * Search method connect to the web service to search for the information needed
         *
         * @param  searchTerm the search term given by user
         * @r Result object to store the replies from server
         * @return the request status code
         */
        private int search(String searchTerm, Result r) {
            r.setValue("");
            String response = "";
            HttpURLConnection conn;
            int status = 0;

            try {
                // pass the name on the URL line of web service on Heroku
                //This is the url for Task 2
                URL url = new URL("https://mysterious-bastion-27872.herokuapp.com" + "//"+searchTerm);
                // This is the url for Task1
//                URL url = new URL("https://sheltered-lake-01459.herokuapp.com" + "//"+searchTerm);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "text/plain");

                // wait for response
                status = conn.getResponseCode();

                // If things went poorly, don't try to read any response, just return.
                if (status != 200) {
                    String msg = conn.getResponseMessage();
                    return conn.getResponseCode();
                }
                String output = "";
                // things went well so let's read the response
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                while ((output = br.readLine()) != null) {
                    response += output;

                }
                System.out.println(response);
                conn.disconnect();

            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }   catch (IOException e) {
                e.printStackTrace();
            }

            // return value from server, and set the result object
            r.setValue(response);
            // return HTTP status to caller
            return status;
        }
    }
}
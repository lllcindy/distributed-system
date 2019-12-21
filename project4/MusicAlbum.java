package edu.cmu.ds.musicalbum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.sql.Timestamp;
import java.util.Date;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The MusicAlbum class manage the Android UI, process the user input and pass them to GetAlbumName
 *
 */
public class MusicAlbum extends AppCompatActivity {

    /**
     *
     * OnCreate method starts the Android application
     *
     * @param savedInstanceState Bundle object
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MusicAlbum ma = this;

        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = (Button) findViewById(R.id.submit);



        // Add a listener to the send button
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                Spinner YearSpinner = (Spinner) findViewById(R.id.spinner);
                // Get the singer name inputted by users
                String searchTerm = ((EditText) findViewById(R.id.searchTerm)).getText().toString();
                // Get the year selected by users
                String year = YearSpinner.getSelectedItem().toString();
                String searchTerm1 = searchTerm.replace(" ", "-");
                Date date1 = new Date();
                // Get the time stamp the user search
                Timestamp t1= new Timestamp(date1.getTime());
                // Make them into one search term, and pass it to GetAlbumName
                String term = year+"/"+searchTerm1+"/"+t1;
                GetAlbumName ga = new GetAlbumName();
                ga.search(term, ma); // Done asynchronously in another thread.  It calls ma.albumReady() in this thread when complete.
            }
        });

    }

    /**
     * This is called by the GetAlbumName object when the album name is ready.
     * This allows for passing back the String to update the TextView
     *
     * @param result The replies from server
     */
    public void albumReady(String result) {
        Spinner YearSpinner = (Spinner) findViewById(R.id.spinner);
        String year = YearSpinner.getSelectedItem().toString();
        TextView searchView = (EditText)findViewById(R.id.searchTerm);
        String searchTerm = ((EditText) findViewById(R.id.searchTerm)).getText().toString();
        TextView feedback = (TextView)findViewById(R.id.feedback);
        // If the result is not "", process the JSON data into the plain format that users are easy to read
        if (!result.equals("")) {
            String r= result.replace("//", "\n");
            String re = r.split("\"result\":\"")[1];
            String res = re.substring(0,re.length()-2);
            System.out.println(res);
            feedback.setText("The albums of "+searchTerm+" released in "+year+" are:\n\n"+res);
        } else {
            // If the reasult is "", then give a error massage to users
            feedback.setText("Sorry, I could not find the album of " + searchTerm+" released in "+year+". Please enter another search.");
        }
        searchView.setText("");
    }
}

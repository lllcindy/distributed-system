package cmu.edu.dsxindilan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/*
 * @author Xindi Lan
 * @date 19/09/2019
 *
 * This part models the business logic for the web application.
 * In this case, the business logic involves recording the
 * selected value each time and the number of times they are
 * selected, and return the selected value and its number of
 * times as string.
 */
public class ClickerModel {

    //Create a hash map to store answers and the number of times it appears
    HashMap<String, Integer> answers = new HashMap<String, Integer>();
    /**
     * Arguments.
     *
     * @param answer The value that is selected by user.
     *
     */
    public void countAnswer(String answer){
        //If the answer doesn't exist, then put the answer into the map and set value to 1.
        if(!answers.containsKey(answer)){
            answers.put(answer, 1);
        }
        //If the answer exists, then add its value by 1.
        else{
            answers.put(answer, answers.get(answer)+1);
        }
    }

    /**
     * Arguments.
     *
     * @return All the answers that are selected by users and their times user select as string.
     *
     */
    public String returnAnswer(){
        String result = "";
        //Create a arraylist to store all teh keys in map
        ArrayList<String> keys = new ArrayList<>(answers.keySet());
        //Sort all the keys in alphabet order
        Collections.sort(keys);
        //Get number of times the answer is selected by keys, and add them into one string.
        for (int i=0; i<keys.size(); i++){
            result = result + keys.get(i) + ": " + answers.get(keys.get(i)) + "<br>";
        }
        return result;
    }
}

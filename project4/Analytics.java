package edu.cmu.task2server;
import java.util.*;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The Analytics class uses several field to store the values used for
 * analytics, and has 3 methods to calculate the 3 interesting operations.
 *
 */
public class Analytics {
    //Store searchterm and their count number.
    private Map searchterm = new HashMap();
    //Store the time period users search and their count number
    private Map period = new HashMap();
    // Store the number of cases that have the result found
    private int found=0;
    // Store the number of cases that don't have the result found
    private int notfound=0;

    /**
     * The method add the count number of a certain singer
     *
     * @param singer the name of the searched singer
     */
    public void addSearch(String singer){
        // If the singer hasn't been seen before, the set the value to be 1
        if (!searchterm.containsKey(singer)){
            searchterm.put(singer, 1);
        }
        else{
            // If the singer has already been in the map, the add the value by 1
            int value = (int)searchterm.get(singer);
            searchterm.put(singer, value+1);
        }
    }

    /**
     * The method add the count number of a certain time period
     *
     * @param time the time that has been searched
     */
    public void addTime(String time){
        // Split the time and only remain the hour to compare
        String t = time.split(" ")[1];
        int hour = Integer.parseInt(t.split(":")[0]);
        // If the time is from 00:00 to 05:00, then value of ket "0" will be added
        if((hour>=0) && (hour<=5)){
            if (!period.containsKey("0")){
                period.put("0", 1);
            }
            else{
                int value = (int)period.get("0");
                period.put("0", value+1);
            }
        }
        // If the time is from 06:00 to 11:00, then value of ket "1" will be added
        else if((hour>=6) && (hour<=11)){
            if (!period.containsKey("1")){
                period.put("1", 1);
            }
            else{
                int value = (int)period.get("1");
                period.put("1", value+1);
            }
        }
        // If the time is from 12:00 to 17:00, then value of ket "2" will be added
        else if((hour>=12) && (hour<=17)){
            if (!period.containsKey("2")){
                period.put("2", 1);
            }
            else{
                int value = (int)period.get("2");
                period.put("2", value+1);
            }
        }
        // If the time is from 18:00 to 23:00, then value of ket "3" will be added
        else if((hour>=18) && (hour<=23)){
            if (!period.containsKey("3")){
                period.put("3", 1);
            }
            else{
                int value = (int)period.get("3");
                period.put("3", value+1);
            }
        }
    }

    /**
     * The method add the number of cases that have the result by 1
     *
     */
    public void addFound(){
        found++;
    }

    /**
     * The method add the number of cases that don't have the result by 1
     *
     */
    public void addNotFound(){
        notfound++;
    }

    /**
     * The method select the top 3 singers that were searched
     *
     * @return The array of String that contains the top 3 searched singers
     */
    public String[] mostSearch(){
        // Sort the map based on the value
        List<Map.Entry<String,Integer>> list = new ArrayList(searchterm.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue() - o2.getValue()));
        // Get the last 3 keys, and return
        String[] search = new String[3];
        search[0] = list.get(list.size()-1).getKey();
        search[1] = list.get(list.size()-2).getKey();
        search[2] = list.get(list.size()-3).getKey();
        return search;
    }

    /**
     * The method find the time period in a day that is searched most
     * @return String that indicates the time period searched most
     *
     */
    public String mostTime(){
        // Sort the map based on the value
        List<Map.Entry<String,Integer>> list = new ArrayList(period.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue() - o2.getValue()));
        // Get the last key
        String k = (String)list.get(list.size()-1).getKey();
        // If the ket is "0", then return "from 00:00 to 06:00"
        if (k.equals("0")){
            return "from 00:00 to 06:00";
        }
        // If the ket is "1", then return "from 06:00 to 12:00"
        else if (k.equals("1")){
            return "from 06:00 to 12:00";
        }
        // If the ket is "2", then return "from 12:00 to 18:00"
        else if (k.equals("2")){
            return "from 12:00 to 18:00";
        }
        else{
            // If the ket is "3", then return "from 18:00 to 00:00"
            return "from 18:00 to 00:00";
        }
    }

    /**
     * The method calculate the rate that the application can find the infomation needed
     *
     * @return double value that indicates the serve rate
     */
    public double serveRate(){
        double rate = (double)found/(double)(found+notfound);
        return (Math.round( rate * 100 ) / 100.0);
    }

}


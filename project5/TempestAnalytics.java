package edu.cmu.andrew.xindi.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Xindi Lan
 * @date 18/11/2019
 *
 * The EmpestAnalytics class count the lines, words and use
 * other methods in Spark to manipulate the file.
 *
 */
public class TempestAnalytics {

    private static void wordCount(String fileName) {

        //Create the configuration of Spark application
        SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("JD Word Counter");

        // Get the context object from SparkConf
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        // Read the file from the filepath
        JavaRDD<String> inputFile = sparkContext.textFile(fileName);

        // Task0: use .count() method to count the number of lines in a file
        System.out.println("There are "+inputFile.count()+" lines in the file.");

        // Use .flatMap to map the content to each word (use " " to split each word)
        JavaRDD<String> wordsFromFile = inputFile.flatMap(content -> Arrays.asList(content.split("[] \\s,?.;=!:'\"/()\t\\[]+")));

        // Task1: use .count() to count the number of words in the content
        System.out.println("There are "+wordsFromFile.count()+" words in the file.");

        // Use .distinct() to find all the unique words in content
        JavaRDD<String> rddDistinct = wordsFromFile.distinct();

        // Task2: use .count() to count the number of unique words in the file
        System.out.println("There are "+rddDistinct.count()+" distinct words in the file.");

        // Use mapToPair() to map each word to the format of (word, 1)
        JavaPairRDD<String, Integer> countData = wordsFromFile.mapToPair(t -> new Tuple2(t, 1));

        // Task3: use saveAsTextFile() save the result in the above step to the directory of "Project5/Part_2/TheTempestOutputDir1"
        countData.saveAsTextFile("Project5/Part_2/TheTempestOutputDir1");

        // Use reduceByKey() to add all the values that have the same key
        JavaPairRDD<String, Integer> wordFrequency = countData.reduceByKey((a, b) -> a + b);

        // Task4: use saveAsTextFile() save the result in the above step to the directory of "Project5/Part_2/TheTempestOutputDir2"
        wordFrequency.saveAsTextFile("Project5/Part_2/TheTempestOutputDir2");

        //Prompt the user to input a word
        System.out.println("Please enter a word you want to search: ");

        // Scanner object to accept the input
        Scanner s = new Scanner(System.in);

        // Convert the input to String
        String input = s.nextLine();

        // Prompt the printed result
        System.out.println("The lines contain the word are: ");

        // Use foreach() method to find the word in each line. The word should match "\\b"+input+"\\b" format where "\b"
        // is the boundary of a word. And then print the line that contains the word.
        inputFile.foreach((str) -> {
            Pattern p = Pattern.compile("\\b"+input+"\\b");
            Matcher m = p.matcher(str);
            if (m.find()) {
            System.out.println(str);
        }});
    }

    public static void main(String[] args) {

        // If there is no file as input, then print "No files provided."
        if (args.length == 0) {
            System.out.println("No files provided.");
            System.exit(0);
        }

        // Call the wordCount() method to do the calculation above
        wordCount(args[0]);
    }
}

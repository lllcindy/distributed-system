package edu.cmu.task2server;

import com.mongodb.client.*;
import org.bson.Document;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The mongoDB class manipulate the mongoDB Atlas, including connecting,
 * inserting, disconnecting and getting the value for Analytics class.
 *
 */
public class mongoDB {
    // The collection stores all my records (documents)
    private MongoCollection<Document> collection;
    // The client object
    private MongoClient mongoClient;
    // The database store the collection for log data
    private MongoDatabase database;

    /**
     * The method that connect Java application to MongoDB Atlas
     *
     */
    public void Connect(){
        mongoClient = MongoClients.create("mongodb+srv://xindilan:xindilan123@cluster0-fywiz.mongodb.net/test?retryWrites=true&w=majority");
        database = mongoClient.getDatabase("mydb");
        collection = database.getCollection("logdata");
    }

    /**
     * The method insert the search term, replies, search time,
     * fetch time, search period and status into each document,
     * and insert the document into the collection
     *
     * @param search The search term from client
     * @param result The replies the server get from 3rd party api
     * @param searchtime The time user search
     * @param fetchtime the time server tech the data
     * @param period the time server used to get the data
     * @param status the status code
     */
    public void Insert(String search, String result, String searchtime, String fetchtime, String period, int status){
        Document logdata = new Document("request", search)
                .append("response", result)
                .append("searchtime", searchtime)
                .append("fetchtime", fetchtime)
                .append("period", period)
                .append("status", status);
        collection.insertOne(logdata);
    }

    /**
     * The method disconnect the Java with MongoDB Atlas
     *
     */
    public void Disconnect(){

        // release resources
        mongoClient.close();
    }

    /**
     * The method uses a cursor to get all types of data,
     * and put them into Analytics object for analysing.
     *
     * @param ana The Anlytics object for aalysing interesting operations
     */
    public void Analysis(Analytics ana){
        // Connect to MongoDB Atlas
        mongoClient = MongoClients.create("mongodb+srv://xindilan:xindilan123@cluster0-fywiz.mongodb.net/test?retryWrites=true&w=majority");
        database = mongoClient.getDatabase("mydb");
        collection = database.getCollection("logdata");
        // Create cursor for getting all the data
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> cursor = findIterable.iterator();
        try{
            // In each loop, get the value by the name of the record
            while(cursor.hasNext()) {
                Document o = cursor.next();
                String req = (String) o.get("request");
                String res = (String) o.get("response");
                String t = (String) o.get("searchtime");
                int sta = (int) o.get("status");
                String singer = req.split(", ")[1];
                // Every time add a record, add the search time of the singer
                ana.addSearch(singer);
                // Every time add a record, add the time for this search
                ana.addTime(t);
                if(sta==200){
                    // If the status code is 200, then add the times the application find the information needed
                    ana.addFound();
                }
                else{
                    // If the status code is not 200, then add the times the application cannot find the information needed
                    ana.addNotFound();
                }
            }
        } finally {
            // After all the documents processed, close the cursor
            cursor.close();
        }
    }
}

package edu.cmu.task2server;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The AlbumServlet class runs as a servlet to listen to the search
 * terms from client (Android). It accept the search term and pass them to
 * AlbumModel to do further process.
 *
 */
public class AlbumServlet extends HttpServlet {
    AlbumModel sam = null;

    @Override
    public void init() {
        sam = new AlbumModel();
    }

    /**
     * Processes requests for HTTP GET method.
     * Returns a result and status code corresponding to a given search term.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("Console: doGET visited");
        int status=0;
        // If the url pattern is /dashboard, then show the dashboard.jsp
        if(request.getServletPath().equals("/dashboard")){
            //Create a MongoDB object to connect to MongoDB Altas and insert the log data
            mongoDB db = new mongoDB();
            // connect to my MongoDb Atlas
            db.Connect();
            // Create an Analytics for analyse interesting operations
            Analytics ana = new Analytics();
            db.Analysis(ana);
            // Get the top 3 singers that have been searched most
            String[] mostsearch = ana.mostSearch();
            // Get the time peroid that has been searched most
            String mosttime = ana.mostTime();
            // Get the rate the application and find the infomation needed
            double serverate = ana.serveRate();
            // Set attributes to sodplay on dashboard
            request.setAttribute("mostSearch1", mostsearch[0]);
            request.setAttribute("mostSearch2", mostsearch[1]);
            request.setAttribute("mostSearch3", mostsearch[2]);
            request.setAttribute("mostTime", mosttime);
            request.setAttribute("serveRate", serverate);

            String nextView;
            // Pass the result to the view.
            nextView = "dashboard.jsp";
            // Transfer control over the the correct "view"
            RequestDispatcher view = request.getRequestDispatcher(nextView);
            view.forward(request, response);
        }
        else{
            // The name is on the path /name so skip over the '/'
            String searchTerm = (request.getServletPath()).substring(1);

            // If the serach term is "", just pass it
            if (searchTerm == null || searchTerm.equals("")) {
                return;
            }
            // Get the result and the information needed for log data from ALbumModel
            String[] result = sam.GetAlbum(searchTerm);
            // The replies returned to client
            String reply = result[0];
            // The request from client
            String req = result[1];
            // The time users search for the term
            String searchtime = result[2];
            // The time server begins to search from Api
            String fetchtime = result[3];
            // The time the server takes to get the JSON from Api
            String period = result[4];

            // Connect to MongoDB Atlas
            mongoDB db = new mongoDB();
            db.Connect();
            // return 401 if the specific album cannot be found
            if (reply == null || reply.equals("")) {
                status = 401;
                // Insert the data into MongoDB
                db.Insert(req, reply, searchtime, fetchtime, period, status);
                response.setStatus(401);
                return;
            }

            // Things went well so set the HTTP response code to 200 OK
            response.setStatus(200);
            status = 200;
            // Insert the data into MongoDB
            db.Insert(req, reply, searchtime, fetchtime, period, status);

            response.setContentType("text/plain;charset=UTF-8");

            // return the value from a GET request
            PrintWriter out = response.getWriter();
            out.println(reply);

            db.Disconnect();
        }
    }

}



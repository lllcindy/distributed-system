package edu.cmu.ds.musicalbum;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 *
 * @author Xindi Lan
 * @date 6/11/2019
 *
 * The SearchAlbumServer class runs as a servlet to listen to the search
 * terms from client (Android). It accept the search term and pass them to
 * SearchAlbumModel to do further process.
 *
 */


public class SearchAlbumServer extends HttpServlet {
    SearchAlbumModel sam = null;

    @Override
    public void init() {
        sam = new SearchAlbumModel();
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
        // If the url pattern is /result, then show the result.jsp
        if (request.getServletPath().equals("/result")) {
            // Get all the replies and then set attribute
            String all_re = sam.GetResult();
            request.setAttribute("result", all_re);
            String nextView;
            // Pass the result to the view.
            nextView = "result.jsp";
            // Transfer control over the the correct "view"
            RequestDispatcher view = request.getRequestDispatcher(nextView);
            view.forward(request, response);
        } else {
            // The name is on the path /name so skip over the '/'
            String searchTerm = (request.getServletPath()).substring(1);

            // If the serach term is "", just pass it
            if (searchTerm == null || searchTerm.equals("")) {
                // no variable name found in map
                return;
            }
            // Get the result from SearchALbumModel
            String result = sam.GetAlbum(searchTerm);
            System.out.println(result);

            // return 401 if the specific album cannot be found
            if (result == null || result.equals("")) {
                response.setStatus(401);
                return;
            }

            // Things went well so set the HTTP response code to 200 OK
            response.setStatus(200);
            response.setContentType("text/plain;charset=UTF-8");

            // return the value from a GET request
            PrintWriter out = response.getWriter();
            out.println(result);
        }
    }
}


package dsxindilan;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "CountryFlagServlet",
       urlPatterns = {"/getFlagPicture"})
/*
 *
 * @author Xindi Lan
 * @date 19/09/2019
 *
 * The servlet is acting as the controller.
 * There are 3 views - prompt.jsp and result.jsp.
 * It decides between the pages by determining if there is a
 * countryname parameter or not. At the beginning it uses prompt.jsp.
 * After the user input the countryname parameter, then it searches
 * for the country flag and description and return the result.jsp.
 * The model is provided by CountryFlagModel.
 *
 */
public class CountryFlagServlet extends HttpServlet {

    CountryFlagModel ipm = null;  // The "business model" for this app

    // Initiate this servlet by instantiating the model that it will use.
    @Override
    public void init() {
        ipm = new CountryFlagModel();
    }

    /**
     * Processes requests for both HTTP GET method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // get the countryName parameter if it exists
        String search = request.getParameter("countryName");

        // determine what type of device our user is
        String ua = request.getHeader("User-Agent");

        boolean mobile;
        // prepare the appropriate DOCTYPE for the view pages
        if (ua != null && ((ua.indexOf("Android") != -1) || (ua.indexOf("iPhone") != -1))) {
            mobile = true;
            request.setAttribute("doctype", "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.2//EN\" \"http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd\">");
        } else {
            mobile = false;
            request.setAttribute("doctype", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        }

        String nextView;
        //If search is not null, use the method in CountryFlagModel to get the flag and description
        if (search != null) {
            String picSize = (mobile) ? "mobile": "desktop";
            // use model to do the search and choose the result view
            String flagURL = ipm.doFlagSearch(search, picSize)[0];
            String description = ipm.doFlagSearch(search, picSize)[1];
            //Pass the search result to the web page
            request.setAttribute("flagURL",flagURL);
            request.setAttribute("description",description);
            // Pass the search result to the view.
            nextView = "result.jsp";
        } else {
            // no search parameter so choose the prompt view
            nextView = "prompt.jsp";
        }
        // Transfer control over the the correct "view"
        RequestDispatcher view = request.getRequestDispatcher(nextView);
        view.forward(request, response);
    }
}


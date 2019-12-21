package cmu.edu.dsxindilan;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 *
 * @author Xindi Lan
 * @date 19/09/2019
 *
 * The servlet is acting as the controller.
 * There are 4 views - index.jsp, noresult.jsp, result.jsp and resultfinal.jsp.
 * It decides among the pages by determining which button value is selected.
 * At the beginning it uses index.jsp. If there is a value selected, then
 * return result.jsp, and use the method in model to calculate the number of
 * times the value be selected. And when input /getResults, if there is no
 * value selected, then return noresult.jsp. Otherwise, return the resultfinal.jsp
 * with the result of method in model.
 * The model is provided by ClickerModel.
 *
 */

public class ClickerServlet extends HttpServlet {
    ClickerModel ipm = null;  // The "business model" for this app.
    // Initiate this servlet by instantiating the model that it will use.
    @Override
    public void init() {
        ipm = new ClickerModel();
    }

    /**
     * Processes requests for both HTTP POST method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Get inputButton parameter if it exists.
        String search = request.getParameter("inputButton");
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
        //Use the countAnswer method in model to record the number of times the value be selected.
        ipm.countAnswer(search);

        String nextView;
        // Pass the result to the view.
        nextView = "result.jsp";
        // Transfer control over the the correct "view"
        RequestDispatcher view = request.getRequestDispatcher(nextView);
        view.forward(request, response);
    }

    /**
     * Processes requests for both HTTP GET method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

        //Use the returnAnswer method in model to return the result of all the surveys
        String re = ipm.returnAnswer();
        //If there is no value selected, return the noresult.jsp to view
        if(re.equals("")){
            RequestDispatcher view = request.getRequestDispatcher("noresult.jsp");
            view.forward(request, response);
        }
        //If there are results, then pass the result to web page to the correct view.
        else {
            request.setAttribute("result", re);
            // Transfer control over the correct "view"
            RequestDispatcher view = request.getRequestDispatcher("resultfinal.jsp");
            view.forward(request, response);
        }
    }
}

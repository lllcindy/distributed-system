package cmu.edu.xindilan;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/*
 *
 * @author Xindi Lan
 * @date 19/09/2019
 *
 * The servlet is acting as the controller.
 * There are 3 views - index.jsp, result.jsp and nextpage.jsp.
 * It decides among the pages by determining if there is a
 * inputText parameter or not. At the beginning it uses index.jsp.
 * If there is no parameter, then it uses nextpage.jsp.
 * If there is a inputText parameter, then it does the
 * computation and return the result.jsp.
 * The model is provided by HashModel.
 */

public class ComputeHashes extends HttpServlet {

    HashModel ipm = null;  // The "business model" for this app

    // Initiate this servlet by instantiating the model that it will use.
    @Override
    public void init() {
        ipm = new HashModel();
    }

    /**
     * Processes requests for both HTTP GET method.
     *
     * @param request servlet request
     * @param response servlet response
     */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // get the inputText parameter if it exists
            String inputText = request.getParameter("inputText");
            // get the inputButton parameter if it exists
            String inputButton = request.getParameter("inputButton");
            //If the input of user is null, then show a friendly prompt to user
            if(inputText==null){
                String  errormessage="The Text is Required!";
                //pass the error message to the web page
                request.setAttribute("errormessage", errormessage);
                //Pass the attributes and parameters to the view
                String nextView = "nextpage.jsp";
                // Transfer control over to the correct "view"
                RequestDispatcher view = request.getRequestDispatcher(nextView);
                view.forward(request, response);
            }

            //If the input of user is not null, then pass the correct text to the web page.
            else {
                //Use model and other functions to get the correct result
                String binary1 = javax.xml.bind.DatatypeConverter.printBase64Binary(ipm.getHash(inputText, inputButton));
                String binary2 = javax.xml.bind.DatatypeConverter.printHexBinary(ipm.getHash(inputText, inputButton));
                //Pass the convert result to the web page
                request.setAttribute("binary1", binary1);
                request.setAttribute("binary2", binary2);
                //Pass the attributes and parameters to the view.
                String nextView = "result.jsp";
                //Transfer control over to the correct "view".
                RequestDispatcher view = request.getRequestDispatcher(nextView);
                view.forward(request, response);
            }
    }
}

package com.uni.mannheim.dws.mapper.webInterface.servlet;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.uni.mannheim.dws.mapper.controller.WebTupleProcessor;
import com.uni.mannheim.dws.mapper.dbConnectivity.DBConnection;
import com.uni.mannheim.dws.mapper.engine.query.QueryEngine;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.knowledgeBase.UncertainKB;
import com.uni.mannheim.dws.mapper.logic.FactSuggestion;

/**
 * Servlet class to handle requests for testing the matching performance
 * 
 * @author Arnab Dutta
 */
public class EntryServlet extends HttpServlet
{

    // define Logger
    static Logger logger = Logger.getLogger(EntryServlet.class.getName());

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of the object.
     */
    public EntryServlet()
    {
        super();
    }

    /**
     * Destruction of the servlet. <br>
     */
    public void destroy()
    {
        super.destroy(); // Just puts "destroy" string in log
        // Put your code here
    }

    Connection connection = null;

    PreparedStatement pstmt = null;

    UncertainKB uncertainKB = new UncertainKB();

    /**
     * The doPost method of the servlet. <br>
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        // list of resultsets to be displayed back on the UI
        List<ResultDAO> retListPredLookUp = new ArrayList<ResultDAO>();
        List<ResultDAO> retListPredSearch = new ArrayList<ResultDAO>();
        List<ResultDAO> retListObj = new ArrayList<ResultDAO>();
        List<ResultDAO> retListSubj = new ArrayList<ResultDAO>();

        // list for suggested fact
        List<SuggestedFactDAO> retListSuggstFacts = new ArrayList<SuggestedFactDAO>();

        List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();

        // initialize with some default values
        int topK = Constants.TOPK;

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        String action = (request.getParameter("action") != null) ? request.getParameter("action") : "sss";

        try {
            if (!"Save Facts".equals(action)) {

                // set topk attribute
                if (request.getParameter("topk") != null) {
                    topK = Integer.parseInt(request.getParameter("topk"));
                }
                QueryEngine.setTopK(topK);
                String subject = request.getParameter("subject").trim();
                String predicate = request.getParameter("predicate").trim();
                String object = request.getParameter("object").trim();
                // This is simple search mode. just querying for terms
                if (!Constants.PREDICTIVE_SEARCH_MODE) { // not really useful..but to just play around

                    // create File object of our index directory
                    File file = new File(Constants.DBPEDIA_ENT_INDEX_DIR);

                    if (!subject.equals("Subject") && !subject.equals("")) {
                        retListSubj = QueryEngine.doSearch(subject, file);
                    }
                    if (!object.equals("Object") && !object.equals("")) {
                        retListObj = QueryEngine.doSearch(object, file);
                    }
                } else {// This is advanced search mode. where the system tries to predict the best matches based on
                        // the input combination

                    // declare class
                    WebTupleProcessor webTupleProc = new WebTupleProcessor(pool, subject, object, predicate);

                    // make a call to the Engine with the required parameters
                    webTupleProc.processTuples(null);

                    // retrieve and hold all the results
                    retList = webTupleProc.getRetList();
                    retListSubj = retList.get(0);
                    retListObj = retList.get(1);

                    // get the predicates
                    retListPredLookUp = webTupleProc.getRetListPredLookUp();
                    retListPredSearch = webTupleProc.getRetListPredSearch();
                }
                // set the request parameter for results display
                if (retListSubj.size() > 0) {
                    request.setAttribute("matchingListSubj", retListSubj);
                }
                if (retListObj.size() > 0) {
                    request.setAttribute("matchingListObj", retListObj);
                }
                request.setAttribute("matchingListPredLookup", retListPredLookUp);
                request.setAttribute("matchingListPredSearch", retListPredSearch);
                // for resetting the text boxes
                request.setAttribute("subject", subject);
                request.setAttribute("predicate", predicate);
                request.setAttribute("object", object);
                request.setAttribute("topk", topK);
                // return a list of possible facts suggestion from best matches
                retListSuggstFacts =
                    FactSuggestion.suggestFact(retListSubj, subject, retListPredLookUp, retListPredSearch, predicate,
                        retListObj, object);
                // for setting the suggested fact that system thinks to be true
                request.setAttribute("suggestedFactList", retListSuggstFacts);

            } else {
                logger.info("U R HERE...");

                String[] facts = request.getParameterValues("checkbox");
                for (String fact : facts) {
                    logger.info(fact);

                    String[] str = fact.split("~");

                    // save it to the KB
                    uncertainKB.createKB(connection, pstmt, new SuggestedFactDAO(str[0], str[1], str[2], null, true));
                }
            }

            // redirect to page
            request.getRequestDispatcher("page/entry.jsp").forward(request, response);

        }

        catch (Throwable theException) {
            System.out.println(theException);
        }

    }

    /**
     * Initialization of the servlet. <br>
     * 
     * @throws ServletException if an error occurs
     */
    public void init() throws ServletException
    {
        DBConnection dbConnection = new DBConnection();

        // retrieve the freshly created connection instance
        connection = dbConnection.getConnection();
        try {
            // create a statement
            pstmt = connection.prepareStatement(Constants.INSERT_FACT_SQL);
        } catch (SQLException ex) {
            logger.error(" EXception in init " + ex.getMessage());
        }

    }

}

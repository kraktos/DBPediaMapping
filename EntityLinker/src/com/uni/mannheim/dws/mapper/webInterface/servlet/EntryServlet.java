package com.uni.mannheim.dws.mapper.webInterface.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.controller.WebTupleProcessor;
import com.uni.mannheim.dws.mapper.engine.query.QueryEngine;
import com.uni.mannheim.dws.mapper.helper.util.Constants;

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

        List<ResultDAO> retListPredLookUp = new ArrayList<ResultDAO>();
        List<ResultDAO> retListPredSearch = new ArrayList<ResultDAO>();
        List<ResultDAO> retListObj = new ArrayList<ResultDAO>();
        List<ResultDAO> retListSubj = new ArrayList<ResultDAO>();

        List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();

        WebTupleProcessor webTupleProc = null;
        // initialize with some default values
        int topK = Constants.TOPK;

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            if (request.getParameter("topk") != null) {
                topK = Integer.parseInt(request.getParameter("topk"));
            }

            QueryEngine.setTopK(topK);

            String subject = request.getParameter("subject").trim();
            String predicate = request.getParameter("predicate").trim();
            String object = request.getParameter("object").trim();

            // This is simple search mode. just querying for terms
            if (!Constants.PREDICTIVE_SEARCH_MODE) { // not really useful..but to just play around

                if (!subject.equals("Subject") && !subject.equals("")) {
                    retListSubj = QueryEngine.doSearch(subject);
                }
                if (!object.equals("Object") && !object.equals("")) {
                    retListObj = QueryEngine.doSearch(object);
                }
            } else {// This is advanced search mode. where the system tries to predict the best matches based on
                    // the input combination

                webTupleProc = new WebTupleProcessor(pool, subject, object, predicate);
                // make a call to the Engine with the required parameters
                webTupleProc.processTuples(null);
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
            // if (retListPredLookUp.size() > 0) {
            request.setAttribute("matchingListPredLookup", retListPredLookUp);
            // }
            // if (retListPredSearch.size() > 0) {
            request.setAttribute("matchingListPredSearch", retListPredSearch);
            // }

            // for resetting the text boxes
            request.setAttribute("subject", subject);
            request.setAttribute("predicate", predicate);
            request.setAttribute("object", object);
            request.setAttribute("topk", topK);

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
        // Put your code here
    }

}
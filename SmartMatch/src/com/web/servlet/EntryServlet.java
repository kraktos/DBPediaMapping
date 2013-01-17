package com.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mapper.relationMatcher.ResultDAO;
import com.mapper.search.QueryEngine;
import com.mapper.utility.Constants;

/**
 * Servlet class to handle requests for testing the matching performance
 * 
 * @author Arnab Dutta
 */
public class EntryServlet extends HttpServlet
{

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

        List<ResultDAO> retListPred = new ArrayList<ResultDAO>();
        List<ResultDAO> retListObj = new ArrayList<ResultDAO>();
        List<ResultDAO> retListSubj = new ArrayList<ResultDAO>();

        List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();

        float score = Constants.SIMILARITY;
        int topK = Constants.TOPK;

        try {
            // receive the query terms over request
            if (request.getParameter("sim") != null) {
                score = Float.parseFloat(request.getParameter("sim"));
            }
            if (request.getParameter("topk") != null) {
                topK = Integer.parseInt(request.getParameter("topk"));
            }

            QueryEngine.setTopK(topK);
            QueryEngine.setSimilarity(score);

            String subject = request.getParameter("subject");
            String predicate = request.getParameter("predicate");
            String object = request.getParameter("object");

            // fetch the answer terms
            if (!subject.equals("Subject") && !object.equals("Object")) {
                retList = QueryEngine.performSearch(subject, object);
                retListSubj = retList.get(0);
                retListObj = retList.get(1);
            } else {
                if (!subject.equals("Subject")) {
                    retListSubj = QueryEngine.doSearch(subject);
                }
                if (!object.equals("Object")) {
                    retListObj = QueryEngine.doSearch(object);
                }
            }
            if (!predicate.equals("Predicate")) {
                retListPred = QueryEngine.doSearch(predicate);
            }

            // set the request parameter for results display
            if (retListSubj.size() > 0) {
                request.setAttribute("matchingListSubj", retListSubj);
            }
            if (retListObj.size() > 0) {
                request.setAttribute("matchingListObj", retListObj);
            }
            if (retListPred.size() > 0) {
                request.setAttribute("matchingListPred", retListPred);
            }

            // redirect to page
            request.getRequestDispatcher("entry.jsp").forward(request, response);

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

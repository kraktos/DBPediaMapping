
package de.dws.mapper.webInterface.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.dws.mapper.controller.WebTupleProcessor;
import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.engine.query.QueryEngine;
import de.dws.mapper.helper.dataObject.ResultDAO;
import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.helper.util.Utilities;
import de.dws.mapper.knowledgeBase.UncertainKB;
import de.dws.mapper.logic.FactSuggestion;
import de.dws.reasoner.axioms.AxiomCreator;
import de.dws.reasoner.inference.Inference;
import de.elog.Application;

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

    // list of resultsets to be displayed back on the UI
    List<ResultDAO> retListPredLookUp = new ArrayList<ResultDAO>();

    List<ResultDAO> retListPredSearch = new ArrayList<ResultDAO>();

    List<ResultDAO> retListObj = new ArrayList<ResultDAO>();

    List<ResultDAO> retListSubj = new ArrayList<ResultDAO>();

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

    // DB connection instance, one per servlet
    Connection connection = null;

    // prepared statement instance
    PreparedStatement pstmt = null;

    // instantiate a new KB
    UncertainKB uncertainKB = new UncertainKB();

    
    double conf = 0;
    
    /**
     * The doPost method of the servlet. <br>
     * This method is called when a form has its tag value method equals to
     * post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        // instance of Axiom Creator
        AxiomCreator axiomCreator = null;

        // instance of fact coming as NELL/Freeverb input
        SuggestedFactDAO uncertainFact = null;

        // list for suggested fact
        List<SuggestedFactDAO> retListSuggstFacts = new ArrayList<SuggestedFactDAO>();

        List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();

        // initialize with some default values
        int topK = Constants.TOPK;
        double sim = Constants.SIMILARITY;

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        String action = (request.getParameter("action") != null) ? request.getParameter("action")
                : "none";
        logger.info("|" + action + "|");

        try {

            if (!"".equals(action)) {

                // set topk attribute
                if (request.getParameter("topk") != null) {
                    topK = Integer.parseInt(request.getParameter("topk"));
                }
                QueryEngine.setTopK(topK);

                // set minimum similarity attribute
                if (request.getParameter("sim") != null) {
                    sim = Double.parseDouble(request.getParameter("sim"));
                }

                String subject = request.getParameter("subject").trim();
                String predicate = request.getParameter("predicate").trim();
                String object = request.getParameter("object").trim();

                if ("suggest".equals(action)) {

                    // these includes both the uris and the scores.
                    String[] candidateSubjs = request.getParameterValues("checkboxSubjs");
                    String[] candidatePredLkUp = request.getParameterValues("checkboxPredLookup");
                    String[] candidatePredSearch = request.getParameterValues("checkboxPredSearch");
                    String[] candidateObjs = request.getParameterValues("checkboxObjs");

                    // need to create a list of ResultDAO from the
                    List<ResultDAO> subDaos = getResultDaos(candidateSubjs);
                    List<ResultDAO> predDaos = getResultDaos(candidatePredSearch);
                    List<ResultDAO> objDaos = getResultDaos(candidateObjs);

                    // ************** kernel density estimation process
                    // ************************************************************************************
                    // return a list of possible facts suggestion from best
                    // matches

                    /*
                     * retListSuggstFacts =
                     * FactSuggestion.suggestFact(candidateSubjs,
                     * candidatePredLkUp, candidatePredSearch, candidateObjs,
                     * sim);
                     */
                    // request.setAttribute("suggestedFactList",
                    // retListSuggstFacts);

                    // *************************************************************************************
                    // we want to validate that the ranking given by kernel
                    // density estimator are in tandem
                    // with the ranking provided by Elog reasoner. Here we feed
                    // the possible candidate matches to the reasoner

                    // send the suggestions to the reasoner module and create
                    // axioms

                    logger.info(conf + " second");
                    
                    uncertainFact = new SuggestedFactDAO(subject.replaceAll("\\s", ""),
                            predicate.replaceAll("\\s", ""), object.replaceAll("\\s", ""), conf ,
                            true);

                    // *************** create axioms
                    // **********************************************************************************

                    logger.info(" STARTING AXIOM CREATION ... ");

                    axiomCreator = new AxiomCreator();
                    axiomCreator.createOwlFromFacts(subDaos, predDaos,
                            objDaos, uncertainFact);

                    // **************** reason with Elog
                    // ************************************************************************************
                    String[] args = new String[4];
                    args[0] = "-sm";
                    args[1] = "-s1000000";
                    args[2] = "-i40";
                    args[3] = "/home/arnab/Workspaces/SchemaMapping/EntityLinker/data/ontology/output/assertions.owl";

                    logger.info(" \nSTARTING ELOG REASONER ... ");

                    Application.main(args);

                    // **************** run inference
                    // ************************************************************************************

                    logger.info(" STARTING INFERENCE BASED ON SAMPLED PROBABILITIES ... ");
                    args[0] = uncertainFact.getSubject();
                    args[1] = uncertainFact.getPredicate();
                    args[2] = uncertainFact.getObject();

                    Inference.main(args);

                } else {
                    String triple = getARandomTriple();
                    subject = triple.split(",")[1];
                    predicate = "birth place";
                    object = triple.split(",")[3];
                    conf = Double.valueOf(triple.split(",")[0]);

                    logger.info(conf + " first");
                    // declare class
                    WebTupleProcessor webTupleProc = new WebTupleProcessor(pool, subject, object,
                            predicate);

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
                request.setAttribute("sim", sim);

            } else {

                List<SuggestedFactDAO> listFacts = new ArrayList<SuggestedFactDAO>();

                // instance of the suggested DBPedia fact
                SuggestedFactDAO suggestedFact = null;

                String[] facts = request.getParameterValues("checkbox");

                for (String fact : facts) {
                    logger.info(fact);

                    String[] str = fact.split("~");

                    suggestedFact =
                            new SuggestedFactDAO(Utilities.prun(str[0]), Utilities.prun(str[1]),
                                    Utilities.prun(str[2]),
                                    Double.valueOf(str[3]), true);

                    // save it to the KB
                    // uncertainKB.createKB(connection, pstmt, suggestedFact);

                    // add to the set of suggested facts
                    listFacts.add(suggestedFact);
                }

                // retrieve the subject, predicate and predicates
                // this is the original text in natural language
                String subject = request.getParameter("subject").trim();
                String predicate = request.getParameter("predicate").trim();
                String object = request.getParameter("object").trim();

                uncertainFact = new SuggestedFactDAO(subject, predicate, object, .8, true);

                logger.info(subject + "  " + predicate + " " + object);

                // send the suggestions to the reasoner module and create axioms
                /*
                 * if (listFacts != null && listFacts.size() > 0) { axiomCreator
                 * = new AxiomCreator();
                 * axiomCreator.createOwlFromFacts(listFacts, uncertainFact); }
                 */

                facts = null;
            }

        }

        catch (Exception theException) {
            logger.error(theException.getMessage());
        }

        // redirect to page
        request.getRequestDispatcher("page/entry.jsp").forward(request, response);

    }

    private String getARandomTriple() throws FileNotFoundException {
        File f = new File("/home/arnab/Work/data/NELL/writerwasbornincity.csv");
        String result = null;
        Random rand = new Random();
        int n = 0;
        for (Scanner sc = new Scanner(f); sc.hasNext();)
        {
            ++n;
            String line = sc.nextLine();
            if (rand.nextInt(n) == 0)
                result = line;
        }

        // logger.info(result);
        return result;

    }

    private List<ResultDAO> getResultDaos(String[] candidateSubjs) {
        List<ResultDAO> retList = new ArrayList<ResultDAO>();

        for (String value : candidateSubjs) {
            String[] arg = value.split("~");
            retList.add(new ResultDAO(arg[0], Double.valueOf(arg[1])));
        }
        return retList;
    }

    /**
     * Initialization of the servlet. <br>
     * 
     * @throws ServletException if an error occurs
     */
    public void init() throws ServletException
    {
        try {
            // instantiate the DB connection
            DBConnection dbConnection = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbConnection.getConnection();

            // create a statement
            pstmt = connection.prepareStatement(Constants.INSERT_FACT_SQL);
        } catch (SQLException ex) {
            logger.error("Connection Failed! Check output console" + ex.getMessage());
        }
    }
}
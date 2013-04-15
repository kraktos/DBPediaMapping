
package de.dws.mapper.webInterface.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.dws.mapper.controller.WebTupleProcessor;
import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.engine.query.QueryEngine;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
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

    Map<String, List<String>> entityTypesMap = new HashMap<String, List<String>>();

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
                    List<ResultDAO> predDaos = (candidatePredSearch != null && candidatePredSearch.length > 0) ? getResultDaos(candidatePredSearch)
                            : getResultDaos(candidatePredLkUp);
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

                    uncertainFact = new SuggestedFactDAO(subject.replaceAll("\\s", ""),
                            predicate.replaceAll("\\s", ""), object.replaceAll("\\s", ""), conf,
                            true);

                    logger.info(uncertainFact.toString());

                    // **************** run inference
                    // ************************************************************************************

                    if (Constants.INFERENCE_MODE) {

                        // *************** create axioms
                        // **********************************************************************************

                        logger.info(" STARTING AXIOM CREATION ... ");
                        axiomCreator = new AxiomCreator();

                        logger.info(subDaos);
                        logger.info(objDaos);
                        logger.info(predDaos);

                        axiomCreator.createOwlFromFacts(subDaos, predDaos,
                                objDaos, uncertainFact, entityTypesMap);

                        axiomCreator.createOutput();

                        // *************************** REASONING
                        // ***************************
                        String[] args = new String[3];
                        args[0] = uncertainFact.getSubject();
                        args[1] = uncertainFact.getPredicate();
                        args[2] = uncertainFact.getObject();
                        Inference.main(args);

                    } else {

                        // ********** GOLD STANDARD CREATION
                        // ************************

                        logger.info(subDaos);
                        logger.info(predDaos);
                        logger.info(objDaos);

                        List<SuggestedFactDAO> listGoldFacts = new ArrayList<SuggestedFactDAO>();
                        SuggestedFactDAO goldFact = null;

                        for (ResultDAO subDao : subDaos) {
                            for (ResultDAO predDao : predDaos) {
                                for (ResultDAO objDao : objDaos) {
                                    // create a gold fact
                                    goldFact = new SuggestedFactDAO(subDao.getFieldURI(),
                                            predDao.getFieldURI(), objDao.getFieldURI(), 1D, true);

                                    // add to the list of facts
                                    listGoldFacts.add(goldFact);
                                }
                            }
                        }

                        // save the list of gold facts for the particular
                        // uncertain fact
                        // mostly, there will be one-to-one mapping . just in
                        // case user is not sure at all,
                        // we can have multiple possibilities. Implementation is
                        // done for now,

                        // save it to the KB
                        int returnType = uncertainKB.createKB(connection, pstmt, listGoldFacts,
                                uncertainFact);

                        if (returnType == 0)
                            logger.info(uncertainFact.toString() + " inserted successfully..");

                    }

                } else {
                    String triple = getARandomTriple();

                    subject = triple.split(",")[1];
                    predicate = triple.split(",")[2];
                    object = triple.split(",")[3];

                    if (shouldFlip(predicate)) {
                        subject = triple.split(",")[3];
                        object = triple.split(",")[1];
                    }

                    conf = Double.valueOf(triple.split(",")[0]);

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
                // specify the page that it is gold standard creation mode
                request.setAttribute("inference", String.valueOf(Constants.INFERENCE_MODE));

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

    private boolean shouldFlip(String predicate) {
        File file = new File(Constants.PREDICATE_FREQ_FILEPATH);
        Scanner sc;
        try {
            sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split("->");

                if (parts[0].contains(predicate)) {
                    return (parts[0].indexOf("-") != -1);
                }

            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private String getARandomTriple() throws FileNotFoundException {
        File f = new File(Constants.NELL_DOMAIN_INPUT_FILE_PATH);
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
        return result.toLowerCase();

    }

    /**
     * takes the list of possible matches and frames a list of ResultDao out of
     * it. Also, it creates a list of the types of the particular entity. E.g.
     * London -> ontology/PopulatedPlace, /ontology/Place, /ontology/Settlement,
     * /ontology/City
     * 
     * @param candidates
     * @return List of {@link ResultDAO}
     */
    private List<ResultDAO> getResultDaos(String[] candidates) {
        List<ResultDAO> retList = new ArrayList<ResultDAO>();

        ResultSet results = null;
        List<QuerySolution> listResults = null;
        String type = null;
        String entityUri = null;
        String[] arg = null;
        List<String> listTypes = null;

        // iterate the candidate array
        for (String value : candidates) {
            arg = value.split("~");
            entityUri = arg[0];

            // find the type of this entity
            results = SPARQLEndPointQueryAPI
                    .queryDBPediaEndPoint("select distinct ?val where {<" +
                            entityUri +
                            "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?val}");
            listResults = ResultSetFormatter.toList(results);

            // for a possible entity there can be multiple types, Person,
            // writer, agent etc
            for (QuerySolution querySol : listResults) {
                type = querySol.get("val").toString();
                if (type.startsWith("http://dbpedia.org/ontology/")) {
                    logger.info(type);
                    // if the key exists, add it to its list of type
                    if (entityTypesMap.containsKey(entityUri)) {
                        entityTypesMap.get(entityUri).add(type);
                    } else {
                        listTypes = new ArrayList<String>();
                        listTypes.add(type);
                        entityTypesMap.put(entityUri, listTypes);
                    }
                }
            }

            // logger.info(entityTypesMap);
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
            pstmt = connection.prepareStatement(Constants.INSERT_GOLD_STANDARD);
        } catch (SQLException ex) {
            logger.error("Connection Failed! Check output console" + ex.getMessage());
        }
    }
}

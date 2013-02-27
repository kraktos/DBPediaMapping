/**
 * 
 */
package com.uni.mannheim.dws.mapper.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.uni.mannheim.dws.mapper.dbConnectivity.DBConnection;
import com.uni.mannheim.dws.mapper.engine.query.QueryEngine;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.knowledgeBase.UncertainKB;
import com.uni.mannheim.dws.mapper.logic.FactSuggestion;

/**
 * This class tries to parse the tuples generated from NELL IE engine and processes them by each tuple see
 * {@link ITupleProcessor}
 * 
 * @author Arnab Dutta
 */
public class NellTupleProcessor implements ITupleProcessor
{

    // define Logger
    static Logger logger = Logger.getLogger(NellTupleProcessor.class.getName());

    // DB connection instance, one per servlet
    Connection connection = null;

    // prepared statement instance
    PreparedStatement pstmt = null;

    // instantiate a new KB
    UncertainKB uncertainKB = new UncertainKB();

    public NellTupleProcessor()
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

    /*
     * (non-Javadoc)
     * @see com.mapper.relationMatcher.TupleProcessor#processTuples(java.lang.String)
     */
    public void processTuples(String dataFilePath) throws IOException, InterruptedException, ExecutionException,
        SQLException
    {
        // open the file stream on the file
        BufferedReader tupleReader = new BufferedReader(new FileReader(dataFilePath));
        String[] strTokens = null;
        double aprioriProb = 0.0;
        String subject;
        String predicate;
        String object;

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        if (tupleReader != null) {
            String tupleFromIE;
            while ((tupleFromIE = tupleReader.readLine()) != null) {
                // process with each of these tuples
                strTokens = tupleFromIE.split(Constants.NELL_IE_DELIMIT);
                // strtokens[0] => Subject
                // strtokens[1] => predicate
                // strtokens[2] => object
                // strtokens[3] => confidence

                // extract the subject, predicate and object out of each tuple
                aprioriProb = (strTokens[0] != null) ? Double.parseDouble(strTokens[0]) : 0.0;
                subject = (strTokens[1] != null) ? strTokens[1] : "";
                predicate = (strTokens[2] != null) ? strTokens[2] : "";
                object =
                    (strTokens[3] != null & strTokens[3].length() > 0) ? strTokens[3] : ((strTokens[4] != null)
                        ? strTokens[4] : "");

                logger.info(subject + " | " + predicate + " | " + object + " | " + aprioriProb);

                // fetch the equivalent DBPedia entities
                List<List<ResultDAO>> retList = QueryEngine.performSearch(pool, subject, object);
                List<ResultDAO> retListSubj = retList.get(0);
                List<ResultDAO> retListObj = retList.get(1);

                // use them to fetch the predicates they are linked with
                /*
                 * QueryEngine.fetchPredicates(retList.get(0),retList.get(1), predicate); System.out.print("\n\n");
                 */

                // create File object of our index directory. this is the property index directory
                File file = new File(Constants.DBPEDIA_PROP_INDEX_DIR);

                List<ResultDAO> retListPredLookUp = QueryEngine.doLookUpSearch(predicate);
                List<ResultDAO> retListPredSearch = QueryEngine.doSearch(predicate, file);

                // return a list of possible facts suggestion from best matches
                List<SuggestedFactDAO> retListSuggstFacts =
                    FactSuggestion.suggestFact(retListSubj, retListPredLookUp, retListPredSearch, retListObj, 0);

                for (SuggestedFactDAO fact : retListSuggstFacts) {
                    logger.debug(fact.toString());
                    // save it to the KB
                    uncertainKB.createKB(connection, pstmt, new SuggestedFactDAO(fact.getSubject(),
                        fact.getPredicate(), fact.getObject(), new Double(aprioriProb), true));
                }
            }
        }
    }

    /**
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, SQLException
    {
        // new ReVerbTupleProcessor().processTuples(Constants.IE_TUPLES_PATH);
        new NellTupleProcessor().processTuples(Constants.NELL_DATA_PATH);
        System.exit(1);
    }

}

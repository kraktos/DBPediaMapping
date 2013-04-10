/**
 * 
 */

package de.dws.mapper.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.engine.query.QueryEngine;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import de.dws.mapper.helper.dataObject.ResultDAO;
import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.knowledgeBase.UncertainKB;
import de.dws.mapper.logic.FactSuggestion;
import de.dws.reasoner.axioms.AxiomCreator;
import de.dws.reasoner.inference.Inference;
import de.elog.Application;

/**
 * This class tries to parse the tuples generated from NELL IE engine and
 * processes them by each tuple see {@link ITupleProcessor}
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

    Map<String, List<String>> entityTypesMap = new HashMap<String, List<String>>();

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
     * @see
     * com.mapper.relationMatcher.TupleProcessor#processTuples(java.lang.String)
     */
    public void processTuples(String dataFilePath) throws IOException, InterruptedException,
            ExecutionException,
            SQLException
    {
        // instance of Axiom Creator
        AxiomCreator axiomCreator = null;

        // instance of fact coming as NELL/Freeverb input
        SuggestedFactDAO uncertainFact = null;

        // open the file stream on the file
        BufferedReader tupleReader = new BufferedReader(new FileReader(dataFilePath));
        String[] strTokens = null;
        double aprioriProb = 0.0;
        String subject;
        String predicate;
        String object;

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        /*
         * try { axiomCreator = new AxiomCreator(); } catch
         * (OWLOntologyCreationException e) {
         * logger.error("Error while creating ontology.." + e.getMessage()); }
         */

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
                        (strTokens[3] != null & strTokens[3].length() > 0) ? strTokens[3]
                                : ((strTokens[4] != null)
                                        ? strTokens[4] : "");

                logger.info(subject + " | " + predicate + " | " + object + " | " + aprioriProb);

                // fetch the equivalent DBPedia entities
                List<List<ResultDAO>> retList = QueryEngine.performSearch(pool, subject, object);
                List<ResultDAO> retListSubj = retList.get(0);
                List<ResultDAO> retListObj = retList.get(1);

                // use them to fetch the predicates they are linked with
                /*
                 * QueryEngine.fetchPredicates(retList.get(0),retList.get(1),
                 * predicate); System.out.print("\n\n");
                 */

                // create File object of our index directory. this is the
                // property index directory
                File file = new File(Constants.DBPEDIA_PROP_INDEX_DIR);

                List<ResultDAO> retListPredLookUp = QueryEngine.doLookUpSearch(predicate);
                List<ResultDAO> retListPredSearch = QueryEngine.doSearch(predicate, file);

                uncertainFact = new SuggestedFactDAO(subject.replaceAll("\\s", ""),
                        predicate.replaceAll("\\s", ""), object.replaceAll("\\s", ""), aprioriProb,
                        true);

                // logger.info(retListSubj);
                // logger.info(retListObj);
                // logger.info(retListPredLookUp);

                createTypes(retListSubj);
                createTypes(retListPredLookUp);
                createTypes(retListObj);

                // *************** create axioms
                // **********************************************************************************


                logger.info(" STARTING AXIOM CREATION ... ");
                try {
                    axiomCreator = new AxiomCreator();

                    axiomCreator.createOwlFromFacts(retListSubj, retListPredLookUp,
                            retListObj, uncertainFact, entityTypesMap);
                } catch (OWLOntologyCreationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // **************** reason with Elog
                // ************************************************************************************
                String[] args = new String[4];
                args[0] = "-sm";
                args[1] = "-s1000000";
                args[2] = "-i20";
                args[3] = "/home/arnab/Workspaces/SchemaMapping/EntityLinker/data/ontology/output/assertions.owl";
                logger.info(" \nSTARTING ELOG REASONER ... ");
                try {
                    Application.main(args);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                
                // return a list of possible facts suggestion from best matches
                /*
                 * List<SuggestedFactDAO> retListSuggstFacts =
                 * FactSuggestion.suggestFact(retListSubj, retListPredLookUp,
                 * retListPredSearch, retListObj, 0);
                 */

                /*
                 * for (SuggestedFactDAO fact : retListSuggstFacts) {
                 * logger.debug(fact.toString()); // save it to the KB
                 * uncertainKB.createKB(connection, pstmt, new
                 * SuggestedFactDAO(fact.getSubject(), fact.getPredicate(),
                 * fact.getObject(), new Double(aprioriProb), true)); }
                 */

            } // end of while

        }
    }

    private void createTypes(List<ResultDAO> retListSubj) {

        String entityUri = null;
        ResultSet results = null;
        List<QuerySolution> listResults = null;
        String type = null;
        List<String> listTypes = null;

        // iterate the list of result daos and find the uri type
        for (ResultDAO dao : retListSubj) {
            entityUri = dao.getFieldURI();

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
                    // logger.info(type);
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
        }
    }

    /**
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, InterruptedException,
            ExecutionException, SQLException
    {
        // new ReVerbTupleProcessor().processTuples(Constants.IE_TUPLES_PATH);
        createRandomTriplesFile();
        new NellTupleProcessor().processTuples(Constants.NELL_RANDOM_TRIPLE_DATA_SET);
        System.exit(1);
    }

    /**
     * creates a set of random triples
     * 
     * @throws IOException
     */
    private static void createRandomTriplesFile() throws IOException {

        File file = new File(Constants.NELL_RANDOM_TRIPLE_DATA_SET);

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        int cnt = 0;
        while (cnt++ < Constants.RANDOM_TRIPLES_LIMIT) {
            bw.write(getARandomTriple());

        }

        bw.close();

        System.out.println("Done");
    }

    /**
     * scans the nell output file and gets a random triple
     * 
     * @return a random triple from the file
     * @throws FileNotFoundException
     */
    private static String getARandomTriple() throws FileNotFoundException {
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

        logger.info(result);
        return result + "\n";

    }

}

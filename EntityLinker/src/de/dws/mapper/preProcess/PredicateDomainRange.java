/**
 * 
 */
package de.dws.mapper.preProcess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import de.dws.helper.util.Constants;
import de.dws.mapper.dbConnectivity.DBConnection;

/**
 * This class generates the domain and range of a property in DBPedia. For instance, the property "/ontology/spouse" has
 * owl:Person as its domain and range as well. For other properties it may be owl:Location, owl:Film etc
 * 
 * @author Arnab Dutta
 */
public class PredicateDomainRange
{
    private static final String LOCAL_DELIMITER = "~";

    /**
     * logger
     */
    static Logger logger = Logger.getLogger(PredicateDomainRange.class.getName());

    /*
     * query to fetch all data type property from DBPedia
     */
    private static String OWL_DATA_TYPE_PROPERTY = "select ?prop where "
        + "{?prop a <http://www.w3.org/2002/07/owl#DatatypeProperty>} order by ?prop";

    /*
     * query to fetch all object type property from DBPedia
     */
    private static String OWL_OBJECT_TYPE_PROPERTY = "select ?prop where "
        + "{?prop a <http://www.w3.org/2002/07/owl#ObjectProperty>} order by ?prop";

    /**
     * holds the {@link ResultSet} returned after making a query to the DBPedia endpoint
     */
    static List<QuerySolution> listResults;

    /**
     * {@link QueryExecution} instance
     */
    static QueryExecution qexec;

    /**
     * holds the result set
     */
    static ResultSet results;

    /**
     * predicate we want to find the domain and range restrictions
     */
    private static String predicate;

    /**
     * domain of the predicate
     */
    private static String domain;

    /**
     * range of the predicate
     */
    private static String range;

    /**
     * holds the set of predicates fetched from DBPedia
     */
    static Set<String> setPredicates = new HashSet<String>();

    /**
     * holds the set of domain range combinations for a predicate
     */
    static Set<String> setDomainsRanges = new HashSet<String>();

    /**
     * DB connection instance, one per servlet
     */
    static Connection connection = null;

    /**
     * prepared statement instance
     */
    static PreparedStatement pstmt = null;

    /**
     * flag to work only on data type property. Warning : If you are setting this to true, make sure you are using
     * OWL_DATA_TYPE_PROPERTY and not OWL_OBJECT_TYPE_PROPERTY. With the current setting no alteration required
     */
    private static boolean isDataTypeProp = false;

    public static void main(String[] args) throws Exception
    {
        // fetch the properties from DBPedia
        queryDBPedia(OWL_OBJECT_TYPE_PROPERTY);

        // Use those predicates to fetch the domain and range values
        computePredicateDomainRange();

        // write to the DB
        writeToDB();

    }

    /**
     * writes to the Database
     * 
     * @throws Exception
     */
    private static void writeToDB() throws Exception
    {
        logger.info("Writing to DB..");
        DBConnection dbConnection = null;

        try {
            // instantiate the DB connection
            dbConnection = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbConnection.getConnection();

            // create a statement
            pstmt = connection.prepareStatement(Constants.INSERT_PROPERTY_DOMAIN_RANGE_SQL);

            // set autocommit to false
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            logger.error("DB Connection creation error" + ex.getMessage());
            throw new Exception(ex);
        }

        try {
            int batchCounter = 0;
            // iterate the set of results and insert in batch
            for (String str : setDomainsRanges) {
                String[] values = str.split(LOCAL_DELIMITER);

                pstmt.setString(1, values[0]);
                pstmt.setString(2, values[1]); // set input parameter 2
                pstmt.setString(3, values[2]); // set input parameter 3

                pstmt.addBatch();

                if (batchCounter++ % Constants.BATCH_SIZE == 0) { // batches of 100 are flushed at a time
                    // execute batch update
                    pstmt.executeBatch();
                    connection.commit();

                    logger.info("FLUSHED TO DB...");
                }
            }

            // residual updates
            // execute batch update
            pstmt.executeBatch();

            // finally commit the transaction
            connection.commit();

            // close the database finally
            dbConnection.shutDown();

        } catch (SQLException ex) {
            logger.error(" record exists  !!");
        } finally {

            // clear up the locally used collections
            setDomainsRanges.clear();
            setPredicates.clear();
            setDomainsRanges = null;
            setPredicates = null;
        }

    }

    /**
     * iterate the predicates and fetch their domain and range values
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    private static void computePredicateDomainRange() throws IOException, InterruptedException
    {
        String instanceCount = null;

        logger.info("Iterating the predicates..");
        // iterate the set of predicates to find its domain and range
        for (String predicate : setPredicates) {

            // first check if at all there are any predicate instances. DBPedia has several such cases. No point moving
            // further if we have actually no entities linked by the predicate
            // query to select if instances exist
            String countInstances = "select (count(*) as ?num) where {?sub <" + predicate + "> ?obj. }";

            getResultSet(countInstances);

            for (QuerySolution querySol : listResults) {
                instanceCount = querySol.get("num").toString();
                instanceCount = instanceCount.substring(0, instanceCount.indexOf("^^"));
            }

            if (!instanceCount.equals("0")) {
                // make a new query to fetch the domain and range for this predicate
                String query =
                    "select distinct ?domain ?range where {<" + predicate
                        + "> <http://www.w3.org/2000/01/rdf-schema#domain> ?domain. <" + predicate
                        + "> <http://www.w3.org/2000/01/rdf-schema#range> ?range. }";

                executeQuery(query, predicate);

                // give some pause
                Thread.sleep(500);
            }

        }
    }

    /**
     * DBPedia has two broad property types: owl:Property (more authentic)[may be functional, data type or object
     * property] and rdf:Property We need to handle these two types differently, since the former has distinctly domain
     * and range values while the later does'nt
     * 
     * @param queryInput the input query
     * @param predicate the predicate for which the domain range we need to find out
     * @throws IOException
     */
    private static void executeQuery(String queryInput, String predicate) throws IOException
    {
        try {

            // frame the result set
            getResultSet(queryInput);

            // Debug break point, remove later
            if (predicate.indexOf("http://dbpedia.org/ontology/thumbnail") != -1)
                logger.info("");

            // sometimes the domain/range are still not available,
            // unusual case where either of them is absent
            if (listResults.size() == 0) {

                if (isDataTypeProp) { // These are data properties with either of the domain/range missing
                    queryInput =
                        "select distinct ?domain (datatype(?obj) as ?range) where {?sub <"
                            + predicate
                            + ">  ?obj. ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?smthng. "
                            + "?smthng <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class>. "
                            + "?smthng <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?domain}";
                    logger.info(queryInput);
                    // re frame the result set
                    getResultSet(queryInput);
                    for (QuerySolution querySol : listResults) {
                        try {
                            domain = querySol.get("domain").toString();
                            range = querySol.get("range").toString();
                        } catch (Exception e) {
                            range = "http://www.w3.org/2001/XMLSchema#string";
                            // setting them to string
                            setDomainsRanges.add(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                        }
                        if (domain.startsWith(Constants.DBPEDIA_HEADER)) { // set in the collection
                            setDomainsRanges.add(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                        }
                    }
                } else { // These are object properties with either of the domain/range missing
                    logger.info("Processing " + predicate);

                    // temporary variables
                    List<QuerySolution> domainResults = null;
                    List<QuerySolution> rangeResults = null;

                    queryInput =
                        "SELECT ?domain ?range WHERE { OPTIONAL {<" + predicate
                            + "> <http://www.w3.org/2000/01/rdf-schema#domain> ?domain } . " + "OPTIONAL{<" + predicate
                            + "> <http://www.w3.org/2000/01/rdf-schema#range> ?range} }";
                    getResultSet(queryInput);

                    if (listResults.size() > 1) {
                        for (QuerySolution querySol : listResults) {
                            try { // check if domain available
                                domain = querySol.get("domain").toString();
                            } catch (Exception e) {
                            }
                            // domain missing...try other way
                            // figure out the domain from the instance level information
                            queryInput =
                                "select distinct ?domain where {?sub <" + predicate
                                    + ">  ?obj. ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?smthng. "
                                    + "?smthng <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?domain}";
                            getResultSet(queryInput);
                            // keep the possible set of domains
                            domainResults = listResults; // may be a more than one domains

                            try { // check if range is available
                                range = querySol.get("range").toString();
                            } catch (Exception e) {
                                // range missing...try other way // figure out the range from the instance level
                                // information
                                queryInput =
                                    "select distinct ?range where {?sub <" + predicate
                                        + ">  ?obj. ?obj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?smthng. "
                                        + "?smthng <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?range. }";
                                getResultSet(queryInput);
                                // keep the possible set of ranges
                                rangeResults = listResults;
                            }
                        }

                        // Now let's see what domain, range information we collected
                        if (rangeResults != null) {
                            for (QuerySolution ranges : rangeResults) {
                                range = ranges.get("range").toString();
                                if (domain.startsWith(Constants.DBPEDIA_HEADER)
                                    && !domain.startsWith(Constants.YAGO_HEADER)
                                    && range.startsWith(Constants.DBPEDIA_HEADER)
                                    && !range.startsWith(Constants.YAGO_HEADER)) {
                                    // set in the collection
                                    setDomainsRanges
                                        .add(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                                    logger.info(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                                }
                            }
                        }
                        if (domainResults != null) {
                            for (QuerySolution domains : domainResults) {
                                domain = domains.get("domain").toString();
                                if (domain.startsWith(Constants.DBPEDIA_HEADER)
                                    && !domain.startsWith(Constants.YAGO_HEADER)
                                    && range.startsWith(Constants.DBPEDIA_HEADER)
                                    && !range.startsWith(Constants.YAGO_HEADER)) { // set
                                                                                   // in
                                                                                   // the
                                                                                   // collection
                                    setDomainsRanges
                                        .add(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                                    logger.info(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                                }
                            }
                        }
                    }
                    // There may be also some predicates, who have no instances at all. (For e.g //
                    // /ontology/giniCoefficientCategory) // Those cases are taken care of and the algorithm runs
                    // normally }

                }
            } else { // normal case where both domain and range present, straight forward, not much hassle here

                for (QuerySolution querySol : listResults) {
                    domain = querySol.get("domain").toString();
                    range = querySol.get("range").toString();
                    if (domain.startsWith(Constants.DBPEDIA_HEADER) && !domain.startsWith(Constants.YAGO_HEADER)
                        && range.startsWith(Constants.DBPEDIA_HEADER) && !range.startsWith(Constants.YAGO_HEADER)) {
                        // set in the collection
                        setDomainsRanges.add(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                        logger.info(predicate + LOCAL_DELIMITER + domain + LOCAL_DELIMITER + range);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Something went wrong for " + predicate + " " + e.getMessage());
        }
    }

    /**
     * helper method to fetch the result set for a given query
     * 
     * @param queryInput input query
     */
    public static void getResultSet(String queryInput)
    {
        // reset the list result
        listResults = null;

        Query query = QueryFactory.create(queryInput);
        // execute the query
        qexec = QueryExecutionFactory.sparqlService(Constants.DBPEDIA_SPARQL_ENDPOINT, query);

        // get the result set ResultSet
        results = qexec.execSelect();

        listResults = ResultSetFormatter.toList(results);
        qexec.close();
    }

    /**
     * Method queries against DBPedia to find the domain range values of the predicate
     * 
     * @param QUERY issued query to find the date/integer differences
     * @throws IOException
     */
    private static void queryDBPedia(final String QUERY) throws IOException
    {
        getResultSet(QUERY);

        for (QuerySolution querySol : listResults) {

            predicate = querySol.get("prop").toString();

            // just take only DBPedia properties, ignore others with namespace as "http://xmlns.com/foaf" or
            // "http://sw.opencyc.org"
            if (predicate.startsWith("http://dbpedia.org")) {
                if (!setPredicates.contains(predicate)) {
                    setPredicates.add(predicate);
                }
            }
        }
        qexec.close();
    }

}

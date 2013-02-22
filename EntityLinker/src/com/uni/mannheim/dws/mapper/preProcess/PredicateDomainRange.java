/**
 * 
 */
package com.uni.mannheim.dws.mapper.preProcess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
import com.uni.mannheim.dws.mapper.helper.util.Constants;

/**
 * This class generates the domain and range of a property in DBPedia. For instance, the property "/ontology/spouse" has
 * owl:Person as its domain and range as well. For other properties it may be owl:Location, owl:Film etc
 * 
 * @author Arnab Dutta
 */
public class PredicateDomainRange
{
    /**
     * logger
     */
    static Logger logger = Logger.getLogger(PredicateDomainRange.class.getName());

    /*
     * query to fetch all predicates from DBPedia
     */
    private static String OWL_PROPERTY = "select ?prop where "
        + "{?prop a <http://www.w3.org/2002/07/owl#DatatypeProperty>} order by ?prop";

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
     * domain of the predicate
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

    public static void main(String[] args) throws IOException, InterruptedException
    {
        FileWriter fstream = new FileWriter(Constants.DBPEDIA_PREDICATE_DISTRIBUTION + "/out.csv");
        BufferedWriter out = new BufferedWriter(fstream);

        // fetch the properties from DBPedia
        queryDBPedia(OWL_PROPERTY, out);

        // close output stream after file writing
        out.close();

        // Use those predicates to fetch the domain and range values
        computePredicateDomainRange();

        // create a file output stream
        BufferedWriter fileOut =
            new BufferedWriter(new FileWriter(Constants.DBPEDIA_PREDICATE_DISTRIBUTION + "/domainRange.csv"));

        // iterate and flush it to the file mentioned above
        for (String str : setDomainsRanges) {
            fileOut.write(str + "\n");
        }
        // close the stream
        fileOut.close();
    }

    /**
     * iterate the predicates and fetch their domain and range values
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    private static void computePredicateDomainRange() throws IOException, InterruptedException
    {

        // iterate the set of predicates to find its domain and range
        for (String predicate : setPredicates) {
            // make a new query to fetch the domain and range for this predicate
            String query =
                "select distinct ?domain ?range where {<" + predicate
                    + "> <http://www.w3.org/2000/01/rdf-schema#domain> ?smthng. <" + predicate
                    + "> <http://www.w3.org/2000/01/rdf-schema#range> ?range. "
                    + "?smthng <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?domain.}";

            //
            executeQuery(query, predicate);

            Thread.sleep(500);
            /*
             * if (count > 50) break;
             */
        }

        logger.info(setDomainsRanges.size());
    }

    /**
     * DBPedia has two broad property types: owl:DataTypeProperty (more authentic) and rdf:Property We need to handle
     * these two types differently, since the former has distinctly domain and range values while the later does'nt
     * 
     * @param queryInput the input query
     * @param predicate the predicate for which the domain range we need to find out
     * @throws IOException
     */
    private static void executeQuery(String queryInput, String predicate) throws IOException
    {
        try {
            getResultSet(queryInput);

            if (predicate.indexOf("productionStartYear") != -1)
                logger.info("");

            // sometimes the domain/range are still not available,
            // unusual case where either of them is absent
            if (listResults.size() == 0) {

                queryInput =
                    "select distinct ?domain (datatype(?obj) as ?range) where {?sub <"
                        + predicate
                        + ">  ?obj. ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?smthng. "
                        + "?smthng <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class>. "
                        + "?smthng <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?domain}";

                getResultSet(queryInput);

                for (QuerySolution querySol : listResults) {
                    domain = querySol.get("domain").toString();
                    range = querySol.get("range").toString();

                    // set in the collection
                    setDomainsRanges.add(predicate + ":" + domain + "," + range);
                }

            } else { // normal case where domain and range present

                for (QuerySolution querySol : listResults) {

                    domain = querySol.get("domain").toString();
                    range = querySol.get("range").toString();

                    // set in the collection
                    setDomainsRanges.add(predicate + ":" + domain + "," + range);
                    logger.info(domain + "," + predicate + "," + range + "\n");
                }
            }

        } catch (Exception e) {
            logger.error("Skipping bad range values");
        }
    }

    /**
     * helper method to fetch the result set for a given query
     * 
     * @param queryInput input query
     */
    public static void getResultSet(String queryInput)
    {
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
     * @param out output buffer where data is flushed to
     * @throws IOException
     */
    private static void queryDBPedia(final String QUERY, BufferedWriter out) throws IOException
    {
        getResultSet(QUERY);

        for (QuerySolution querySol : listResults) {

            predicate = querySol.get("prop").toString();

            // just take only DBPedia properties, ignore others with namespace as "http://xmlns.com/foaf" or
            // "http://sw.opencyc.org"
            if (predicate.startsWith("http://dbpedia.org")) {
                if (!setPredicates.contains(predicate)) {
                    setPredicates.add(predicate);
                    out.write(predicate + "\n");
                }
            }
        }
        // Close the output stream
        out.close();

        qexec.close();
    }

}

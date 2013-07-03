/**
 * 
 */

package de.dws.mapper.engine.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.reasoner.GenericConverter;

/**
 * @author Arnab Dutta
 */
public class SPARQLEndPointQueryAPI
{

    Logger logger = Logger.getLogger(SPARQLEndPointQueryAPI.class.getName());

    public static void queryDBPedia(final String QUERY)
    {

        Logger logger = Logger.getLogger(SPARQLEndPointQueryAPI.class.getName());

        String sparqlQueryString1 = QUERY;

        Query query = QueryFactory.create(sparqlQueryString1);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Constants.DBPEDIA_SPARQL_ENDPOINT, query);

        // get the result set
        ResultSet results = qexec.execSelect();

        @SuppressWarnings("unchecked")
        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        @SuppressWarnings("unchecked")
        List<String> listVarnames = results.getResultVars();

        for (QuerySolution querySol : listResults) {
            for (int indx = 0; indx < listVarnames.size();) {
                String key = querySol.get(listVarnames.get(indx++)).toString();
                String value = querySol.get(listVarnames.get(indx++)).toString();
                logger.info(key + "  " + value);
            }
        }

        qexec.close();
    }

    public static ResultSet queryDBPediaEndPoint(final String QUERY)
    {

        Query query = QueryFactory.create(QUERY);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Constants.DBPEDIA_SPARQL_ENDPOINT, query);

        // get the result set
        ResultSet results = qexec.execSelect();
        qexec.close();

        return results;
    }

    /**
     * get type of a given instance
     * 
     * @param inst instance
     * @return list of its type
     */
    public static List<String> getInstanceTypes(String inst) {
        List<String> result = new ArrayList<String>();
        String sparqlQuery = null;

        try {
            ResultSet results = null;
            sparqlQuery = "select ?val where{ <http://dbpedia.org/resource/" + inst
                    + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?val} ";

            // fetch the result set
            results = queryDBPediaEndPoint(sparqlQuery);
            List<QuerySolution> listResults = ResultSetFormatter.toList(results);

            for (QuerySolution querySol : listResults) {
                if (querySol.get("val").toString().indexOf(Constants.DBPEDIA_CONCEPT_NS) != -1)
                    result.add(Utilities.cleanDBpediaURI(querySol.get("val").toString()));
            }
        } catch (Exception e) {
            GenericConverter.logger.info("problem with " + sparqlQuery + " " + e.getMessage());
        }
        return result;
    }

    /**
     * get type of a given instance
     * 
     * @param inst instance
     * @return list of its type
     */
    public static List<String> getInstanceTypesAll(String inst) {
        List<String> result = new ArrayList<String>();
        String sparqlQuery = null;

        try {
            ResultSet results = null;
            sparqlQuery = "select ?val where{ <" + inst
                    + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?val} ";

            // fetch the result set
            results = queryDBPediaEndPoint(sparqlQuery);
            List<QuerySolution> listResults = ResultSetFormatter.toList(results);

            for (QuerySolution querySol : listResults) {
                if (querySol.get("val").toString().indexOf(Constants.DBPEDIA_CONCEPT_NS) != -1)
                    result.add(querySol.get("val").toString());
            }
        } catch (Exception e) {
            GenericConverter.logger.info("problem with " + sparqlQuery + " " + e.getMessage());
        }
        return result;
    }

} // end class

/**
 * 
 */
package com.uni.mannheim.dws.mapper.engine.query;

import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;


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
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

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
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

        // get the result set
        ResultSet results = qexec.execSelect();
        qexec.close();

        return results;
    }

    
} // end class
/**
 * 
 */
package com.mapper.query;

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
import com.mapper.indexer.DataIndexerImpl;
import com.mapper.utility.Hasher;

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

        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        List<String> listVarnames = results.getResultVars();
        // logger.info(" results var = " + results.getResultVars().toString());

        for (QuerySolution querySol : listResults) {
            for (int indx = 0; indx < listVarnames.size();) {
                String key = querySol.get(listVarnames.get(indx++)).toString();
                String value = querySol.get(listVarnames.get(indx++)).toString();
                logger.info(key + "  " + value);

                // addToMap(key, value);
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

    private static void addToMap(String key, String value)
    {

        List<Long> tempList;

        long propertyHash = Hasher.hash64(key);
        long labelHash = Hasher.hash64(value);

        // add the label names
        DataIndexerImpl.MAP_DBPEDIA_LITERALS.put(Long.valueOf(labelHash), value);

        if (DataIndexerImpl.MAP_PROPERTY_LABELS.containsKey(propertyHash)) {
            tempList = DataIndexerImpl.MAP_PROPERTY_LABELS.get(propertyHash);
            tempList.add(labelHash);
            DataIndexerImpl.MAP_PROPERTY_LABELS.put(propertyHash, tempList);
        } else {
            DataIndexerImpl.LIST_DBPEDIA_ENTITIES = new ArrayList<Long>();
            DataIndexerImpl.LIST_DBPEDIA_ENTITIES.add(labelHash);
            DataIndexerImpl.MAP_PROPERTY_LABELS.put(propertyHash, DataIndexerImpl.LIST_DBPEDIA_ENTITIES);
        }
    }
} // end class

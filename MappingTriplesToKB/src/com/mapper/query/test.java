package com.mapper.query;

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
import com.mapper.utility.Constants;

public class test
{
    static Logger logger = Logger.getLogger(test.class.getName());

    private static String QUERY = "select ?uri ?label " + "where { "
        + "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x. "
        + "?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label} limit 2000 offset ";

    static List<QuerySolution> listResults;

    static List<String> listVarnames;

    static Query query;

    static QueryExecution qexec;

    static ResultSet results;

    static Set<String> set = new HashSet<String>();

    static String key;

    static String value;

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {

        int factor = 28476001; // 5590001;//5580001;
        String s = "";

        FileWriter fstream = new FileWriter(Constants.DBPEDIA_DATA_DIR + "/out.csv");
        BufferedWriter out = new BufferedWriter(fstream);

        try {

            while (true) {
                s = QUERY + factor;
                logger.info(s);
                queryDBPedia(QUERY + factor, out);
                factor = factor + 2000;
                Thread.sleep(5000);

            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Close the output stream
        out.close();
    }

    public static void queryDBPedia(final String QUERY, BufferedWriter out)
    {

        Query query = QueryFactory.create(QUERY);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

        //http://dbpedia.openlinksw.com:8890/isparql
            
        // get the result set
        ResultSet results = qexec.execSelect();

        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        List<String> listVarnames = results.getResultVars();

        for (QuerySolution querySol : listResults) {
            for (int indx = 0; indx < listVarnames.size();) {

                if (querySol.get(listVarnames.get(indx)) != null) {
                    try {
                        key = querySol.get(listVarnames.get(indx++)).toString();
                        // System.out.println(querySol.toString()) ;
                        // if (querySol.get(listVarnames.get(indx++)) != null) {

                        if (key.startsWith("http://dbpedia.org")) {
                            value = querySol.get(listVarnames.get(indx++)).toString();
                            // System.out.println(key + "  ~!~ " + value);
                            value = (value.lastIndexOf("@") != -1) ? value.substring(0, value.lastIndexOf("@")) : value;
                            if (!set.contains(key + value)) {
                                set.add(key + value);
                                if (set.size() == 100000) {
                                    System.out.println("CLEARED");
                                    set.clear();
                                }
                                out.write(key + "~!~" + value + "\n");
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
                // }
            }
        }

        qexec.close();
    }

}

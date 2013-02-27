package com.uni.mannheim.dws.mapper.preProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.uni.mannheim.dws.mapper.dbConnectivity.DBConnection;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.helper.util.Utilities;
import com.uni.mannheim.dws.mapper.preProcess.estimator.KernelDensityEstimator;

public class PredicateDataDistribution
{
    /*
     * query to fetch all object type property from DBPedia
     */
    private static String OWL_OBJECT_TYPE_PROPERTY = "select ?prop where "
        + "{?prop a <http://www.w3.org/2002/07/owl#ObjectProperty>} order by ?prop";

    /**
     * holds the set of predicates fetched from DBPedia
     */
    static Set<String> setPredicates = new HashSet<String>();

    /**
     * logger
     */
    static Logger logger = Logger.getLogger(PredicateDataDistribution.class.getName());

    static List<QuerySolution> listResults;

    static QueryExecution qexec;

    static ResultSet results;

    static Set<Double> set = new HashSet<Double>();

    static String subVal;

    static String objVal;

    static String sub;

    static String obj;

    // Array used for storing the data values used as input to the estimator
    static List<Double> dataArr = new ArrayList<Double>();

    /**
     * statement instance
     */
    static java.sql.ResultSet resultSet = null;

    /**
     * @return the dataArr
     */
    public static Double[] getDataArr()
    {
        return dataArr.toArray(new Double[dataArr.size()]);
    }

    public void createPredicateDistribution() throws IOException, SQLException
    {

        for (String predicate : setPredicates) {
            logger.info(" fetching " + predicate);
            findDataDistribution(predicate);
        }

    }

    /**
     * collect the data from DBPedia, and frame a distribution of data values for the given property
     * 
     * @param mainProperty the actual property whose distribution we are trying to build
     * @throws IOException
     * @throws SQLException
     */
    public void findDataDistribution(String mainProperty) throws IOException, SQLException
    {
        int factor = 0;

        String domainPredicate = null;

        String rangePredicate = null;

        String queryString =
            "SELECT \"DOMAIN_PROP\", \"RANGE_PROP\" FROM \"PREDICATE_DOMAIN_RANGE\" " + "WHERE \"PREDICATE\" = '"
                + mainProperty + "';";

        String path = mainProperty.substring(mainProperty.lastIndexOf("/"), mainProperty.length());

        DBConnection dbConnection = new DBConnection();

        // set the statement instance
        dbConnection.setStatement(dbConnection.getConnection().createStatement());

        // fetch the result set
        resultSet = dbConnection.getResults(queryString);

        while (resultSet.next()) { // process results one row at a time
            domainPredicate = resultSet.getString(1);
            rangePredicate = resultSet.getString(2);

            // System.out.println("domainPredicate = " + domainPredicate);
            // System.out.println("rangePredicate = " + rangePredicate);
        }
        // close the result set
        resultSet.close();

        // shutdown database
        dbConnection.shutDown();

        // use the predicates to form a new query
        String QUERY =
            "select distinct * where {?sub <" + mainProperty + "> ?obj. ?sub <" + domainPredicate + "> ?subVal. "
                + "?obj <" + rangePredicate + "> ?objVal} ";

        if (domainPredicate != null && rangePredicate != null) {
            FileWriter fstream = new FileWriter(Constants.DBPEDIA_PREDICATE_DISTRIBUTION + path + ".csv");
            BufferedWriter out = new BufferedWriter(fstream);
            try {

                // logger.info(QUERY + factor);
                queryDBPedia(QUERY, out);
                // factor = factor + 2000;
                Thread.sleep(4000);

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            // close the file output stream
            out.close();
            logger.info("done writing to " + path + ".csv");
        }
    }

    public double findDensity(KernelDensityEstimator kde, double queryValue)
    {
        return kde.getEstimatedDensity(queryValue);
    }

    /**
     * read the data file and frame an array of data objects This serves as the input for the density estimator
     */
    public void frameDataArray()
    {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(Constants.DBPEDIA_PREDICATE_DISTRIBUTION + "/out.csv"));

            while ((line = br.readLine()) != null) {
                dataArr.add(Double.parseDouble(line));
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
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
    private static void getFromDBPedia(final String QUERY) throws IOException
    {
        getResultSet(QUERY);

        for (QuerySolution querySol : listResults) {

            String predicate = querySol.get("prop").toString();

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

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException
    {

        PredicateDataDistribution predicateDataDistribution = new PredicateDataDistribution();

        // fetch the properties from DBPedia
        getFromDBPedia(OWL_OBJECT_TYPE_PROPERTY);

        predicateDataDistribution.createPredicateDistribution();

    }

    /**
     * Method queries against DBPedia to find the date/integer values of the subject-object pairs which links these two
     * entities
     * 
     * @param QUERY issued query to find the date/integer differences
     * @param out output buffer
     * @throws IOException
     */
    public static void queryDBPedia(final String QUERY, BufferedWriter out)
    {
        Calendar calendar1 = new GregorianCalendar();
        Calendar calendar2 = new GregorianCalendar();
        double difference;

        try {
            Query query = QueryFactory.create(QUERY);

            // get the result set ResultSet
            QueryExecution qexec = QueryExecutionFactory.sparqlService(Constants.DBPEDIA_SPARQL_ENDPOINT, query);

            results = qexec.execSelect();

            List<QuerySolution> listResults = ResultSetFormatter.toList(results);

            for (QuerySolution querySol : listResults) {
                subVal = querySol.get("subVal").toString();
                objVal = querySol.get("objVal").toString();
                sub = querySol.get("sub").toString();
                obj = querySol.get("obj").toString();

                subVal = removeDataDefinition(subVal);
                objVal = removeDataDefinition(objVal);

                try {
                    Date year1Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(subVal);
                    Date year2Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(objVal);
                    calendar1.setTime(year1Date);
                    calendar2.setTime(year2Date);

                    // calculate the difference of years
                    difference = Math.abs(calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR));

                    // logger.info(sub + " " + subVal + " " + difference + " " + objVal + "  " + obj);
                    // flush it to the file
                    out.write(difference + "\n");

                } catch (ParseException e) {
                    continue;
                }
            }
            qexec.close();
        } catch (IOException e) {
            logger.error(" Error in executing query " + e.getMessage());
        }
    }

    /**
     * DBPedia returns date formatted with the XML type concated to it. This needs to be chopped off for further
     * processing
     * 
     * @param input input string to be formatted
     * @return formatted string
     */
    public static String removeDataDefinition(String input)
    {
        return input.substring(0, input.indexOf("^^"));
    }

}

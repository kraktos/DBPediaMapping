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
    /**
     * logger
     */
    static Logger logger = Logger.getLogger(PredicateDataDistribution.class.getName());

    static List<QuerySolution> listResults;

    static QueryExecution qexec;

    static ResultSet results;

    static Set<Double> set = new HashSet<Double>();

    static String year1;

    static String year2;

    static String yr1;

    static String yr2;

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
        // the predicate interested in
        String predicate = "http://dbpedia.org/ontology/commandStructure";

        findDataDistribution(predicate);

    }

    /**
     * collect the data from DBPedia, and frame a distribution of data values for the given property
     * 
     * @param predicate
     * @throws IOException
     * @throws SQLException
     */
    public void findDataDistribution(String predicate) throws IOException, SQLException
    {
        int factor = 0;

        String queryString =
            "SELECT \"DOMAIN_PROP\", \"RANGE_PROP\" FROM \"PREDICATE_DOMAIN_RANGE\" " + "WHERE \"PREDICATE\" = '"
                + predicate + "';";

        String path = predicate.substring(predicate.lastIndexOf("/"), predicate.length());

        DBConnection dbConnection = new DBConnection();

        // set the statement instance
        dbConnection.setStatement(dbConnection.getConnection().createStatement());

        resultSet = dbConnection.getResults(queryString);

        while (resultSet.next()) { // process results one row at a time
            String domainPredicate = resultSet.getString(1);
            String rangePredicate = resultSet.getString(2);

            System.out.println("domainPredicate = " + domainPredicate);
            System.out.println("rangePredicate = " + rangePredicate);
        }

        // close the result set
        resultSet.close();

        // shutdown data base
        dbConnection.shutDown();

        /*
         * FileWriter fstream = new FileWriter(Constants.DBPEDIA_PREDICATE_DISTRIBUTION + path + ".csv"); BufferedWriter
         * out = new BufferedWriter(fstream); String QUERY =
         * "select ?a ?year2 ?s ?year1 where {?a <http://dbpedia.org/ontology/spouse> ?s. ?s <http://dbpedia.org/ontology/birthDate> ?year1. "
         * + "?a <http://dbpedia.org/ontology/birthDate> ?year2} limit 2000 offset "; try { while (true) {
         * logger.info(QUERY + factor); queryDBPedia(QUERY + factor, out); factor = factor + 2000; Thread.sleep(4000);
         * if (factor > 30000) break; } } catch (Exception e) { logger.info(e.getMessage()); } // Close the output
         * stream out.close();
         */
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
     * @param args
     * @throws InterruptedException
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException
    {

        PredicateDataDistribution predicateDataDistribution = new PredicateDataDistribution();
        predicateDataDistribution.createPredicateDistribution();

        /*
         * predicateDataDistribution.frameDataArray(); long start = Utilities.startTimer(); KernelDensityEstimator kde =
         * new KernelDensityEstimator(getDataArr()); logger.info(" Data Range is " + kde.getMinValue() + " -> " +
         * kde.getMaxValue() + " out of " + getDataArr().length + " elements"); logger.info("Density Estimate at " + 80
         * + " = " + predicateDataDistribution.findDensity(kde, 80)); logger.info("Density Estimate at " + 5 + " = " +
         * predicateDataDistribution.findDensity(kde, 5)); Utilities.endTimer(start, "DENSITY ESTIMATED IN ");
         */
    }

    /**
     * Method queries against DBPedia to find the date/integer values of the subject-object pairs which links these two
     * entities
     * 
     * @param QUERY issued query to find the date/integer differences
     * @param out output buffer
     * @throws IOException
     */
    public static void queryDBPedia(final String QUERY, BufferedWriter out) throws IOException
    {
        Calendar calendar1 = new GregorianCalendar();
        Calendar calendar2 = new GregorianCalendar();
        double difference;

        Query query = QueryFactory.create(QUERY);

        // get the result set ResultSet
        QueryExecution qexec = QueryExecutionFactory.sparqlService(Constants.DBPEDIA_SPARQL_ENDPOINT, query);

        results = qexec.execSelect();

        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        for (QuerySolution querySol : listResults) {
            year1 = querySol.get("year1").toString();
            year2 = querySol.get("year2").toString();
            // yr1 = querySol.get("s").toString();
            // yr2 = querySol.get("a").toString();

            /*
             * if(yr1.indexOf("Henry_IV_of_France") != -1){ logger.info(year1); }
             */
            year1 = removeDataDefinition(year1);
            year2 = removeDataDefinition(year2);

            try {
                Date year1Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(year1);
                Date year2Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(year2);
                calendar1.setTime(year1Date);
                calendar2.setTime(year2Date);

                // calculate the difference of years
                difference = Math.abs(calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR));
                /*
                 * if (difference > 400) { logger.info(yr1 + "  " + calendar1.get(Calendar.YEAR) + "  " +
                 * calendar2.get(Calendar.YEAR) + " " + yr2); }
                 */

                // flush it to the file
                out.write(difference + "\n");

            } catch (ParseException e) {
                continue;
            }
        }
        qexec.close();
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

/**
 * 
 */
package com.uni.mannheim.dws.mapper.preProcess.estimator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.uni.mannheim.dws.mapper.dbConnectivity.DBConnection;
import com.uni.mannheim.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.helper.util.Utilities;

/**
 * This class is responsible for taking a predicate as input and fetching its density estimate, by referring the backend
 * list of its distribution
 * 
 * @author Arnab Dutta
 */
public class PredicateLikelihoodEstimate
{
    /**
     * logger
     */
    static Logger logger = Logger.getLogger(PredicateLikelihoodEstimate.class.getName());

    /**
     * Array used for storing the data values used as input to the estimator
     */
    static List<Double> dataArr = new ArrayList<Double>();

    /**
     * instance of {@link KernelDensityEstimator}
     */
    private static KernelDensityEstimator kde;

    private static void calculateLikelihood(String sub, String mainProperty, String obj)
    {
        String domainPredicate = null;
        String rangePredicate = null;

        // for the given property, create the data points distribution
        createDataPointDistribution(mainProperty.substring(mainProperty.lastIndexOf("/") + 1, mainProperty.length()));

        // initiate estimator with these data points freshly created
        kde = new KernelDensityEstimator(getDataArr());

        logger.info(" Data Range is " + kde.getMinValue() + " -> " + kde.getMaxValue() + " out of "
            + getDataArr().length + " elements");

        // frame the query to fetch the domain and range attributes to be used
        String queryString =
            "SELECT \"DOMAIN_PROP\", \"RANGE_PROP\" FROM \"PREDICATE_DOMAIN_RANGE\" " + "WHERE \"PREDICATE\" = '"
                + mainProperty + "'";

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {

        long start = Utilities.startTimer();

        createDataPointDistribution("doctoralAdvisor");

        // initiate estimator
        KernelDensityEstimator kde = new KernelDensityEstimator(getDataArr());

        logger.info(" Data Range is " + kde.getMinValue() + " -> " + kde.getMaxValue() + " out of "
            + getDataArr().length + " elements");

        String domainPredicate = null;

        String rangePredicate = null;

        String mainProperty = "http://dbpedia.org/ontology/spouse";

        String queryString =
            "SELECT \"DOMAIN_PROP\", \"RANGE_PROP\" FROM \"PREDICATE_DOMAIN_RANGE\" " + "WHERE \"PREDICATE\" = '"
                + mainProperty + "'";

        try {
            DBConnection dbConnection = new DBConnection();

            // set the statement instance
            dbConnection.setStatement(dbConnection.getConnection().createStatement());

            // fetch the result set
            java.sql.ResultSet resultSet = dbConnection.getResults(queryString);

            while (resultSet.next()) { // process results one row at a time
                domainPredicate = resultSet.getString(1);
                rangePredicate = resultSet.getString(2);
            }
            // close the result set
            resultSet.close();

            // shutdown database
            dbConnection.shutDown();

        } catch (SQLException e) {
            logger.error("Error finding domain range attributes for " + mainProperty + "  " + e.getMessage());
        }

        String sub = "http://dbpedia.org/resource/Eleanor_Powell";
        String obj = "http://dbpedia.org/resource/Glenn_Ford";// "http://dbpedia.org/resource/Lord_Byron";

        // 0.01281693147388699

        // use the predicates to form a new query
        String QUERY =
            "select distinct * where {<" + sub + "> <" + domainPredicate + "> ?subVal. " + "<" + obj + "> <"
                + rangePredicate + "> ?objVal} ";

        String subVal;
        String objVal;
        Calendar calendar1 = new GregorianCalendar();
        Calendar calendar2 = new GregorianCalendar();
        double difference;

        ResultSet results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(QUERY);
        List<QuerySolution> listResults = ResultSetFormatter.toList(results);
        for (QuerySolution querySol : listResults) {
            subVal = querySol.get("subVal").toString();
            objVal = querySol.get("objVal").toString();

            subVal = subVal.substring(0, subVal.indexOf("^^"));
            objVal = objVal.substring(0, objVal.indexOf("^^"));

            try {
                Date year1Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(subVal);
                Date year2Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(objVal);
                calendar1.setTime(year1Date);
                calendar2.setTime(year2Date);

                // calculate the difference of years
                difference = Math.abs(calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR));
                logger.info("Density Estimate at " + difference + " = " + kde.getEstimatedDensity(difference));

            } catch (ParseException e) {
                continue;
            }
        }

        Utilities.endTimer(start, "DENSITY ESTIMATED IN ");

    }

    /**
     * converts the {@link List} of data points to an {@link Double} array
     * 
     * @return the dataArr in an array
     */
    public static Double[] getDataArr()
    {
        return dataArr.toArray(new Double[dataArr.size()]);
    }

    /**
     * read the data file and frame an array of data objects This serves as the input for the density estimator
     */
    public static void createDataPointDistribution(String predicate)
    {
        BufferedReader buffReader = null;
        String line;
        try {
            buffReader =
                new BufferedReader(new FileReader(Constants.DBPEDIA_PREDICATE_DISTRIBUTION + "/" + predicate + ".csv"));

            while ((line = buffReader.readLine()) != null) {
                dataArr.add(Double.parseDouble(line));
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                buffReader.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public static List<SuggestedFactDAO> rankFacts(List<SuggestedFactDAO> retList)
    {
        String sub = null;
        String pred = null;
        String obj = null;

        for (SuggestedFactDAO factTriple : retList) {
            sub = factTriple.getSubject();
            pred = factTriple.getPredicate();
            obj = factTriple.getObject();

            logger.info(sub + " " + pred + " " + obj);

            calculateLikelihood(sub, pred, obj);
        }

        return retList;
    }

}

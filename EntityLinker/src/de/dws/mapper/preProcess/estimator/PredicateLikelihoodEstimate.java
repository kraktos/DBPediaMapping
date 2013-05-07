/**
 * 
 */

package de.dws.mapper.preProcess.estimator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import de.dws.helper.dataObject.SuggestedFactDAO;
import de.dws.helper.util.Constants;
import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;

/**
 * This class is responsible for taking a predicate as input and fetching its
 * density estimate, by referring the back end list of its distribution
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
    static KernelDensityEstimator kde;

    /**
     * attribute difference value
     */
    static Double difference;

    /**
     * estimate the density for the combination
     * 
     * @param sub Subject
     * @param mainProperty Predicate
     * @param obj Object
     * @return estimated density of this triple
     */
    private static Double estimateDensity(String sub, String mainProperty, String obj)
    {
        String domainPredicate = null;
        String rangePredicate = null;

        // frame the query to fetch the domain and range attributes to be used
        String queryString =
                "SELECT \"DOMAIN_PROP\", \"RANGE_PROP\" FROM \"PREDICATE_DOMAIN_RANGE\" "
                        + "WHERE \"PREDICATE\" = '"
                        + mainProperty + "'";

        // Try connecting to back end DB to fetch the domain and range predicate
        // attributes to query upon from DBPEdia
        try {
            DBConnection dbConnection = new DBConnection();

            // set the statement instance
            dbConnection.setStatement(dbConnection.getConnection().createStatement());

            // fetch the result set
            java.sql.ResultSet resultSet = dbConnection.getResults(queryString);

            if (resultSet != null) {
                while (resultSet.next()) { // process results one row at a time
                    domainPredicate = resultSet.getString(1);
                    rangePredicate = resultSet.getString(2);
                }

                // close the result set
                resultSet.close();
            }

            // shutdown database
            dbConnection.shutDown();

            // fetch data points if only we have some valid attributes for the
            // domain and range
            if (domainPredicate != null && rangePredicate != null) {

                // for the given property, create the data points distribution
                fetchDataDistribution(mainProperty.substring(mainProperty.lastIndexOf("/") + 1,
                        mainProperty.length()));

                // use the predicates, the subject and object to fetch the
                // integral value of difference on the predicate
                calculateAttributeDiff(sub, obj, domainPredicate, rangePredicate);

                if (difference != null) {
                    // only now we can initialize the estimator
                    kde = new KernelDensityEstimator(getDataArr());

                    return kde.getEstimatedDensity(difference);
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding domain range attributes for " + mainProperty + "  "
                    + e.getMessage());
        }

        return null;

    }

    /**
     * @param sub subject instance
     * @param obj object instance
     * @param domainPredicate the attribute of the subject on which we want to
     *            figure out the difference with object
     * @param rangePredicate the attribute of the object on which we want to
     *            figure out the difference with subject
     * @return {@link Double} value of the difference in attribute
     */
    public static Double calculateAttributeDiff(String sub, String obj, String domainPredicate,
            String rangePredicate)
    {
        // use the predicates to form a new query
        String QUERY = "select ?val where{  <" + sub + "> <" + domainPredicate + "> ?val}  ";

        String QUERY2 = "select ?val where{ <" + obj + "> <" + rangePredicate + "> ?val} ";

        String subVal = null;
        String objVal = null;

        Calendar calendar1 = new GregorianCalendar();
        Calendar calendar2 = new GregorianCalendar();

        ResultSet results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(QUERY);
        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        for (QuerySolution querySol : listResults) {
            subVal = querySol.get("val").toString();
        }

        ResultSet results2 = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(QUERY2);
        List<QuerySolution> listResults2 = ResultSetFormatter.toList(results2);

        for (QuerySolution querySol : listResults2) {
            objVal = querySol.get("val").toString();
        }

        try {
            subVal = subVal.substring(0, subVal.indexOf("^^"));
            objVal = objVal.substring(0, objVal.indexOf("^^"));

            Date year1Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(subVal);
            Date year2Date = new SimpleDateFormat("yyyy-dd-mm", Locale.ENGLISH).parse(objVal);
            calendar1.setTime(year1Date);
            calendar2.setTime(year2Date);

            // calculate the difference of years
            difference = new Double(Math.abs(calendar1.get(Calendar.YEAR)
                    - calendar2.get(Calendar.YEAR)));

            // compute the density estimate
            return difference;

        } catch (Exception e) {
            difference = null;
            return null;
        }

    }

    /**
     * converts the {@link List} of data points to an {@link Double} array
     * 
     * @return the dataArr in an array
     */
    private static Double[] getDataArr()
    {
        return dataArr.toArray(new Double[dataArr.size()]);
    }

    /**
     * read the data file and frame an array of data objects This serves as the
     * input for the density estimator
     * 
     * @param predicate
     */
    public static void fetchDataDistribution(String predicate)
    {
        BufferedReader buffReader = null;
        String line;
        try {
            buffReader =
                    new BufferedReader(new FileReader(Constants.DBPEDIA_PREDICATE_DISTRIBUTION
                            + "/" + predicate + ".csv"));

            // clear off all pre-existing any elements
            dataArr.clear();

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

    /**
     * this method takes a collection of possible facts and ranks them according
     * to validity, using the underlying kernel density estimator
     * 
     * @param listFacts {@link List} of {@link SuggestedFactDAO}
     * @return map of facts ranked on the density estimates
     */
    public static Map<Double, Set<SuggestedFactDAO>> rankFacts(List<SuggestedFactDAO> listFacts)
    {
        // local variables
        String sub = null;
        String pred = null;
        String obj = null;

        // the estimated density value
        Double densityEstimate = null;

        // define a map to store the ranked facts on a descending order of
        // densities, the one with the highest density
        // is the most likely one
        // and so a custom comparator needs to be constructed which reverses the
        // natural ordering of the TreeMap
        Map<Double, Set<SuggestedFactDAO>> mapRankedResults =
                new TreeMap<Double, Set<SuggestedFactDAO>>(new Comparator<Double>()
                {
                    public int compare(Double first, Double second)
                    {
                        return second.compareTo(first);
                    }
                });

        // iterate the return list of SuggestedFactDAO s
        for (SuggestedFactDAO factTriple : listFacts) {
            sub = factTriple.getSubject();
            pred = factTriple.getPredicate();
            obj = factTriple.getObject();

            // use the triples to extract the density estimate for this fact to
            // be valid
            densityEstimate = estimateDensity(sub, pred, obj);

            // if we have a proper density we add it to the list of possible
            // suggestion facts
            if (densityEstimate != null) {
                if (mapRankedResults.containsKey(densityEstimate)) {
                    try {
                        mapRankedResults.get(densityEstimate).add(factTriple);
                    } catch (Exception ex) {
                        continue;
                    }
                } else {
                    Set<SuggestedFactDAO> list = new TreeSet<SuggestedFactDAO>();
                    // update the confidence for the fact and add it back
                    factTriple.setConfidence(densityEstimate);
                    list.add(factTriple);
                    mapRankedResults.put(densityEstimate, list);
                }
            } else {
                // reset the value
                difference = null;
            }
        }
        return mapRankedResults;
    }

}

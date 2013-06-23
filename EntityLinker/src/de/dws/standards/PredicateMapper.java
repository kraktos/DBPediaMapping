/**
 * 
 */

package de.dws.standards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * This class is responsible for finding the ranked set of predicates for a
 * given NELL predicate
 * 
 * @author Arnab Dutta
 */
public class PredicateMapper {

    private static String DB_NAME = "goldStandardClean";

    private static final String GET_COOCC_PREDICATES_SQL = "select count(*) as cnt, D_PRED from " +
            DB_NAME +
            " where E_PRED =? group by D_PRED order by cnt desc";

    public static final String GET_SAMPLE = "select E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ from "
            +
            DB_NAME +
            " where E_PRED =? and D_PRED=? limit 2";

    private static final String GET_NELL_PREDICATES = "select E_PRED, count(*) as cnt from " +
            DB_NAME +
            " group by E_PRED order by cnt desc";

    // define Logger
    static Logger logger = Logger.getLogger(PredicateMapper.class.getName());

    /**
     * stores all the matched NELL predicates to DBPedia predicates
     */

    static Map<String, Long> ALL_NELL_PREDS = new HashMap<String, Long>();

    static Map<Pair<String, String>, Double> map = new HashMap<Pair<String, String>, Double>();

    static Map<String, Long> nellPredMap = new HashMap<String, Long>();

    static Map<String, Long> dbpPredicateMap = new HashMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("resources/log4j.properties");

        loadNELLPredicates();

        iteratePredicates();

    }

    /**
     * iterate the set of predicates to find the frequency of co-occurance with
     * other DBPedia predicates
     */
    private static void iteratePredicates() {

        Map<String, Long> rankedPredicates = null;

        String predicate = null;
        long total = 0;

        long instCount = 0;
        List<String> results = new ArrayList<String>();
        String[] arr = null;

        DBWrapper
                .init(GET_COOCC_PREDICATES_SQL);

        Pair<String, String> pair = null;

        for (Map.Entry<String, Long> entry : ALL_NELL_PREDS.entrySet()) {

            long predTotal = 0;

            predicate = entry.getKey();

            rankedPredicates = DBWrapper.getRankedPredicates(predicate);

            long val = 0;
            String dbpPred;

            for (Entry<String, Long> rankedVal : rankedPredicates.entrySet()) {

                pair = new Pair<String, String>(predicate, rankedVal.getKey());
                total = total + rankedVal.getValue();
                instCount = rankedVal.getValue();
                dbpPred = rankedVal.getKey();

                // logger.info(predicate + "\t" + dbpPred + "\t" +
                // instCount);

                if (instCount > 5) {
                    // predTotal = predTotal + instCount;
                    map.put(pair, new Double(instCount));

                    if (nellPredMap.containsKey(predicate)) {
                        val = nellPredMap.get(predicate);
                        nellPredMap.put(predicate, val + instCount);
                    } else {
                        nellPredMap.put(predicate, instCount);
                    }

                    if (dbpPredicateMap.containsKey(dbpPred)) {
                        val = dbpPredicateMap.get(dbpPred);
                        dbpPredicateMap.put(dbpPred, val + instCount);
                    } else {
                        dbpPredicateMap.put(dbpPred, instCount);
                    }
                }

                // find two instances of them
                // DBWrapper.init(GET_SAMPLE);

                // results = DBWrapper.getSampleInstances(predicate,
                // rankedVal.getKey());

                // for (String reString : results) {
                // arr = reString.split(DBWrapper.GS_DELIMITER);
                // logger.info(predicate + "\t" + rankedVal.getKey() + "\t" +
                // rankedVal.getValue());
                // }
            }

        }

        for (Entry<Pair<String, String>, Double> rankedVal : map.entrySet()) {

            long nellval = nellPredMap.get(rankedVal.getKey().getFirst());
            long dbpVal = dbpPredicateMap.get(rankedVal.getKey().getSecond());
            double joinedVal = rankedVal.getValue();
            System.out.println(rankedVal.getKey() + " " + joinedVal + ",  P(nell|dbp) = "
                    + joinedVal / (double) dbpVal + ",  P(dbp|nell) = " + joinedVal
                    / (double) nellval);

            // pair = new Pair<String, String>(predicate, rankedVal.getKey());
            // // update the map..
            // Double value = map.get(pair);
            // if (value != null) {
            // value = value / predTotal;
            // System.out.println(pair.getFirst() + "\t" + pair.getSecond() +
            // "\t" + value);
            // }
        }

        // for (Map.Entry<Pair<String, String>, Double> e : map.entrySet()) {
        // System.out.println(e.getKey().getFirst() + " " +
        // e.getKey().getSecond() + " "
        // + e.getValue());
        //
        // }

        DBWrapper.shutDown();
        System.out.println(total);
    }

    /**
     * Make DB call and load the NELL predicates
     */
    private static void loadNELLPredicates() {
        DBWrapper
                .init(GET_NELL_PREDICATES);

        DBWrapper.getAllNellPreds(ALL_NELL_PREDS);

        System.out.println("after fetching,,predicatres = " + ALL_NELL_PREDS.size());
    }

}

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

import de.dws.helper.util.Constants;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * This class is responsible for finding the ranked set of predicates for a
 * given NELL predicate
 * 
 * @author Arnab Dutta
 */
public class PredicateMapper {

    private static final String GET_SAMPLE = "select E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ from goldStandardClean where E_PRED =? and D_PRED=? limit 2";

    // define Logger
    static Logger logger = Logger.getLogger(PredicateMapper.class.getName());

    /**
     * stores all the matched NELL predicates to DBPedia predicates
     */
    static Map<String, Long> ALL_NELL_PREDS = new HashMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

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
        List<String> results = new ArrayList<String>();
        String[] arr = null;

        for (Map.Entry<String, Long> entry : ALL_NELL_PREDS.entrySet()) {
            predicate = entry.getKey();

            DBWrapper
                    .init(Constants.GET_COOCC_PREDICATES_SQL);

            rankedPredicates = DBWrapper.getRankedPredicates(predicate);

            for (Entry<String, Long> rankedVal : rankedPredicates.entrySet()) {
                total = total + rankedVal.getValue();
                // logger.info(predicate + "," + rankedVal.getKey() + "," +
                // rankedVal.getValue());

                // find two instances of them
                DBWrapper
                        .init(GET_SAMPLE);

                results = DBWrapper.getSampleInstances(predicate, rankedVal.getKey());

                for (String reString : results) {
                    arr = reString.split(DBWrapper.GS_DELIMITER);
                    logger.info(predicate + "\t" + rankedVal.getKey() + "\t" + rankedVal.getValue()
                            + "\t" +
                            arr[0] + "\t" + arr[1] + "\t" + arr[2] + "\t" + arr[3] + "\t" + arr[4]
                            + "\t" + arr[5]);
                }
                
                DBWrapper.shutDown();
            }

        }


        System.out.println(total);
    }

    /**
     * Make DB call and load the NELL predicates
     */
    private static void loadNELLPredicates() {
        DBWrapper
                .init(Constants.GET_NELL_PREDICATES);

        DBWrapper.getAllNellPreds(ALL_NELL_PREDS);
    }

}

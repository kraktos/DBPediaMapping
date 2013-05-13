/**
 * 
 */

package de.dws.standards;

import java.util.HashMap;
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

        Map<Long, String> rankedPredicates = null;

        DBWrapper
                .init(Constants.GET_COOCC_PREDICATES_SQL);

        String predicate = null;
        for (Map.Entry<String, Long> entry : ALL_NELL_PREDS.entrySet()) {
            predicate = entry.getKey();

            rankedPredicates = DBWrapper.getRankedPredicates(predicate);
            for (Entry<Long, String> rankedVal : rankedPredicates.entrySet()) {
                logger.info(predicate + "," + rankedVal.getValue() + "," + rankedVal.getKey());
            }
        }
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

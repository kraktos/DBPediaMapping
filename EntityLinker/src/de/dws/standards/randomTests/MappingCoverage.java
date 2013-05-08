/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class MappingCoverage {

    static Map<String, Long> ALL_NELL_PREDS = new HashMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        loadNELLPredicates();

    }

    private static void loadNELLPredicates() {
        DBWrapper
                .init("select predicate, count(*) as cnt from nell group by predicate order by cnt desc");

        DBWrapper.getAllNellPreds(ALL_NELL_PREDS);
        System.out.println(ALL_NELL_PREDS.size());
        fetchMatchedPreds(ALL_NELL_PREDS);

    }

    private static void fetchMatchedPreds(Map<String, Long> nellPredsMap) {
        String pred = null;
        long count = 0;
        long matchCount = 0;

        for (Entry<String, Long> entry : nellPredsMap.entrySet()) {
            pred = entry.getKey();
            count = entry.getValue();

            DBWrapper
                    .init("select count(*) from goldStandard where E_PRED = ?");
            // select predicate, count(*) as cnt from nell group by predicate
            // order by cnt desc

            matchCount = DBWrapper.findPredMatches(pred);

            System.out.println(pred + "," + count + "," + matchCount);
        }
    }

}

/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class MappingCoverage {

    private static final String NO_DUPLICATES_SQL = "select count(distinct E_SUB, E_PRED, E_OBJ) from goldStandard where E_PRED = ?";
    
    private static final String DUPLICATES_SQL = "select count(*) from goldStandard where E_PRED = ?";

    static Map<String, Long> ALL_NELL_PREDS = new TreeMap<String, Long>();

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
                    .init(MappingCoverage.NO_DUPLICATES_SQL);            

            matchCount = DBWrapper.findPredMatches(pred);

            System.out.println(pred + "," + count + "," + matchCount);
        }
    }

}

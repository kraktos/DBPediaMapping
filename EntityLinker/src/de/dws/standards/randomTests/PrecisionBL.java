/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * Compute the precision og the Baseline ALgorithm comapred to the baseLine
 * 
 * @author Arnab Dutta
 */
public class PrecisionBL {

    // All matching instances for a particular predicate, i.e, the BL subject,
    // GS subject match and BL object, GS object match
    private static final String ALL_MATCHING_INSTANCES_BY_PREDICATE = "select count(*) from BL b where b.D_OBJ = b.B_OBJ and b.D_SUB = b.B_SUB and b.PRED=?";

    // All instances for a particular predicate,
    private static final String ALL_INSTANCES_BY_PREDICATE = "select count(*) from BL b where b.PRED =?";

    // All instances for a particular predicate, where only BL subject,
    // GS subject match
    static final String SUBJECT_PRECISION_SQL = "select count(*) from BL b where b.D_SUB = b.B_SUB and b.PRED=?";

    // All instances for a particular predicate, where only BL object, GS object
    // match
    static final String OBJECT_PRECISION_SQL = "select count(*) from BL b where b.D_OBJ = b.B_OBJ and b.PRED=?";

    // fet all GS predicates
    static final String GET_GS_PREDICATES = "select E_PRED, count(*) as cnt from goldStandardClean group by E_PRED order by cnt desc";

    // stores the Predicates retrieved from GS
    static Map<String, Long> ALL_NELL_PREDS_IN_GS = new TreeMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        // load the NEL properties from GS
        loadNELLPredicates();

        // match predicate covered
        computePrecision(ALL_NELL_PREDS_IN_GS);
    }

    /**
     * load the NEL properties from GS
     */
    private static void loadNELLPredicates() {
        DBWrapper
                .init(GET_GS_PREDICATES);

        DBWrapper.getAllNellPreds(ALL_NELL_PREDS_IN_GS);
        System.out.println(ALL_NELL_PREDS_IN_GS.size());

    }

    /**
     * iterate the set of predicates in the gold Standard to fetch the precision
     * by predicates as well as overall
     * 
     * @param nellPredsMap
     */
    private static void computePrecision(Map<String, Long> nellPredsMap) {
        String pred = null;

        long matchCount = 0;
        long preciseCount = 0;
        long subjectCount = 0;
        long objectCount = 0;
        long totalPrec = 0;
        long totalMc = 0;

        for (Entry<String, Long> entry : nellPredsMap.entrySet()) {
            pred = entry.getKey();

            DBWrapper.init(ALL_INSTANCES_BY_PREDICATE);
            matchCount = DBWrapper.findPredMatches(pred);

            DBWrapper.init(ALL_MATCHING_INSTANCES_BY_PREDICATE);
            preciseCount = DBWrapper.findPerfectMatches(pred);

            DBWrapper.init(SUBJECT_PRECISION_SQL);
            subjectCount =
                    DBWrapper.findPerfectMatches(pred);
            DBWrapper.init(OBJECT_PRECISION_SQL);
            objectCount =
                    DBWrapper.findPerfectMatches(pred);

            double prec = (matchCount == 0) ? 0 : ((double) preciseCount / (double) matchCount);

            double precSubj = (matchCount == 0) ? 0 : ((double)
                    subjectCount / (double) matchCount);
            double precObj =
                    (matchCount == 0) ? 0 : ((double) objectCount / (double)
                            matchCount);

            System.out.println(pred + "," + " " + matchCount + ", " + preciseCount + ", "
                    + prec * 100 + ",  " + precSubj * 100 + ",  " + precObj * 100);

            totalPrec = totalPrec + preciseCount;
            totalMc = totalMc + matchCount;

        }

        DBWrapper.shutDown();

        System.out.println(totalMc + "  " + (double) totalPrec / (double) totalMc);

    }
}

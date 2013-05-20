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
public class PrecisionMappingChecker {

    private static final String NO_DUPLICATES_SQL = "select count(distinct E_SUB, E_PRED, E_OBJ) from goldStandard where E_PRED = ?";

    // private static final String DUPLICATES_SQL =
    // "select count(*) from goldStandard where E_PRED = ?";
    private static final String DUPLICATES_SQL = "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where E_PRED = ? group by E_SUB , E_PRED , E_OBJ) as Aa";

    // EVALUATION TABLE
    private static final String NO_DUPLI_EVAL_SQL = "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where E_PRED = ?";

    // find overall precision
    // static final String PRECISION_SQL =
    // "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where G_SUB = B_SUB and G_OBJ = B_OBJ and E_PRED = ?";
    static final String PRECISION_SQL = "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where G_SUB = B_SUB and G_OBJ = B_OBJ and E_PRED =? group by E_SUB , E_PRED , E_OBJ) as AA";

    // subject precision
    // static final String SUBJECT_PRECISION_SQL =
    // "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where G_SUB = B_SUB and  E_PRED = ?";
    static final String SUBJECT_PRECISION_SQL = "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where G_SUB = B_SUB and E_PRED =? group by E_SUB , E_PRED , E_OBJ) as AA";

    // object precision
    // static final String OBJECT_PRECISION_SQL =
    // "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where G_OBJ = B_OBJ and E_PRED = ?";
    static final String OBJECT_PRECISION_SQL = "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where G_OBJ = B_OBJ and E_PRED =? group by E_SUB , E_PRED , E_OBJ) as AA";

    static Map<String, Long> ALL_NELL_PREDS = new TreeMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        loadNELLPredicates();

    }

    private static void loadNELLPredicates() {
        DBWrapper
                .init("select E_PRED, count(*) as cnt from eval group by E_PRED order by cnt desc");

        DBWrapper.getAllNellPreds(ALL_NELL_PREDS);
        // System.out.println(ALL_NELL_PREDS.size());

        // match predicate covered
        fetchMatchedPreds(ALL_NELL_PREDS);

        // precision on predicates
        // computePredicatePrecision(ALL_NELL_PREDS);

    }

    private static void fetchMatchedPreds(Map<String, Long> nellPredsMap) {
        String pred = null;
        long count = 0;
        long matchCount = 0;
        long preciseCount = 0;
        long subjectCount = 0;
        long objectCount = 0;
        long totalPrec = 0;
        long totalMc = 0;
        
        //DBWrapper.init(DUPLICATES_SQL);

        for (Entry<String, Long> entry : nellPredsMap.entrySet()) {
            pred = entry.getKey();
            count = entry.getValue();

            if (true) {

                DBWrapper.init(DUPLICATES_SQL);
                matchCount = DBWrapper.findPredMatches(pred);
                totalMc = totalMc + matchCount;
                
                DBWrapper.init(PRECISION_SQL);
                preciseCount = DBWrapper.findPerfectMatches(pred);
                totalPrec = totalPrec + preciseCount;
                
               DBWrapper.init(SUBJECT_PRECISION_SQL);
                subjectCount = DBWrapper.findPerfectMatches(pred);
                
                DBWrapper.init(OBJECT_PRECISION_SQL);
                objectCount = DBWrapper.findPerfectMatches(pred);
                
                double prec = (matchCount == 0) ? 0 : ((double) preciseCount / (double) matchCount);
                double precSubj = (matchCount == 0) ? 0
                        : ((double) subjectCount / (double) matchCount);
                double precObj = (matchCount == 0) ? 0
                        : ((double) objectCount / (double) matchCount);
                
                
                    System.out.println(pred + "," + " " +  matchCount + ", "
                            + prec * 100  + ",  " + precSubj * 100 + ",  " + precObj * 100);

                DBWrapper.shutDown();

            } else {

                matchCount = DBWrapper.findPredMatches(pred);

                System.out.println(pred + " " + matchCount);
            }
            


        }

        
        System.out.println(totalMc + "  " + (double) totalPrec /(double) totalMc);


    }
}

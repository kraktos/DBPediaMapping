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
public class PrecisionBL {

    // Retrieved
    // select * from baseLine b where b.E_PRED ='countryalsoknownas' ;

    // Relevant and retrieved
    // select * from goldStandard g, baseLine b where b.E_SUB = g.E_SUB and
    // b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED and b.D_SUB = g.D_SUB and
    // b.D_OBJ = g.D_OBJ and b.E_PRED ='countryalsoknownas' ;

    // Relevant
    // select * from goldStandard g, baseLine b where b.E_SUB = g.E_SUB and
    // b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED and b.E_PRED
    // ='countryalsoknownas' ;

    private static final String NO_DUPLICATES_SQL = "select count(distinct E_SUB, E_PRED, E_OBJ) from goldStandard where E_PRED = ?";

    // private static final String DUPLICATES_SQL =
    // "select count(*) from goldStandard where E_PRED = ?";
    private static final String RETRIEVED_DOCS_BL_SQL = "select count(*) from baseLine b where b.E_PRED =?";
            
            //"select count(*) from (select * from baseLine b where b.E_PRED =?) as  AA, goldStandard G where AA.E_SUB = G.E_SUB and AA.E_OBJ =G.E_OBJ and AA.E_PRED = G.E_PRED"; 
            
            //"select count(*) from baseLine b where b.E_PRED =?";

    // "select sum(cn) from (select count(*) as cn from goldStandard g, baseLine b where b.E_SUB = g.E_SUB and b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED  and b.E_PRED =? group by b.E_SUB, b.E_PRED, b.E_OBJ)    AA";
    // "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where E_PRED = ? group by E_SUB , E_PRED , E_OBJ) as Aa";
    // select sum(cn) from (select count(*) as cn from goldStandard g, baseLine
    // b where b.E_SUB = g.E_SUB and b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED
    // and b.E_PRED ='countryalsoknownas' group by b.E_SUB, b.E_PRED, b.E_OBJ)
    // AA;

    // EVALUATION TABLE
    private static final String NO_DUPLI_EVAL_SQL = "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where E_PRED = ?";

    // find overall precision
    // static final String PRECISION_SQL =
    // "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where G_SUB = B_SUB and G_OBJ = B_OBJ and E_PRED = ?";

    static final String RETRIEVED_AND_RELEVANT_BL_SQL = "select distinct b.E_SUB, b.E_PRED, b.E_OBJ, b.D_SUB, b.D_PRED, b.D_OBJ from goldStandard g, baseLine b where b.E_SUB = g.E_SUB and b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED and b.D_SUB = g.D_SUB and b.D_OBJ = g.D_OBJ and b.D_PRED = g.D_PRED and b.E_PRED =?"; 
     //"select count(*) from goldStandard g, baseLine b where b.E_SUB = g.E_SUB and b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED and b.D_SUB = g.D_SUB and b.D_OBJ = g.D_OBJ and b.E_PRED =?";

    // "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where G_SUB = B_SUB and G_OBJ = B_OBJ and E_PRED =? group by E_SUB , E_PRED , E_OBJ) as AA";

    // select sum(cn) from (select count(*) as cn from goldStandard g, baseLine
    // b where b.E_SUB = g.E_SUB and b.E_OBJ = g.E_OBJ and b.E_PRED = g.E_PRED
    // and b.D_SUB = g.D_SUB and b.D_OBJ = g.D_OBJ and b.E_PRED
    // ='countryalsoknownas' group by b.E_SUB, b.E_PRED, b.E_OBJ) AA;

    // subject precision
    // static final String SUBJECT_PRECISION_SQL =
    // "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where G_SUB = B_SUB and  E_PRED = ?";
    static final String SUBJECT_PRECISION_SQL = "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where G_SUB = B_SUB and E_PRED =? group by E_SUB , E_PRED , E_OBJ) as AA";

    // object precision
    // static final String OBJECT_PRECISION_SQL =
    // "select count(distinct E_SUB, E_PRED, E_OBJ) from eval where G_OBJ = B_OBJ and E_PRED = ?";
    static final String OBJECT_PRECISION_SQL = "SELECT SUM(cn) AS Total from (select count(*) as cn from eval where G_OBJ = B_OBJ and E_PRED =? group by E_SUB , E_PRED , E_OBJ) as AA";

    static Map<String, Long> ALL_BL_PREDS = new TreeMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        loadNELLPredicates();

    }

    private static void loadNELLPredicates() {
        DBWrapper
                .init("select E_PRED, count(*) as cnt from baseLine group by E_PRED order by cnt desc");

        DBWrapper.getAllNellPreds(ALL_BL_PREDS);
        // System.out.println(ALL_NELL_PREDS.size());

        // match predicate covered
        fetchMatchedPreds(ALL_BL_PREDS);

        // precision on predicates
        // computePredicatePrecision(ALL_NELL_PREDS);

    }

    private static void findNonMatchedPreds() {

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

        // DBWrapper.init(DUPLICATES_SQL);

        for (Entry<String, Long> entry : nellPredsMap.entrySet()) {
            pred = entry.getKey();
            count = entry.getValue();

            if (true) {

                /*if(pred.equals("generalizations"))
                {
                    matchCount = 14409;
                    totalMc = totalMc + matchCount;

                    preciseCount = 12806;
                    totalPrec = totalPrec + preciseCount;

                }else{*/
                DBWrapper.init(RETRIEVED_DOCS_BL_SQL);
                matchCount = DBWrapper.findPredMatches(pred);

                DBWrapper.init(RETRIEVED_AND_RELEVANT_BL_SQL);
                preciseCount = DBWrapper.findPerfectMatches(pred);

                
                
                //}
                /*
                 * DBWrapper.init(SUBJECT_PRECISION_SQL); subjectCount =
                 * DBWrapper.findPerfectMatches(pred);
                 * DBWrapper.init(OBJECT_PRECISION_SQL); objectCount =
                 * DBWrapper.findPerfectMatches(pred);
                 */
                double prec = (matchCount == 0) ? 0 : ((double) preciseCount / (double) matchCount);
                

                /*
                 * double precSubj = (matchCount == 0) ? 0 : ((double)
                 * subjectCount / (double) matchCount); double precObj =
                 * (matchCount == 0) ? 0 : ((double) objectCount / (double)
                 * matchCount);
                 */
                System.out.println(pred + "," + " " + matchCount + ", " + preciseCount + ", "
                        + prec * 100); // + ",  " + precSubj * 100 + ",  " +
                                       // precObj * 100);

                /*if(prec > 1.0){
                    preciseCount = matchCount;
                    prec = 1.0;
                }*/
                totalPrec = totalPrec + preciseCount;
                totalMc = totalMc + matchCount;

            } else {

                matchCount = DBWrapper.findPredMatches(pred);

                System.out.println(pred + " " + matchCount);
            }

        }
        
        DBWrapper.shutDown();

        System.out.println(totalMc + "  " + (double) totalPrec / (double) totalMc);

    }
}

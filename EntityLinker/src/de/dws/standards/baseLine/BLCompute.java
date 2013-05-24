/**
 * 
 */

package de.dws.standards.baseLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class BLCompute {

    // define Logger
    static Logger logger = Logger.getLogger(BLCompute.class.getName());

    private static final String DB_NAME_SOURCE_GS = "goldStandardClean_Reverb";
    private static final String DB_NAME_TARGET_BL = "BL_Reverb";

    private static final String DISTINCT_IE_TRIPLES_GS = "select distinct E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ from "
            +
            DB_NAME_SOURCE_GS;

    private static final String INSERT_INTO_BL = "INSERT INTO " +
            DB_NAME_TARGET_BL +
            " (SUB,PRED,OBJ,D_SUB,D_PRED,D_OBJ,B_SUB,B_OBJ)VALUES(?,?,?,?,?,?,?,?)";

    private static final String DB_HEAD = "http://dbpedia.org/resource/";

    // stores all the distinct gold standard triples
    private static final List<String> ALL_DISTINCT_GOLD_TRIPLES = new ArrayList<String>();

    private static Map<String, String> IN_MEMORY_CONCEPTS = new HashMap<String, String>();

    private static List<String> BL_INSERT_ROWS = new ArrayList<String>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        // get the distinct IE triples from gold standard
        getGoldStdIETriples();

        // get the most frequent URI
        getMostFreqConcept();

        // save to baseline DB
        dumpToDB();

    }

    /**
     * saves to baseline DB
     */
    private static void dumpToDB() {
        DBWrapper.init(INSERT_INTO_BL);
        for (String tuple : BL_INSERT_ROWS) {
            DBWrapper.saveToBL(tuple, "\t");
        }

        // flush residuals
        DBWrapper.saveResidualSFs();

        // shutdown DB
        DBWrapper.shutDown();

    }

    /**
     * get the distinct IE triples from gold standard
     */
    private static void getGoldStdIETriples() {
        DBWrapper
                .init(DISTINCT_IE_TRIPLES_GS);

        DBWrapper.getGoldTriples(ALL_DISTINCT_GOLD_TRIPLES);
        System.out.println(ALL_DISTINCT_GOLD_TRIPLES.size());
    }

    /**
     * get the most frequent URI
     */
    private static final void getMostFreqConcept() {
        List<String> subjConcepts = null;
        List<String> objConcepts = null;

        String blSubj = null;
        String blObj = null;

        String ieSubj = null;
        String ieRel = null;
        String ieObj = null;

        String goldSubj = null;
        String goldRel = null;
        String goldObj = null;

        String[] arrGoldInst = null;
        // init DB
        DBWrapper.init(Constants.GET_WIKI_TITLES_SQL);

        int cntr = 0;
        for (String goldInstance : ALL_DISTINCT_GOLD_TRIPLES) {

            arrGoldInst = goldInstance.split(DBWrapper.GS_DELIMITER);

            ieSubj = arrGoldInst[0];
            ieRel = arrGoldInst[1];
            ieObj = arrGoldInst[2];

            goldSubj = arrGoldInst[3];
            goldRel = arrGoldInst[4];
            goldObj = arrGoldInst[5];

            // for the IE subject
            if (IN_MEMORY_CONCEPTS.containsKey(ieSubj)) {
                blSubj = IN_MEMORY_CONCEPTS.get(ieSubj);
            } else {
                if (Constants.IS_NELL) { // For NELL
                    subjConcepts = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieSubj).replaceAll(
                            "_",
                            " "));
                } else { // For ReVerb
                    subjConcepts = DBWrapper.fetchWikiTitles(Utilities.removeStopWords(ieSubj
                            .replaceAll(" 's", "'s")));
                }
                if (subjConcepts.size() > 0) {
                    blSubj = subjConcepts.get(0);
                    IN_MEMORY_CONCEPTS.put(ieSubj, Utilities.utf8ToCharacter(blSubj));
                }
            }

            // for the IE object
            if (IN_MEMORY_CONCEPTS.containsKey(ieObj)) {
                blObj = IN_MEMORY_CONCEPTS.get(ieObj);
            } else {
                if (Constants.IS_NELL) {
                    objConcepts = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieObj).replaceAll(
                            "_",
                            " "));
                } else {
                    objConcepts = DBWrapper.fetchWikiTitles(Utilities.removeStopWords(ieObj
                            .replaceAll(" 's", "'s")));
                }
                if (objConcepts.size() > 0) {
                    blObj = objConcepts.get(0);
                    IN_MEMORY_CONCEPTS.put(ieObj, Utilities.utf8ToCharacter(blObj));
                }
            }

            if (cntr++ % 100 == 0)
                System.out.println(cntr);

            logger.info(ieSubj + "\t" + ieRel + "\t" + ieObj + "\t" + goldSubj + "\t" + goldRel
                    + "\t" + goldObj + "\t"
                    + DB_HEAD + blSubj + "\t" + DB_HEAD + blObj);

            // add to the collection
            BL_INSERT_ROWS.add(ieSubj + "\t" + ieRel + "\t" + ieObj + "\t" + goldSubj + "\t"
                    + goldRel
                    + "\t" + goldObj + "\t"
                    + DB_HEAD + blSubj + "\t" + DB_HEAD + blObj);
        }// end of for loop
    }

}

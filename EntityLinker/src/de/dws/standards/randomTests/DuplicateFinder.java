/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.nlp.dao.FreeFormFactDao;

/**
 * @author Arnab Dutta
 */
public class DuplicateFinder {

    // define Logger
    static Logger logger = Logger.getLogger(DuplicateFinder.class.getName());

    static final String DUPLICATE_TRIPLES = "select E_SUB, E_PRED, E_OBJ, count(*) from goldStandard group by E_SUB, E_PRED , E_OBJ having count(*) > 1";

    static final String MULTI_GS_INSTANCES = "select D_SUB, D_PRED, D_OBJ from goldStandard where E_SUB =? and E_PRED = ? and E_OBJ = ?";

    static final String WRONG_MAPPINGS = "select * from "
            +
            "             (select * from eval where E_PRED = ?) as AA where "
            +
            "(E_SUB, E_PRED, E_OBJ, G_SUB  ,G_PRED , G_OBJ  ,B_SUB  ,B_PRED,B_OBJ  ) NOT IN "
            +
            "             (select * from eval where E_PRED = ? and G_SUB = B_SUB and G_OBJ = B_OBJ)";

    private static final List<FreeFormFactDao> ALL_DUPLI_TRIPLES = new ArrayList<FreeFormFactDao>();

    
    static Map<String, Long> ALL_NELL_PREDS = new TreeMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {

        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        // find wrong mappings in evaluation
        loadNELLPredicates();
        fetchWrongMappings();
        
        // find duplicate gold standard instances
        //loadDuplicateNellTriples();
        //fetchTriples();
    }
    
    
    private static void fetchWrongMappings() {
        String pred = null;
        List<String> result = new ArrayList<String>();
        String[] arr = null;
        DBWrapper.init(WRONG_MAPPINGS);
        
        for(Map.Entry<String, Long> entry : ALL_NELL_PREDS.entrySet()){
            pred = entry.getKey();
            result = DBWrapper.getWrongMappingsFromEval(pred);
            
            if (result.size() > 0) {
                logger.info(pred + " = " + result.size());
                for (String resuString : result) {
                    logger.info(resuString);
                }
                logger.info("\n\n");
            }
        }
    }


    private static void loadNELLPredicates() {
        DBWrapper
                .init("select E_PRED, count(*) as cnt from eval group by E_PRED order by cnt desc");

        DBWrapper.getAllNellPreds(ALL_NELL_PREDS);
    }
    

    private static void fetchTriples() {
        List<FreeFormFactDao> retList = null;
        DBWrapper.init(MULTI_GS_INSTANCES);
        for (FreeFormFactDao nellTriple : ALL_DUPLI_TRIPLES) {
            retList = DBWrapper.giveDupliRows(nellTriple);
            for (FreeFormFactDao gsTriple : retList) {
                logger.info(nellTriple.getSurfaceSubj() + ", "
                        + nellTriple.getRelationship() + ", " + nellTriple.getSurfaceObj() + ", "
                        + gsTriple.getSurfaceSubj() + ", " + gsTriple.getRelationship() + ", "
                        + gsTriple.getSurfaceObj());
            }
        }

    }

    private static void loadDuplicateNellTriples() {
        DBWrapper.init(DUPLICATE_TRIPLES);
        DBWrapper.getAllDuplicateNellPreds(ALL_DUPLI_TRIPLES);
    }

}

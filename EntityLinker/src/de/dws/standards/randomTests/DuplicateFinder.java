/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final List<FreeFormFactDao> ALL_DUPLI_TRIPLES = new ArrayList<FreeFormFactDao>();

    /**
     * @param args
     */
    public static void main(String[] args) {

        PropertyConfigurator.configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        
        loadDuplicateNellTriples();
        fetchTriples();
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

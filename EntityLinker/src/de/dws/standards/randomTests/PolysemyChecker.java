/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * Find average polysemy per entity
 * 
 * @author Arnab Dutta
 */
public class PolysemyChecker {

    // define Logger
    static Logger logger = Logger.getLogger(PolysemyChecker.class.getName());

    private static final String GS_TABLE_NAME = "goldStandardClean";

    private static final String ALL_SF_SUB = "select distinct E_SUB from " +
            GS_TABLE_NAME +
            " where E_PRED = ?";

    private static final String ALL_SF_OBJ = "select distinct E_OBJ from "
            +
            GS_TABLE_NAME +
            " where E_PRED = ?";

    private static final String POYSEM_CNT_SF = "select distinct URI from surfaceForms where SF = ?";

    static Set<String> ALL_SURFACES = null;
    static Set<String> ALL_URIS = null;

    static Map<String, Long> ALL_GS_PREDS = new TreeMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        loadNELLPredicates();

        run();
    }

    private static void loadNELLPredicates() {
        // FOr all
        // DBWrapper
        // .init("select E_PRED, count(*) as cnt from goldStandard group by E_PRED order by cnt desc");

        // for multi mappings
        DBWrapper
                .init("select E_PRED, count(*) as cnt from " +
                        GS_TABLE_NAME +
                        " group by E_PRED order by cnt desc");

        DBWrapper.getAllNellPreds(ALL_GS_PREDS);
        logger.info("TOTAL GS PREDS = " + ALL_GS_PREDS.size());

    }

    private static void run() {

        for (Map.Entry<String, Long> entry : ALL_GS_PREDS.entrySet()) {

            ALL_SURFACES = new TreeSet<String>();
            ALL_URIS = new TreeSet<String>();

            DBWrapper.init(ALL_SF_SUB);
            DBWrapper.getAllSurfaceForms(ALL_SURFACES, entry.getKey());

            DBWrapper.init(ALL_SF_OBJ);
            DBWrapper.getAllSurfaceForms(ALL_SURFACES, entry.getKey());

            DBWrapper.init(POYSEM_CNT_SF);

            for (String sf : ALL_SURFACES) {
                DBWrapper.findURIs(ALL_URIS, cleanse(sf));
            }

            System.out.println(entry.getKey() + "," + (double) ALL_URIS.size()
                    / (double) ALL_SURFACES.size() + "," + entry.getValue());

        }

        DBWrapper.shutDown();
    }

    public static String cleanse(String arg) {
        arg = arg.substring(arg.lastIndexOf(":") + 1, arg.length());
        return arg.toLowerCase();
    }
}

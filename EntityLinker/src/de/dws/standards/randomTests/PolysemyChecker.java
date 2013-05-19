/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.ArrayList;
import java.util.List;

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

    private static final String ALL_SF_SUB = "select distinct E_SUB from goldStandard ";
    private static final String ALL_SF_OBJ = "select distinct E_OBJ from goldStandard";
    private static final String POYSEM_CNT_SF = "select count(distinct URI) from surfaceForms where SF = ?";

    static List<String> ALL_SURFACES = new ArrayList<String>();

    /**
     * @param args
     */
    public static void main(String[] args) {

        run();
    }

    private static void run() {
        DBWrapper.init(ALL_SF_SUB);
        DBWrapper.getAllSurfaceForms(ALL_SURFACES);

        DBWrapper.init(ALL_SF_OBJ);
        DBWrapper.getAllSurfaceForms(ALL_SURFACES);
        
        DBWrapper.init(POYSEM_CNT_SF);

        long count = 0;
        long total = 0;
        long unAmbiCount = 0;
        long ambiCount = 0;

        for (String sf : ALL_SURFACES) {
            count = DBWrapper.findPerfectMatches(cleanse(sf));

            if (count == 1)
                unAmbiCount++;
            else
                ambiCount++;

            logger.info(sf + " => " + count);
            total = total + count;
        }

        logger.info("surface forms = " + ALL_SURFACES.size());
        logger.info("Total Concepts = " + total);
        logger.info("Average polysemy = " + (double) total / (double) ALL_SURFACES.size());
        logger.info("Total Ambiguous = " + ambiCount);
        logger.info("Total Unambiguous = " + unAmbiCount);

        DBWrapper.shutDown();
    }

    public static String cleanse(String arg) {
        arg = arg.substring(arg.lastIndexOf(":") + 1, arg.length());
        return arg.toLowerCase();
    }
}

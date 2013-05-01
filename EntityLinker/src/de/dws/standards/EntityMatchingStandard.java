/**
 * 
 */

package de.dws.standards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.mapper.helper.util.Constants;
import de.dws.nlp.dao.FreeFormFactDao;
import de.dws.nlp.dao.SurfaceFormDao;

/**
 * @author Arnab Dutta
 */
public class EntityMatchingStandard {

    // define Logger
    static Logger logger = Logger.getLogger(EntityMatchingStandard.class.getName());

    // global counter
    private static long cntr = 0;

    // unmatched facts counter
    private static long unMatchedFactCnt = 0;

    // global timer
    private static long timer = 0;

    private static long SINGLE_SINGLE_MATCHING = 0;

    private static long SINGLE_MANY_MATCHING = 0;

    private static long MANY_MANY_MATCHING = 0;

    private static long NONE_MATCHING = 0;

    static List<FreeFormFactDao> nellTriples = null;

    //static Map<FreeFormFactDao, FreeFormFactDao> map = new HashMap<FreeFormFactDao, FreeFormFactDao>();

    /**
     * @param args
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException,
            ExecutionException {

        PropertyConfigurator.configure(args[2]);

        // check input parameters
        if (args.length < 2) {
            logger.info("USAGE: java -jar runner.jar <path of file> <number of facts>");
        } else {
            logger.info("Starting processing " + args[0]);
            processFile(args[0], Integer.parseInt(args[1]));
        }
    }

    /**
     * Takes as input an Wikipedia infobox extracted data
     * 
     * @param filePath
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static void processFile(String filePath, int dataSize) throws FileNotFoundException,
            IOException,
            InterruptedException, ExecutionException {
        String[] arr = null;
        boolean flag = false;

        BufferedReader tupleReader = new BufferedReader(new FileReader(filePath));

        if (tupleReader != null) {
            String tupleFromIE;

            // create a DB routine to fetch the surface forms of the arguments
            DBWrapper.init(Constants.GET_WIKI_SURFACE_FORMS_SQL);

            // read a random triple at a time
            while ((tupleFromIE = tupleReader.readLine()) != null) {

                arr = tupleFromIE.split("\\s");

                flag = checkIfValidTriple(arr[0], arr[1], arr[2]);
                if (flag) {

                    // use this high quality ground fact to generate possible
                    // facts
                    processTriple(stripHeaders(arr[0]),
                            stripHeaders(arr[1]), stripHeaders(arr[2]));

                    // findSurfaceForms2(stripHeaders(arr[0]),
                    // stripHeaders(arr[1]), stripHeaders(arr[2]));

                    cntr++;
                }
                if (cntr == dataSize) // check point
                    break;

            } // end of while

            /*for (Map.Entry<FreeFormFactDao, FreeFormFactDao> entry : map.entrySet()) {
                FreeFormFactDao key = entry.getKey();
                FreeFormFactDao value = entry.getValue();
                logger.info(key.toString() + " => " + value.toString() + "\n");
            }*/

            logger.info("\n Extraction performed in  .." + timer + " millisecds");

            logger.debug("NONE_MATCHING = " + (double) NONE_MATCHING / (double) cntr
                    + " \nMANY_MANY_MATCHING = "
                    + (double) MANY_MANY_MATCHING / (double) cntr + " \nSINGLE_SINGLE_MATCHING = "
                    + (double) SINGLE_SINGLE_MATCHING / (double) cntr
                    + " \nSINGLE_MANY_MATCHING = " + (double) SINGLE_MANY_MATCHING / (double) cntr
                    + " \n TOTAL = " + cntr);

        }
    }

    /**
     * remove the header information. We are interested only in the concept name
     * 
     * @param arg full URI of the concept
     * @return stripped concept name
     */
    private static String stripHeaders(String arg) {
        arg = arg.replace("<http://dbpedia.org/resource/", "");
        arg = arg.replace("<http://dbpedia.org/ontology/", "");
        arg = arg.replace(">", "");
        arg = arg.replace("%", "");

        return arg;
    }

    private static void findSurfaceForms2(String arg1, String rel, String arg2)
            throws InterruptedException, ExecutionException, IOException {

        long t0 = 0;
        long tn = 0;

        // start time
        t0 = System.currentTimeMillis();

        List<SurfaceFormDao> subjSurfaceForms = DBWrapper.fetchSurfaceFormsUri(arg1);
        List<SurfaceFormDao> objSurfaceForms = DBWrapper.fetchSurfaceFormsUri(arg2);

        logger.info("\n\n " + arg1 + ", " + rel + ", " + arg2);

        DBWrapper.dbRoutine(subjSurfaceForms);
        DBWrapper.dbRoutine(objSurfaceForms);

        // end time
        tn = System.currentTimeMillis();

        // update global timer
        timer = timer + (tn - t0);

    }

    /**
     * Figure out what are the different ways of referring this subject or
     * object. Basically, fetch from wikipedia data dumps which anchors are
     * leading to the page
     * 
     * @param arg1
     * @param rel
     * @param arg2
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    private static void processTriple(String arg1, String rel, String arg2)
            throws InterruptedException, ExecutionException, IOException {

        long t0 = 0;
        long tn = 0;

        // start time
        t0 = System.currentTimeMillis();

        List<String> subjSurfaceForms = DBWrapper.fetchSurfaceForms(arg1);
        List<String> objSurfaceForms = DBWrapper.fetchSurfaceForms(arg2);

        logger.debug( arg1 + ", " + rel + ", " + arg2);

        subjSurfaceForms = enhanceSurfaceForms(arg1, subjSurfaceForms);
        objSurfaceForms = enhanceSurfaceForms(arg2, objSurfaceForms);

        logger.debug(arg1 + " => " + subjSurfaceForms);
        logger.debug(arg2 + " => " + objSurfaceForms);

        findNELLMatchingTriples(arg1, rel, arg2, subjSurfaceForms, objSurfaceForms);

        // end time
        tn = System.currentTimeMillis();

        // update global timer
        timer = timer + (tn - t0);

    }

    /**
     * use the surface forms to find matching NELL triples
     * 
     * @param subjSurfaceForms list of subject surface forms
     * @param objSurfaceForms list of object surface forms
     * @throws IOException
     */
    private static void findNELLMatchingTriples(String arg1, String rel, String arg2,
            List<String> subjSurfaceForms,
            List<String> objSurfaceForms) throws IOException {

        List<FreeFormFactDao> nellTriples = null;

        // updateStatsCounter(subjSurfaceForms, objSurfaceForms);

        for (String subj : subjSurfaceForms) {
            for (String obj : objSurfaceForms) {
                nellTriples = NELLQueryEngine.doSearch(subj, obj);

                for (FreeFormFactDao nellTriple : nellTriples) {
                    // save to DB all the values
                    
                    DBWrapper.saveGoldStandard(nellTriple, arg1, rel, arg2);
                    //map.put(nellTriple, new FreeFormFactDao("DBP:"+arg1, "DBP:"+rel, "DBP:"+arg2));
                }
            }
        }
    }

    /**
     * update the statistics that how many are mapped and not
     * 
     * @param subjSurfaceForms list of subject surface forms
     * @param objSurfaceForms list of object surface forms
     */
    private static void updateStatsCounter(List<String> subjSurfaceForms,
            List<String> objSurfaceForms) {

        if (subjSurfaceForms.size() == 0 || objSurfaceForms.size() == 0)
            NONE_MATCHING++;
        else if (subjSurfaceForms.size() > 1 && objSurfaceForms.size() > 1)
            MANY_MANY_MATCHING++;
        else if (subjSurfaceForms.size() == 1 && objSurfaceForms.size() == 1)
            SINGLE_SINGLE_MATCHING++;
        else if ((subjSurfaceForms.size() == 1 && objSurfaceForms.size() > 1)
                || (subjSurfaceForms.size() > 1 && objSurfaceForms.size() == 1))
            SINGLE_MANY_MATCHING++;
    }

    // for a given page title, the substrings of the title are also valid
    // surface forms,
    // e.g. for tom cruise page, tom and cruise are valid forms
    private static List<String> enhanceSurfaceForms(String arg, List<String> forms) {
        String[] arr = arg.split("_");
        // doing just for two words
        if (arr.length == 2) {
            if (!forms.contains(arr[0]))
                forms.add(arr[0]);

            if (!forms.contains(arr[1]))
                forms.add(arr[1]);
        }
        return forms;
    }

    /**
     * Skipping any datatype values. Only entities i.e something with
     * "http://dbpedia.org/" header
     * 
     * @param arg1
     * @param rel
     * @param arg2
     * @return
     */
    private static boolean checkIfValidTriple(String arg1, String rel, String arg2) {
        if (arg1.contains(Constants.DBPEDIA_HEADER) && rel.contains(Constants.ONTOLOGY_NAMESPACE) &&
                arg2.contains(Constants.DBPEDIA_HEADER))
            return true;
        return false;
    }

}

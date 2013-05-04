/**
 * 
 */

package de.dws.standards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.mapper.helper.util.Constants;
import de.dws.nlp.dao.FreeFormFactDao;

/**
 * @author Arnab Dutta
 */
public class EntityMatchingStandard {

    // define Logger
    static Logger logger = Logger.getLogger(EntityMatchingStandard.class.getName());

    // global counter
    private static long cntr = 0;

    // global timer
    private static long timer = 0;

    static List<FreeFormFactDao> nellTriples = null;

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
            processDBPediaTriple(args[0], Integer.parseInt(args[1]));
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
    private static void processDBPediaTriple(String filePath, int dataSize)
            throws FileNotFoundException,
            IOException,
            InterruptedException, ExecutionException {

        String[] arr = null;

        BufferedReader tupleReader = new BufferedReader(new FileReader(filePath));

        if (tupleReader != null) {
            String tripleFromDBPedia;

            // create a DB routine to fetch the surface forms of the arguments
            DBWrapper.init(Constants.GET_WIKI_SURFACE_FORMS_SQL);

            // read a random triple at a time
            while ((tripleFromDBPedia = tupleReader.readLine()) != null) {

                arr = tripleFromDBPedia.split("\\s");

                if (checkIfValidTriple(arr[0], arr[1], arr[2])) {

                    // use this high quality ground fact to generate possible
                    // facts
                    processTriple(stripHeaders(arr[0]),
                            stripHeaders(arr[1]), stripHeaders(arr[2]));

                    cntr++;
                    double perc = ((double) cntr / (double) dataSize) * 100;
                    if (perc % 10 == 0)
                        logger.info(perc + " % completed in " + ((double) timer / (double) 1000)
                                + " secds");
                }
                if (cntr == dataSize) // check point
                    break;

            } // end of while

            DBWrapper.saveResiduals();
            // shutdown DB
            DBWrapper.shutDown();

            // logger.info("\n Extraction performed in  .." + timer +
            // " millisecds");

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

        logger.debug(arg1 + ", " + rel + ", " + arg2);

        // subjSurfaceForms = enhanceSurfaceForms(arg1, subjSurfaceForms);
        // objSurfaceForms = enhanceSurfaceForms(arg2, objSurfaceForms);

        /*
         * if (arg2.equals("Tad_Lincoln")) { logger.info(arg1 + " => " +
         * subjSurfaceForms); logger.info(arg2 + " => " + objSurfaceForms); }
         */

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
                nellTriples = NELLQueryEngine.doSearch(Constants.NELL_ENT_INDEX_DIR, subj, obj);

                for (FreeFormFactDao nellTriple : nellTriples) {
                    // save to DB all the values
                    DBWrapper.saveGoldStandard(nellTriple, arg1, rel, arg2);
                }
            }
        }

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

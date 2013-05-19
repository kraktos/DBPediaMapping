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

import de.dws.helper.util.Constants;
import de.dws.helper.util.Timer;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;
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

    /**
     * triple returned after a match occurs
     */
    static List<FreeFormFactDao> nellTriples = null;

    /**
     * caches the surface forms
     */
    static Map<String, List<String>> inMemorySurfForms = new HashMap<String, List<String>>();

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
            //System.out.println(Utilities.utf8ToCharacter("K%C5%8Dichi_Yamadera"));
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

        // initiate timer
        Timer timerObj = new Timer();

        // initiate Lucene searcher
        TripleIndexQueryEngine searcher = new TripleIndexQueryEngine(Constants.NELL_ENT_INDEX_DIR);

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
                    processTriple(searcher, stripHeaders(arr[0]),
                            stripHeaders(arr[1]), stripHeaders(arr[2]));
                }

                cntr++;

                if ((cntr % 50000) == 0) {
                    timer = timer + timerObj.tick();
                    logger.info(cntr + " completed in " + ((double) timer / (double) 1000)
                            + " secds");
                }

                // if (cntr == dataSize) // check point
                // break;

            } // end of while

            // write to DB residual tuples
            DBWrapper.saveResidualGS();

            // shutdown DB
            DBWrapper.shutDown();

            inMemorySurfForms.clear();
        }
    }

    /**
     * remove the header information. We are interested only in the concept name
     * 
     * @param arg full URI of the concept
     * @return stripped concept name
     */
    private static String stripHeaders(String arg) {
        // arg = arg.replace("<http://dbpedia.org/resource/", "");
        arg = arg.replaceAll(">", "");
        arg = arg.replaceAll("<", "");

        return Utilities.utf8ToCharacter(arg);
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
    private static void processTriple(TripleIndexQueryEngine searcher, String arg1, String rel,
            String arg2)
            throws InterruptedException, ExecutionException, IOException {

        List<String> subjSurfaceForms = null;
        List<String> objSurfaceForms = null;

        if (inMemorySurfForms.containsKey(arg1)) {
            subjSurfaceForms = inMemorySurfForms.get(arg1);
        } else {
            subjSurfaceForms = DBWrapper.fetchSurfaceForms(arg1);
            inMemorySurfForms.put(arg1, subjSurfaceForms);
        }

        if (inMemorySurfForms.containsKey(arg2)) {
            objSurfaceForms = inMemorySurfForms.get(arg2);
        } else {
            objSurfaceForms = DBWrapper.fetchSurfaceForms(arg2);
            inMemorySurfForms.put(arg2, objSurfaceForms);
        }

        logger.debug(arg1 + ", " + rel + ", " + arg2);

        findNELLMatchingTriples(searcher, arg1, rel, arg2, subjSurfaceForms, objSurfaceForms);
    }

    /**
     * use the surface forms to find matching NELL triples
     * 
     * @param subjSurfaceForms list of subject surface forms
     * @param objSurfaceForms list of object surface forms
     * @throws IOException
     */
    private static void findNELLMatchingTriples(TripleIndexQueryEngine searcher,
            String arg1, String rel, String arg2,
            List<String> subjSurfaceForms,
            List<String> objSurfaceForms) throws IOException {

        for (String subj : subjSurfaceForms) {
            for (String obj : objSurfaceForms) {
                nellTriples = searcher.doSearch(subj, obj, Constants.NELL_IE_DELIMIT);
                for (FreeFormFactDao nellTriple : nellTriples) {
                    // save to DB all the values
                    // send the surface form or make the link counter case
                    // insensitive
                    DBWrapper.saveGoldStandard(nellTriple, arg1, rel, arg2);
                }
            }
        }

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

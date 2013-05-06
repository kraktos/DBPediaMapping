/**
 * 
 */

package de.dws.standards.baseLine;

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
import de.dws.mapper.helper.util.Utilities;
import de.dws.nlp.dao.FreeFormFactDao;
import de.dws.standards.EntityMatchingStandard;
import de.dws.standards.NELLQueryEngine;

/**
 * This class is a baseline for the mapping tool. This generates a bunch of NELL
 * triples with DBPedia mappings. We compare it with the goldStandard data.
 * Theoritically, if the baseline is poor than gold standard, there is room for
 * improvement. This class figures out that deficiency (if any :))
 * 
 * @author Arnab Dutta
 */
public class BaseLine {

    // define Logger
    static Logger logger = Logger.getLogger(BaseLine.class.getName());

    // global timer
    private static long timer = 0;

    // global counter
    private static long cntr = 0;

    /**
     * @param args
     * @throws NumberFormatException
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws NumberFormatException, IOException,
            InterruptedException, ExecutionException {

        // check input parameters
        if (args.length < 2) {
            logger.info("USAGE: java -jar runner.jar <path of file> <number of facts>");
        } else {
            PropertyConfigurator.configure(args[2]);

            logger.info("Starting processing " + args[0]);
            processFile(args[0], Integer.parseInt(args[1]));
        }

    }

    private static void processFile(String filePath, int dataSize) throws IOException,
            InterruptedException, ExecutionException {

        String[] arr = null;

        // create a file reader stream
        BufferedReader tupleReader = new BufferedReader(new FileReader(filePath));

        // init DB
        DBWrapper.init(Constants.GET_WIKI_TITLES_SQL);

        if (tupleReader != null) {

            // read a random triple at a time
            String tripleFromIE;
            while ((tripleFromIE = tupleReader.readLine()) != null) {
                arr = tripleFromIE.split(Constants.NELL_IE_DELIMIT);

                processTriple(arr[0], arr[1], arr[2]);

                cntr++;
                // if (cntr == dataSize) // check point
                // break;

            }

            // save resudual tuples
            DBWrapper.saveResidualBaseLine();
            // shutdown DB
            DBWrapper.shutDown();

        }

    }

    private static void processTriple(String arg1, String rel, String arg2)
            throws InterruptedException, ExecutionException, IOException {

        long t0 = 0;
        long tn = 0;

        // start time
        t0 = System.currentTimeMillis();

        List<String> subjTitle = DBWrapper.fetchWikiTitles(Utilities.cleanse(arg1).replaceAll("_",
                " "));
        List<String> objTitle = DBWrapper.fetchWikiTitles(Utilities.cleanse(arg2).replaceAll("_",
                " "));

        logger.info(arg1 + ", " + rel + ", " + arg2);

        logger.debug(arg1 + " => " + subjTitle);
        logger.debug(arg2 + " => " + objTitle);

        // fetch DBPedia Infobox instances. Take the top candidate
        if (subjTitle.size() > 0 && objTitle.size() > 0)
            findDBPediaMatchingTriples(arg1, rel, arg2, subjTitle.get(0), objTitle.get(0));

        // end time
        tn = System.currentTimeMillis();

        // update global timer
        timer = timer + (tn - t0);
    }

    private static void findDBPediaMatchingTriples(String arg1, String rel, String arg2,
            String subjTitle,
            String objTitle) throws IOException {

        List<FreeFormFactDao> dbPediaTriples = null;

        dbPediaTriples = NELLQueryEngine.doSearch(Constants.DBPEDIA_INFO_INDEX_DIR, subjTitle,
                objTitle);

        for (FreeFormFactDao dbPediaTriple : dbPediaTriples) {
            // save to DB all the values
            DBWrapper.saveBaseLine(arg1, rel, arg2, dbPediaTriple);
        }
    }

}

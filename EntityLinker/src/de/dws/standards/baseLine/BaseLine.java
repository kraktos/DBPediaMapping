/**
 * 
 */

package de.dws.standards.baseLine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.clarkparsia.pellet.sparqldl.engine.CostBasedQueryPlanNew;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Timer;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.nlp.dao.FreeFormFactDao;
import de.dws.standards.TripleIndexQueryEngine;

/**
 * This class is a baseline for the mapping tool. This generates a bunch of NELL
 * triples with DBPedia mappings. We compare it with the goldStandard data.
 * Theoritically, if the baseline is poor than gold standard, there is room for
 * improvement. This class figures out that deficiency (if any :))
 * 
 * @author Arnab Dutta
 */
public class BaseLine {

    private static final String DELIMIT = Constants.NELL_IE_DELIMIT;//Constants.REVERB_IE_DELIMIT; // Constants.NELL_IE_DELIMIT;

    // define Logger
    static Logger logger = Logger.getLogger(BaseLine.class.getName());

    private static Map<String, String> inMemoryTitles = new HashMap<String, String>();

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
            processIEInputFile(args[0], Integer.parseInt(args[1]));
        }

    }

    /**
     * process the file from IE
     * 
     * @param filePath
     * @param dataSize
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void processIEInputFile(String filePath, int dataSize) throws IOException,
            InterruptedException, ExecutionException {

        String[] arr = null;

        // initiate Lucene searcher
        TripleIndexQueryEngine searcher = new TripleIndexQueryEngine(
                Constants.DBPEDIA_INFO_INDEX_DIR);

        // create a file reader stream
        BufferedReader tupleReader = new BufferedReader(new FileReader(filePath));

        // init DB
        DBWrapper.init(Constants.GET_WIKI_TITLES_SQL);

        // initiate timer
        Timer timerObj = new Timer();

        if (tupleReader != null) {

            // read a random triple at a time
            String tripleFromIE;
            while ((tripleFromIE = tupleReader.readLine()) != null) {
                arr = tripleFromIE.split(DELIMIT);

                // process individual triples
                processTriple(searcher, arr[0], arr[1], arr[2]);
                cntr++;

                if (cntr % 50000 == 0) {
                    timer = timer + timerObj.tick();

                    logger.info("Processed " + cntr + " triples in " + ((double) timer / 1000)
                            + " secds");
                }               
            }

            // save residual tuples
            DBWrapper.saveResidualBaseLine();

            // shutdown DB
            DBWrapper.shutDown();

        }

    }

    /**
     * @param searcher
     * @param ieArg1
     * @param ieRel
     * @param ieArg2
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    private static void processTriple(TripleIndexQueryEngine searcher, String ieArg1, String ieRel,
            String ieArg2)
            throws InterruptedException, ExecutionException, IOException {

        List<String> subjTitle = null;
        List<String> objTitle = null;
        String subj = null;
        String obj = null;

        // retrieve from cache if available else go on to make DB query
        if (inMemoryTitles.containsKey(ieArg1)) {
            subj = inMemoryTitles.get(ieArg1);
        } else {
            if (Constants.IS_NELL)
                subjTitle = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieArg1).replaceAll("_",                        " "));
                //subjTitle = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieArg1));
            else
                subjTitle = DBWrapper.fetchWikiTitles(Utilities.removeStopWords(ieArg1.replaceAll(
                        " 's", "'s")));
            if (subjTitle.size() > 0) {
                subj = subjTitle.get(0);
                inMemoryTitles.put(ieArg1, subj);
            }
        }

        if (inMemoryTitles.containsKey(ieArg2)) {
            obj = inMemoryTitles.get(ieArg2);
        } else {
            if (Constants.IS_NELL)
                objTitle = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieArg2).replaceAll("_"," "));
                //objTitle = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieArg2));
            else
                objTitle = DBWrapper.fetchWikiTitles(Utilities.removeStopWords(ieArg2.replaceAll(
                        " 's", "'s")));

            if (objTitle.size() > 0) {
                obj = objTitle.get(0);
                inMemoryTitles.put(ieArg2, obj);
            }
        }

        logger.debug(ieArg1 + ", " + ieRel + ", " + ieArg2);

        logger.debug(ieArg1 + " => " + subjTitle);
        logger.debug(ieArg2 + " => " + objTitle);

        // fetch DBPedia Infobox instances. Take the top candidate
        if (subj != null && obj != null) {
            findDBPediaMatchingTriples(searcher, ieArg1, ieRel, ieArg2, subj, obj);
        }
    }

    
    private static String stripHeaders(String arg) {
        arg = arg.replace("http://dbpedia.org/resource/", "");
        arg = arg.replace("http://dbpedia.org/ontology/", "");
        
        return arg;
    }
    /**
     * Find the matching DBPedia triples given the IE SPO input
     * 
     * @param searcher
     * @param arg1
     * @param rel
     * @param arg2
     * @param subjTitle
     * @param objTitle
     * @throws IOException
     */
    private static void findDBPediaMatchingTriples(TripleIndexQueryEngine searcher, String arg1,
            String rel, String arg2,
            String subjTitle,
            String objTitle) throws IOException {

        List<FreeFormFactDao> dbPediaTriples = null;

        // search over Lucene indices
        dbPediaTriples = searcher.doSearch(stripHeaders(subjTitle),
                stripHeaders(objTitle), Constants.DBPEDIA_DATA_DELIMIT);

        for (FreeFormFactDao dbPediaTriple : dbPediaTriples) {
            // save to DB all the values
            DBWrapper.saveBaseLine(arg1, rel, arg2, dbPediaTriple);
        }
    }

}

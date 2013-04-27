/**
 * 
 */

package de.dws.nlp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.mapper.helper.util.Constants;
import de.dws.nlp.dao.SentenceDao;
import de.dws.nlp.dao.WikiDao;

/**
 * @author Arnab Dutta
 */
public class StandardCreation {

    // define Logger
    static Logger logger = Logger.getLogger(StandardCreation.class.getName());

    // global counter
    private static long cntr = 0;

    // unmatched facts counter
    private static long unMatchedFactCnt = 0;

    // WikiCrawler instance
    private static WikiCrawler wikiCrawler;

    // Text Processor instance
    private static ProcessText textProcessor;

    // global timer
    private static long timer = 0;

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
                    findSurfaceForms(stripHeaders(arr[0]),
                            stripHeaders(arr[1]),
                            stripHeaders(arr[2]));

                    cntr++;
                }
                if (cntr == dataSize) // check point
                    break;

            } // end of while

            logger.info("\n Extraction performed in  .." + timer + " millisecds");
            logger.info((double) unMatchedFactCnt / (double) cntr
                    + " could not be matched, out of " + cntr + " records");
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
     */
    private static void findSurfaceForms(String arg1, String rel, String arg2)
            throws InterruptedException, ExecutionException {

        long t0 = 0;
        long tn = 0;

        // start time
        t0 = System.currentTimeMillis();

        List<String> subjs = DBWrapper.fetchSurfaceForms(arg1);
        List<String> objs = DBWrapper.fetchSurfaceForms(arg2);

        logger.info("\n\n " + arg1 + ", " + rel + ", " + arg2);

        subjs = enhanceSurfaceForms(arg1, subjs);
        objs = enhanceSurfaceForms(arg2, objs);

        logger.info(arg1 + " => " + subjs);
        logger.info(arg2 + " => " + objs);

        // call routine for wikipedia operations, parsing the text, finding
        // possible triples etc.
        WikiDao wikiDao = new WikiDao(Constants.WIKI_PAGE_HEADER + arg1, subjs, objs, null, arg1,
                rel, arg2);

        processPage(wikiDao);

        // end time
        tn = System.currentTimeMillis();

        // update global timer
        timer = timer + (tn - t0);

    }

    private static List<String> enhanceSurfaceForms(String arg, List<String> forms) {
        String[] arr = arg.split("_");
        if (arr.length == 2) {
            if (!forms.contains(arr[0]))
                forms.add(arr[0]);

            if (!forms.contains(arr[1]))
                forms.add(arr[1]);
        }
        return forms;
    }

    /**
     * takes a page and performs the search for all possible triples on it
     * 
     * @param wikiDao {@link WikiDao} instance of the page
     */
    private static void processPage(WikiDao wikiDao) {
        String text = null;
        String[] sentencesInText = null;
        List<SentenceDao> listSentenceDao;

        // crawl the page and retrieve the page text
        wikiCrawler = new WikiCrawler(wikiDao.getPageTitle());
        text = wikiCrawler.getWikiText();

        // set it back in the access object
        wikiDao.setContent(text);
        // logger.info(wikiDao.getPageTitle() + "  ->  " + text);

        // process the text
        textProcessor = new ProcessText(text);
        sentencesInText = textProcessor.getSentences();

        // get those sentences which are having a relationship
        // between some pair of surface forms
        listSentenceDao = textProcessor.fetchMatchingSentences(wikiDao.getPageTitle(),
                sentencesInText,
                wikiDao.getListSubjSurfaceForms(),
                wikiDao.getListObjectSurfaceForms());

        // do something with this bunch of sentence dao
        for (SentenceDao dao : listSentenceDao) {
            logger.info(dao.toString());
        }

        // keep a track how many could not be fetched
        if (listSentenceDao.size() == 0) {
            unMatchedFactCnt++;
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

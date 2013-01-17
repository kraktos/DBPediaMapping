/**
 * 
 */
package com.mapper.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mapper.client.Main;
import com.mapper.indexer.DataIndexerImpl;
import com.mapper.score.ScoreEngineImpl;
import com.mapper.score.Similarity;

/**
 * All different kinds of utility methods are placed here
 * 
 * @author Arnab Dutta
 */
public class Utilities
{

    // define Logger
    static Logger logger = Logger.getLogger(Utilities.class.getName());

    static Set<Long> UNIQUE_PROPERTIES = new HashSet<Long>();

    /**
     * Prints a map
     * 
     * @param map
     */
    public static void printMap(Map< ? , ? > map)
    {
        for (Iterator< ? > it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry< ? , ? > entry = (Entry< ? , ? >) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            logger.info(key + "  " + value);
        }
    }

    /**
     * Iterate the list and print out the string literals for the query
     * 
     * @param resultList
     * @param out
     */
    public static void printList(List<Long> resultList, BufferedWriter out)
    {
        try {

            String propertyLabel = null;

            for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
                // print only the odd values
                if (listCounter % 2 != 0) {
                    // this gives a set of properties for the given query
                    UNIQUE_PROPERTIES.add(resultList.get(listCounter));
                }
            }
            for (Long key : UNIQUE_PROPERTIES) {
                extractPropertyFromURI(key, out);
            }
            logger.info("Unique properties  = " + UNIQUE_PROPERTIES.size() + "\n");
        } finally {
            UNIQUE_PROPERTIES.clear();
        }

    }

    /**
     * Prints a set
     * 
     * @param set
     */
    public static void printSet(final Set< ? > set)
    {
        Iterator< ? > it = set.iterator();
        while (it.hasNext()) {
            logger.info(it.next());
        }
    }

    /**
     * @param propertyURI
     * @param key
     * @param out
     * @return
     */
    private static void extractPropertyFromURI(final Long key, BufferedWriter out)
    {

        String propertyURI = DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(key);

        FileUtil.writeToFlatFile(out, propertyURI + "\n");

    }

    /**
     * @param word
     * @return
     */
    public static String extractPredicatesFromTuples(final String word)
    {
        String[] entities = word.split(",");
        return entities[1];
    }

    /**
     * Takes a set of Strings and writes to the output file
     * 
     * @param SET_DBPEDIA_TERMS set of string values
     * @param targetFilePath putput file location
     * @throws IOException
     */
    public static void writeSetToFile(Set<String> SET_DBPEDIA_TERMS, String targetFilePath) throws IOException
    {

        FileWriter fstream = new FileWriter(targetFilePath);
        BufferedWriter out = new BufferedWriter(fstream);

        Iterator< ? > it = SET_DBPEDIA_TERMS.iterator();
        while (it.hasNext()) {
            FileUtil.writeToFlatFile(out, it.next() + "\n");
        }

        out.close();
    }

    /**
     * This method takes as input a tuple from the IE engine and tries to match the respective S, P, O with the ones in
     * DBPedia
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public static void mapTuple() throws IOException, InterruptedException
    {
        // get a stream of the source tuples
        BufferedReader br = null;
        String tupleFromIE;

        // check if the pruned file is available else continue with the larger
        // CSV file from the IE engine
        try {
            br = new BufferedReader(new FileReader(Main.greppedIEOutputCsvFilePath));
        } catch (FileNotFoundException ex) {
            br = new BufferedReader(new FileReader(Main.ieOutputCsvFilePath));
        }

        while ((tupleFromIE = br.readLine()) != null) {
            // process with each of these tuples
            Similarity.matchTuple(tupleFromIE, Main.dbPediaSubjAndObjFilePath, Main.dbPediaPredicatesFilePath);

        }
    }

    /**
     * @param tupleFromIE
     * @return
     */
    public static String extractLabel(String tupleFromIE)
    {

        String s = tupleFromIE.substring(tupleFromIE.lastIndexOf(":") + 1, tupleFromIE.length());
        return s;

    }

    /**
     * Method to create a sub set of data from the data set provided by the IE engine. This is purely for test purpose.
     * Can be removed later on.
     * 
     * @throws IOException
     */
    public static void createSubSetOfIEOuputTuples() throws IOException
    {

        // Take the user query and extract those tuples from the CSV file
        // TODO: think of doing it in without query api.
        // At some point there would be no grepped files..we have to match
        // the entire IE output tuples
        String userQuery = Main.searchQuery;
        final File file = new File(Main.ieOutputCsvFilePath);

        FileWriter fstream = new FileWriter(Main.greppedIEOutputCsvFilePath);
        BufferedWriter greppedIEOutput = new BufferedWriter(fstream);

        FileUtil.extractMatchingTuples(userQuery, file, greppedIEOutput);

        greppedIEOutput.close();

    }

    /**
     * Transform a set of extracted facts output from any IE engine like NELL, and convert it to a CSV file with
     * associated truth values of each such fact.
     */
    public static void createCSVFilefromIEDataSet()
    {

        ScoreEngineImpl scoreEngine = new ScoreEngineImpl();
        scoreEngine.readExtractedFacts(Main.extractedFactDataSet, Main.ieOutputCsvFilePath);
    }

    /**
     * The tuples coming from the IE engine are usually noisy. Add all cleansing logic here.
     * 
     * @param stringArg String to Clean
     * @return
     */
    public static String cleanseLabels(String stringArg)
    {

        // remove any "http://" coz it wont be helpful in calculating the score
        if (stringArg.lastIndexOf("http://") != -1) {
            // stringArg = stringArg.substring(stringArg.lastIndexOf("http://") + 1, stringArg.length());
            stringArg = stringArg.replaceAll("http://", "");
        }

        if (stringArg.indexOf("en.wikipedia.org/wiki/") != -1) {
            // stringArg = stringArg.substring(stringArg.lastIndexOf("en.wikipedia.org/wiki/") + 1, stringArg.length());
            stringArg = stringArg.replaceAll("en.wikipedia.org/wiki/", "");

        }

        // extract the label
        if (stringArg.lastIndexOf(":") != -1)
            stringArg = stringArg.substring(stringArg.lastIndexOf(":") + 1, stringArg.length());

        return stringArg;
    }

    /**
     * Method to check if a given String value exists in the given set
     * 
     * @param set The set to check
     * @param stringValue The value to check
     * @return
     */
    public static boolean checkUniqueness(Set<String> set, String stringValue)
    {

        if (!set.contains(stringValue)) {
            set.add(stringValue);
            return true;
        }
        return false;
    }

    /**
     * @param start the timer start point
     * @param message the message you want to display
     */
    public static void endTimer(final long start, final String message)
    {
        long end = System.currentTimeMillis();
        long execTime = end - start;
        logger.info(message + " " + String.format("%02d ms", TimeUnit.MILLISECONDS.toMillis(execTime)));
    }

    /**
     * @return the start point of time
     */
    public static long startTimer()
    {
        long start = System.currentTimeMillis();
        return start;
    }

    public static void printList(List< ? > resultList)
    {
        for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
            logger.info(resultList.get(listCounter));
        }
    }

}

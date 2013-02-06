/**
 * 
 */
package com.uni.mannheim.dws.mapper.helper.util;

import java.io.BufferedWriter;
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
            for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
                // print only the odd values
                if (listCounter % 2 != 0) {
                    // this gives a set of properties for the given query
                    UNIQUE_PROPERTIES.add(resultList.get(listCounter));
                }
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
     * @param tupleFromIE
     * @return
     */
    public static String extractLabel(String tupleFromIE)
    {

        String s = tupleFromIE.substring(tupleFromIE.lastIndexOf(":") + 1, tupleFromIE.length());
        return s;

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

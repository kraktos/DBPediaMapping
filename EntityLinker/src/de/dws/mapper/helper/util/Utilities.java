/**
 * 
 */

package de.dws.mapper.helper.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * All different kinds of utility methods are placed here
 * 
 * @author Arnab Dutta
 */
public class Utilities
{
    public static HashSet<String> dict = new HashSet<String>();

    public static int countGlbl = 10;

    public static Set<String> set = new TreeSet<String>();

    // define Logger
    static Logger logger = Logger.getLogger(Utilities.class.getName());

    static Set<Long> UNIQUE_PROPERTIES = new HashSet<Long>();

    // pattern for allowing english text only during indexing
    static Pattern pattern = Pattern.compile(Constants.ALLOWED_ENGLISH_TEXT);

    /**
     * Prints a map
     * 
     * @param map
     */
    public static void printMap(Map<?, ?> map)
    {
        for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<?, ?> entry = (Entry<?, ?>) it.next();
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
    public static void printSet(final Set<?> set)
    {
        Iterator<?> it = set.iterator();
        while (it.hasNext()) {
            logger.info(it.next());
        }
    }

    /**
     * Takes a set of Strings and writes to the output file
     * 
     * @param SET_DBPEDIA_TERMS set of string values
     * @param targetFilePath putput file location
     * @throws IOException
     */
    public static void writeSetToFile(Set<String> SET_DBPEDIA_TERMS, String targetFilePath)
            throws IOException
    {

        FileWriter fstream = new FileWriter(targetFilePath);
        BufferedWriter out = new BufferedWriter(fstream);

        Iterator<?> it = SET_DBPEDIA_TERMS.iterator();
        while (it.hasNext()) {
            FileUtil.writeToFlatFile(out, it.next() + "\n");
        }

        out.close();
    }

    /**
     * Method to check if a given String value exists in the given set
     * 
     * @param set The set to check
     * @param stringValue The value to check
     * @return a flag stating if input is in the given set
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
        logger.info(message + " "
                + String.format("%02d ms", TimeUnit.MILLISECONDS.toMillis(execTime)));
    }

    /**
     * @return the start point of time
     */
    public static long startTimer()
    {
        long start = System.currentTimeMillis();
        return start;
    }

    public static void printList(List<?> resultList)
    {
        for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
            logger.info(resultList.get(listCounter));
        }
    }

    /**
     * function to check if the string input contains non-English characters
     * 
     * @param strInput input test
     * @return boolean value to indicate if it contains or not
     */
    public static boolean containsNonEnglish(String strInput)
    {
        return pattern.matcher(strInput).find();
    }

    public static void splitIntoBagOfWords(String string) throws FileNotFoundException
    {
        split("", string);

        System.out.println(set.iterator().next());
    }

    public static void split(String head, String in)
    {

        // System.out.println(head + "  " + in);
        // head + " " + in is a segmentation
        String segment = head + " " + in;

        // count number of dictionary words
        int count = 0;
        int total = 0;
        Scanner phraseScan = new Scanner(segment);
        while (phraseScan.hasNext()) {
            total++;
            String word = phraseScan.next();
            if (dict.contains(word))
                count++;
        }

        if (count < countGlbl) {
            if (set.size() > 0) {
                set.clear();
            }
            set.add(segment);
        }

        if (count == 4)
            System.out.println(segment + "\t" + count + " English words");

        // recursive calls
        for (int i = 2; i < in.length(); i++) {
            split(head + " " + in.substring(0, i), in.substring(i, in.length()));
        }

    }

    public static String prun(String uri)
    {
        String s = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        if (s.indexOf(":") != -1)
            s = s.replaceAll(":", "");
        
            return s;
    }

}

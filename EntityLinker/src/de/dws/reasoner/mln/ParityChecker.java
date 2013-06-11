/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.dataObject.Pair;
import de.dws.reasoner.GenericConverter;

/**
 * Checks and compares the output of the MLN and its evidence files for the
 * assertions that were removed
 * 
 * @author Arnab Dutta
 */
public class ParityChecker {

    private static Map<String, String> inMemMap = new HashMap<String, String>();

    // define Logger
    static Logger logger = Logger.getLogger(ParityChecker.class.getName());

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {

        Map<String, String> map = new HashMap<String, String>();

        String strLine1, strLine2;
        long originalSize = 0;

        PropertyConfigurator
                .configure("resources/log4j.properties");

        FileInputStream file1 = new FileInputStream(args[0]);
        FileInputStream file2 = new FileInputStream(args[1]);

        BufferedReader input1 = new BufferedReader
                (new InputStreamReader(file1));

        BufferedReader input2 = new BufferedReader
                (new InputStreamReader(file2));

        try {
            if (args.length != 2)
                throw (new RuntimeException("Usage : java compare <filetoread> <filetoread>"));

            String[] arr = null;
            
            while ((strLine1 = input1.readLine()) != null) {
                if (strLine1.startsWith("sameAsConf")) {
                    strLine1 = strLine1.replaceAll("sameAsConf\\(", "").replaceAll("\"", "");
                    arr = strLine1.split(",");

                    if (arr[0].indexOf("DBP#") != -1
                            && arr[1].indexOf("OIE#") != -1) {

                        // make the OIE instance key, since it will always map
                        // to one DBP instance, not the
                        // other way round
                        map.put(arr[1].trim(), arr[0].trim());
                    }
                }
            }
            originalSize = map.size();

            Map<String, String> temp = map;
            while ((strLine2 = input2.readLine()) != null) {
                if (strLine2.startsWith("sameAs")) {
                    strLine2 = strLine2.replaceAll("sameAs\\(", "").replaceAll("\"", "")
                            .replaceAll("\\)", "");

                    arr = strLine2.split(",");

                    if (arr[0].indexOf("DBP#") != -1
                            && arr[1].indexOf("OIE#") != -1) {
                        temp.remove(arr[1].trim());
                    }
                }
            }

            loadUri2CanonInMemory();

            for (Map.Entry<String, String> entry : temp.entrySet()) {
                logger.info(inMemMap.get(entry.getKey().trim()) + " (" + entry.getKey()
                        + ") ==> " +
                        entry.getValue());
            }

            logger.info("===========================\n " +
                    "REMOVAL RATE = " + ((double) temp.size() / (double) originalSize));

        } catch (IOException ioe) {
            System.out.println("Error: " + ioe);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * read the OIE uri to canonical forms in memory, for using it for
     * comparison purposes
     */
    public static void loadUri2CanonInMemory() {
        BufferedReader br = null;

        try {

            String sCurrentLine;
            String key = null;
            String value = null;

            br = new BufferedReader(new FileReader(GenericConverter.URI_CANONICAL_FILE));

            while ((sCurrentLine = br.readLine()) != null) {
                key = sCurrentLine.split("\t")[0].trim();
                value = sCurrentLine.split("\t")[1].trim();
                inMemMap.put(key, value);
            }

        } catch (IOException e) {
            logger.info("Error reading " + GenericConverter.URI_CANONICAL_FILE);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}

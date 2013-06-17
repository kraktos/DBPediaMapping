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

//import de.dws.reasoner.GenericConverter;

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
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

//        PropertyConfigurator
//                .configure("resources/log4j.properties");

        //String extension = args[0].substring(args[0].lastIndexOf(".") + 1, args[0].length());
        // if (!extension.equals("db"))
        // convertToEvidence(args[0]);

        computeOverlap(args);
    }

    private static void convertToEvidence(String args) throws IOException {

        String nellSub = null;
        String nellPred = null;
        String nellObj = null;

        String goldSubj = null;
        String goldObj = null;

        FileInputStream file = new FileInputStream(args);

        BufferedReader input1 = new BufferedReader
                (new InputStreamReader(file));

        String strLine;
        String ret;

        Set<String> set = new TreeSet<String>();

        while ((strLine = input1.readLine()) != null) {

            nellSub = strLine.split("\t")[0];
            nellObj = strLine.split("\t")[2];

            goldSubj = strLine.split("\t")[3];
            goldObj = strLine.split("\t")[5];

            // System.out.println(strLine);

            set.add(createSameAsMLN(nellSub, goldSubj));
            set.add(createSameAsMLN(nellObj, goldObj));
        }

        for (String ds : set) {
            System.out.println(ds);
        }

    }

    private static String createSameAsMLN(String nell, String gold) {
        String nellFiltered = nell.substring(nell.indexOf(":") + 1, nell.length());
        String goldFiltered = gold.replaceAll("http://dbpedia.org/resource/", "");

        return "sameAs(\"NELL#Instance/" + nellFiltered + "\", \"DBP#Instance/" + goldFiltered
                + "\")";

    }

    /**
     * @param args
     * @param map
     * @param strLine1
     * @param strLine2
     * @throws FileNotFoundException
     */
    public static void computeOverlap(String[] args)
            throws FileNotFoundException {

        long originalSize;
        String evidenceFileLine;
        String outputFileLine;

        Map<String, String> map = new HashMap<String, String>();

        // read the evidence and output files
        FileInputStream file1 = new FileInputStream(args[0]);
        FileInputStream file2 = new FileInputStream(args[1]);

        BufferedReader evidenceFile = new BufferedReader
                (new InputStreamReader(file1));

        BufferedReader output = new BufferedReader
                (new InputStreamReader(file2));

        try {
            if (args.length != 2)
                throw (new RuntimeException("Usage : java -jar Parity.jar <filetoread> <filetoread>"));

            String[] arr = null;

            // read the evidence file and put the "sameAs" axioms in a map
            while ((evidenceFileLine = evidenceFile.readLine()) != null) {
                if (evidenceFileLine.startsWith("sameAsConf")) {
                    evidenceFileLine = evidenceFileLine.replaceAll("sameAsConf\\(", "").replaceAll("\"", "");
                    arr = evidenceFileLine.split(",");

                    if (arr[0].indexOf("DBP#") != -1
                            && arr[1].indexOf("NELL#") != -1) {

                        // make the OIE instance key, since it will always map
                        // to one DBP instance, not the
                        // other way round
                        map.put(arr[1].trim(), arr[0].trim());
                    }
                }
            }
            originalSize = map.size();

            // put in another variable, to avoid messing up the evidence file 
            Map<String, String> temp = map;
            
            // read the output file and read the sameAs Axioms
            while ((outputFileLine = output.readLine()) != null) {
                if (outputFileLine.startsWith("sameAs")) {
                    outputFileLine = outputFileLine.replaceAll("sameAs\\(", "").replaceAll("\"", "")
                            .replaceAll("\\)", "");

                    arr = outputFileLine.split(",");
                    
                    // if there is a match, remove them
                    if (arr[0].indexOf("DBP#") != -1
                            && arr[1].indexOf("NELL#") != -1) {
                        temp.remove(arr[1].trim());
                    }
                }
            }

            // loadUri2CanonInMemory();

            // ones left are the ones which are actually removed by reasoning from the evidence file
            for (Map.Entry<String, String> entry : temp.entrySet()) {
                // logger.info(inMemMap.get(entry.getKey().trim()) + " (" +
                // entry.getKey()
                // + ") ==> " +
                // entry.getValue());

                logger.info(entry.getKey().trim() + " " + entry.getValue());
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

            //br = new BufferedReader(new FileReader(GenericConverter.URI_CANONICAL_FILE));

            while ((sCurrentLine = br.readLine()) != null) {
                key = sCurrentLine.split("\t")[0].trim();
                value = sCurrentLine.split("\t")[1].trim();
                inMemMap.put(key, value);
            }

        } catch (IOException e) {
            //logger.info("Error reading " + GenericConverter.URI_CANONICAL_FILE);
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

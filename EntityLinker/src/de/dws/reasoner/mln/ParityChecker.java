/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Utilities;

//import de.dws.reasoner.GenericConverter;

/**
 * Checks and compares the output of the MLN and its evidence files for the
 * assertions that were removed
 * 
 * @author Arnab Dutta
 */
public class ParityChecker {

    private static final String GOLD_EVIDENCE = "resources/goldEvidence.db";

    private static Map<String, String> inMemMap = new HashMap<String, String>();

    // define Logger
    static Logger logger = Logger.getLogger(ParityChecker.class.getName());

    private static Map<Pair<String, String>, List<String>> INVERTED_MAP_SAMEAS = new HashMap<Pair<String, String>, List<String>>();

    static int numTriples = 0;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // PropertyConfigurator
        // .configure("resources/log4j.properties");

        String extension = args[0].substring(args[0].lastIndexOf(".") + 1, args[0].length());
        if (!extension.equals("db"))
            convertToEvidence(args[0]);

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

        // the file where the evidences for the MLN are written out
        FileWriter fw = new FileWriter(GOLD_EVIDENCE);
        BufferedWriter bw = new BufferedWriter(fw);

        while ((strLine = input1.readLine()) != null) {

            nellSub = strLine.split("\t")[0];
            nellPred = strLine.split("\t")[1];
            nellObj = strLine.split("\t")[2];

            goldSubj = strLine.split("\t")[3];
            goldObj = strLine.split("\t")[5];

            // System.out.println(strLine);

            createSameAsMLN(nellSub, goldSubj, bw, nellSub + "\t" + nellPred
                    + "\t" + nellObj);
            createSameAsMLN(nellObj, goldObj, bw, nellSub + "\t" + nellPred
                    + "\t" + nellObj);

            //
            // if (set.contains(nellSub + "\t" + nellPred + "\t" + nellObj))
            // System.out.println("oops");
            // else
            // set.add(nellSub + "\t" + nellPred + "\t" + nellObj);

            numTriples++;
        }

        logger.info(numTriples + " " + INVERTED_MAP_SAMEAS.size());

        bw.close();

    }

    private static void maintainInvertedIndexOfSameAs(Pair<String, String> pair, String string) {

        if (INVERTED_MAP_SAMEAS.containsKey(pair)) {
            List<String> list = INVERTED_MAP_SAMEAS.get(pair);
            list.add(string);
            INVERTED_MAP_SAMEAS.put(pair, list);
        } else {
            List<String> list = new ArrayList<String>();
            list.add(string);
            INVERTED_MAP_SAMEAS.put(pair, list);
        }

    }

    private static void createSameAsMLN(String nell, String gold, BufferedWriter bw, String string)
            throws IOException {
        String nellFiltered = nell.substring(nell.indexOf(":") + 1, nell.length());
        String goldFiltered = gold.replaceAll("http://dbpedia.org/resource/", "");

        bw.write("sameAs(\"DBP#resource/" + goldFiltered + "\"\t \"NELL#Instance/" + nellFiltered
                + "\")\n");

        maintainInvertedIndexOfSameAs(new Pair<String, String>("DBP#resource/" + goldFiltered,
                "NELL#Instance/" + nellFiltered), string);

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

        Map<Pair<String, String>, String> map = new HashMap<Pair<String, String>, String>();

        // read the evidence and output files
        FileInputStream file1 = new FileInputStream(GOLD_EVIDENCE);
        FileInputStream file2 = new FileInputStream(args[1]);

        BufferedReader evidenceFile = new BufferedReader
                (new InputStreamReader(file1));

        BufferedReader output = new BufferedReader
                (new InputStreamReader(file2));

        Pair<String, String> pair = null;
        // Pair<String, String> pair2 = null;
        try {
            if (args.length != 2)
                throw (new RuntimeException(
                        "Usage : java -jar Parity.jar <filetoread> <filetoread>"));

            String[] arr = null;

            // read the evidence file and put the "sameAs" axioms in a map
            while ((evidenceFileLine = evidenceFile.readLine()) != null) {
                if (evidenceFileLine.startsWith("sameAs")) {
                    evidenceFileLine = evidenceFileLine.replaceAll("sameAs\\(", "")
                            .replaceAll("\"\\)", "").replaceAll(
                                    "\"", "");
                    arr = evidenceFileLine.split("\t");
                    // System.out.println(arr[0] + "  " + arr[1]);

                    if (arr[0].indexOf("DBP#") != -1
                            && arr[1].indexOf("NELL#") != -1) {

                        pair = new Pair<String, String>(arr[0].trim(), arr[1].trim());
                        // make the OIE instance key, since it will always map
                        // to one DBP instance, not the
                        // other way round
                        map.put(pair, "");
                    }
                }
            }

            System.out.println(map.size());
            // System.out.println(map);
            originalSize = map.size();

            int count = 0;
            int c1 = 0;
            int c2 = 0;

            // put in another variable, to avoid messing up the evidence file
            Map<Pair<String, String>, String> temp = map;

            // read the output file and read the sameAs Axioms
            while ((outputFileLine = output.readLine()) != null) {
                if (outputFileLine.startsWith("sameAs")) {

                    c1++;
                    outputFileLine = outputFileLine.replaceAll("sameAs\\(", "")
                            .replaceAll("\"", "")
                            .replaceAll("\\)", "");

                    arr = outputFileLine.split(",");

                    // if there is a match, remove them
                    if (arr[0].indexOf("DBP#") != -1
                            && arr[1].indexOf("NELL#") != -1) {

                        c2++;
                        while (Character.isDigit(arr[1].charAt(arr[1].length() - 1))) {
                            arr[1] = arr[1].substring(0, arr[1].length() - 1);
                            if (arr[1].charAt(arr[1].length() - 1) == '_')
                                arr[1] = Utilities.utf8ToCharacter(arr[1].substring(0,
                                        arr[1].length() - 1));
                        }

                        pair = new Pair<String, String>(Utilities.utf8ToCharacter(arr[0].trim()
                                .replaceAll("~", "%"))
                                , Utilities.utf8ToCharacter(arr[1].trim().replaceAll("~", "%")));

                        if(arr[0].indexOf("Louisville~2C_Kentucky")!= -1)
                            System.out.println("");
                        
                        // System.out.println(pair);

                        if (isThereInGold(temp, pair)) {
                            count++;
                            temp.remove(pair);

                        } 
//                        else {
//                            System.out.println(pair);
//                            INVERTED_MAP_SAMEAS.remove(pair);
//                        }
                    }
                }
            }
            //System.out.println(c1 + " "+ c2);
            

            // loadUri2CanonInMemory();

            // ones left are the ones which are actually removed by reasoning
            // from the evidence file
            for (Map.Entry<Pair<String, String>, String> entry : temp.entrySet()) {
                // logger.info(inMemMap.get(entry.getKey().trim()) + " (" +
                // entry.getKey()
                // + ") ==> " +
                // entry.getValue());

                // logger.info(entry.getKey().getFirst() + " " +
                // entry.getKey().getSecond());
                INVERTED_MAP_SAMEAS.remove(entry.getKey());
            }

            logger.info("===========================\n " +
                    "ACCURACY OF SAME AS  = " + ((double) count / (double) originalSize));

            logger.info(numTriples + " " + INVERTED_MAP_SAMEAS.size());

            int triplesLeft = 0;
            Set<String> qq = new TreeSet<String>();
            for (Map.Entry<Pair<String, String>, List<String>> entry : INVERTED_MAP_SAMEAS
                    .entrySet()) {
                triplesLeft = triplesLeft + entry.getValue().size();
                // qq.add(entry.getValue());
                for (String s : entry.getValue()) {
                    qq.add(s);
                }
            }

            System.out.println(qq.size() + "  " + triplesLeft);
            logger.info("===========================\n " +
                    "ACCURACY OF OUTPUT  = " + (((double) qq.size() / (double) numTriples)));

        } catch (IOException ioe) {
            System.out.println("Error: " + ioe);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static boolean isThereInGold(Map<Pair<String, String>, String> temp,
            Pair<String, String> pair) {

        for (Map.Entry<Pair<String, String>, String> entry : temp.entrySet()) {

            // System.out.println(entry.getKey().getFirst() + " " +
            // pair.getFirst() + "  "
            // + entry.getKey().getSecond() + "  " + pair.getSecond());

            // if (entry.getKey().getFirst().indexOf("Louse") != -1) {
            // System.out.println("");
            // }
            if (entry.getKey().getFirst().equals(pair.getFirst())
                    && entry.getKey().getSecond().equals(pair.getSecond())) {
                // System.out.println(pair);
                return true;
            }
        }

        return false;

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

            // br = new BufferedReader(new
            // FileReader(GenericConverter.URI_CANONICAL_FILE));

            while ((sCurrentLine = br.readLine()) != null) {
                key = sCurrentLine.split("\t")[0].trim();
                value = sCurrentLine.split("\t")[1].trim();
                inMemMap.put(key, value);
            }

        } catch (IOException e) {
            // logger.info("Error reading " +
            // GenericConverter.URI_CANONICAL_FILE);
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

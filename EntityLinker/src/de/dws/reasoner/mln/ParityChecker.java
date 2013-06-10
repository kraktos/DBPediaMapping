/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream file1 = new FileInputStream(args[0]);
        FileInputStream file2 = new FileInputStream(args[1]);

        BufferedReader input1 = new BufferedReader
                (new InputStreamReader(file1));

        BufferedReader input2 = new BufferedReader
                (new InputStreamReader(file2));
        String strLine1, strLine2;

        Set<Pair<String, String>> setPairs = new TreeSet<Pair<String, String>>();

        Map<String, String> map = new HashMap<String, String>();

        try {
            if (args.length != 2)
                throw (new RuntimeException("Usage : java compare <filetoread> <filetoread>"));

            String[] arr = null;
            Pair<String, String> pair = null;

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
                        // System.out.println(arr[1]);

                    }
                }
            }

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

            System.out.println("Removed \n");

            loadUri2CanonInMemory();

            for (Map.Entry<String, String> entry : temp.entrySet()) {
                System.out.println(inMemMap.get(entry.getKey().trim()) + " (" + entry.getKey()
                        + ") ==> " +
                        entry.getValue());
            }

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

            br = new BufferedReader(new FileReader(GenericConverter.URI_CANONICAL_FILE));

            while ((sCurrentLine = br.readLine()) != null) {

                String key = sCurrentLine.split("\t")[0].trim();
                String value = sCurrentLine.split("\t")[1].trim();
                inMemMap.put(key, value);
            }

        } catch (IOException e) {
            e.printStackTrace();
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

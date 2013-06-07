/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.dws.helper.dataObject.Pair;

/**
 * Checks and compares the output of the MLN and its evidence files for the
 * assertions that were removed
 * 
 * @author Arnab Dutta
 */
public class ParityChecker {

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

                    if (arr[1].indexOf("http://dws/OIE#") != -1
                            && arr[0].indexOf("http://dbpedia.org") != -1) {
                        // pair = new Pair<String, String>(arr[0], arr[1]);
                        map.put(arr[0], arr[1]);
                    }
                }
            }

            Map<String, String> temp = map;
            while ((strLine2 = input2.readLine()) != null) {
                if (strLine2.startsWith("sameAs")) {
                    strLine2 = strLine2.replaceAll("sameAs\\(", "").replaceAll("\"", "");
                    arr = strLine2.split(",");

                    if (arr[1].indexOf("http://dws/OIE#") != -1
                            && arr[0].indexOf("http://dbpedia.org") != -1) {

                        temp.remove(arr[0]);
                    }
                }
            }

            System.out.println("Removed \n");
            for (Map.Entry<String, String> entry : temp.entrySet()) {
                System.out.println(entry.getKey() + " ==> " + entry.getValue());
            }

        } catch (IOException ioe) {
            System.out.println("Error: " + ioe);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

    }

}

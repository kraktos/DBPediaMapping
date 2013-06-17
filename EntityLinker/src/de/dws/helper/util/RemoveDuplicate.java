
package de.dws.helper.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.dws.helper.dataObject.Pair;

public class RemoveDuplicate {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        // removeDuplicates();

        alterPredicates();

    }

    private static void alterPredicates() throws IOException {
        FileInputStream file = new FileInputStream(
                "/home/arnab/Work/data/experiments/reasoning/subFiles/isOfTypeConf.nell.db");

        BufferedReader input = new BufferedReader
                (new InputStreamReader(file));

        String strLine;
        String strTemp;

        String s1 = null;
        String s2 = null;
        String conf = null;
        
        while ((strLine = input.readLine()) != null) {
            strTemp = strLine.replaceAll("isOfTypeConf\\(", "").replaceAll("\"\\)", "\"")
                    .replaceAll("\"", "");
            //System.out.println(strTemp);

            s1 = strTemp.trim().split(",")[0].trim();
            s2 = strTemp.trim().split(",")[1].trim();
            conf = strTemp.trim().split(",")[2].trim();

            s2 = s2.replaceAll("_[0-9]+/*\\.*[0-9]*", "").replaceAll("#Instance/", "#Concept/");

            //System.out.println(s1 + "  " + s2);
            System.out.println("isOfTypeConf(\"" + s2 + "\", \"" +
                    s1 + "\", " + conf);
        }

    }

    /**
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void removeDuplicates() throws FileNotFoundException, IOException {
        FileInputStream file = new FileInputStream(
                "/home/arnab/Work/data/experiments/reasoning/subFiles/cdis.dbpedia.db");

        BufferedReader input = new BufferedReader
                (new InputStreamReader(file));

        String strLine;
        String strTemp;
        Set<Set<String>> setSets = new TreeSet<Set<String>>();
        Set<String> smallSet = null;

        Map<Pair<String, String>, String> map = new HashMap<Pair<String, String>, String>();

        String s1 = null;
        String s2 = null;
        String s1Val = null;
        String s2Val = null;

        Pair<String, String> pair = null;
        Pair<String, String> pairReverse = null;

        while ((strLine = input.readLine()) != null) {
            // smallSet = new TreeSet<String>();

            strTemp = strLine.replaceAll("cdis\\(", "").replaceAll("\"\\)", "\"")
                    .replaceAll("\"", "");
            // System.out.println(strTemp);
            s1 = strTemp.trim().split(",")[0].trim();
            s2 = strTemp.trim().split(",")[1].trim();
            pair = new Pair<String, String>(s1, s2);
            pairReverse = new Pair<String, String>(s2, s1);

            if (!map.containsKey(pair) && !map.containsKey(pairReverse)) {
                map.put(pair, "");
            }
        }

        for (Map.Entry<Pair<String, String>, String> entry : map.entrySet()) {
            System.out.println("cdis(\"" + entry.getKey().getFirst() + "\", \"" +
                    entry.getKey().getSecond() + "\")");
        }
    }

}

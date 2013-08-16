/**
 * 
 */

package de.dws.standards.baseLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class GetProbability {

    // define Logger
    static Logger logger = Logger.getLogger(GetProbability.class.getName());

    private static final String DB_NAME_SOURCE_GS = "goldStandardClean";

    private static final String DISTINCT_IE_TRIPLES_GS = "select distinct E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ from "
            +
            DB_NAME_SOURCE_GS;

    // stores all the distinct gold standard triples
    private static final List<String> ALL_DISTINCT_GOLD_TRIPLES = new ArrayList<String>();

    private static final String GET_WIKI_LINKS_APRIORI_SQL = "select  URI, (SUM(COUNT)/(select  SUM(COUNT) from wikiPrep  where SF =?)) as p from wikiPrep  where SF =? group by URI order by p desc limit ?";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // PropertyConfigurator
        // .configure("resources/log4j.properties");

        // get the distinct IE triples from gold standard
        // getGoldStdIETriples();

        BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.APRIORI_PROB_FILE));

        BufferedReader baseReader = new BufferedReader(new FileReader(Constants.INPUT_CSV_FILE));

        // get the most frequent URI
        // getConceptLinksProbability(bw);

        getConceptLinksProbability(baseReader, bw);

        bw.close();
        // save to baseline DB
        // dumpToDB();

    }

    /**
     * get the distinct IE triples from gold standard
     */
    private static void getGoldStdIETriples() {
        DBWrapper
                .init(DISTINCT_IE_TRIPLES_GS);

        DBWrapper.getGoldTriples(ALL_DISTINCT_GOLD_TRIPLES);
        System.out.println(ALL_DISTINCT_GOLD_TRIPLES.size());
    }

    /**
     * get the most frequent URI
     * 
     * @param bw
     * @throws IOException
     */
    private static final void getConceptLinksProbability(BufferedReader baseReader,
            BufferedWriter bw) throws IOException {
        List<String> uriVsProbabilities = null;

        String ieSubj = null;
        String ieObj = null;

        String[] arrBaseLineInst = null;

        // init DB
        DBWrapper.init(GET_WIKI_LINKS_APRIORI_SQL);

        Set<String> s = new HashSet<String>();

        String bLine;
        while ((bLine = baseReader.readLine()) != null) {

            arrBaseLineInst = bLine.split("\t");

            ieSubj = arrBaseLineInst[0];
            ieObj = arrBaseLineInst[2];

            uriVsProbabilities = DBWrapper.fetchTopKLinksWikiPrepProb(Utilities.cleanse(ieSubj)
                    .replaceAll("\\_+", " "), 3);

            for (String val : uriVsProbabilities) {
                if (!s.contains(ieSubj + val)) {
                    bw.write(ieSubj + "\t" + Utilities.utf8ToCharacter(val) + "\n");
                    s.add(ieSubj + val);
                }
            }
            uriVsProbabilities = null;

            uriVsProbabilities = DBWrapper.fetchTopKLinksWikiPrepProb(Utilities.cleanse(ieObj)
                    .replaceAll("\\_+", " "), 3);

            for (String val : uriVsProbabilities) {
                if (!s.contains(ieObj + val)) {
                    bw.write(ieObj + "\t" + Utilities.utf8ToCharacter(val) + "\n");
                    s.add(ieObj + val);
                }
            }

        }// end of for loop
        s.clear();
    }

    /**
     * get the most frequent URI
     * 
     * @param bw
     * @throws IOException
     */
    private static final void getConceptLinksProbability(BufferedWriter bw) throws IOException {
        List<String> uriVsProbabilities = null;

        String ieSubj = null;
        String ieObj = null;

        String[] arrGoldInst = null;

        // init DB
        DBWrapper.init(GET_WIKI_LINKS_APRIORI_SQL);

        Set<String> s = new HashSet<String>();

        for (String goldInstance : ALL_DISTINCT_GOLD_TRIPLES) {

            arrGoldInst = goldInstance.split(DBWrapper.GS_DELIMITER);

            ieSubj = arrGoldInst[0];
            ieObj = arrGoldInst[2];

            uriVsProbabilities = DBWrapper.fetchTopKLinksWikiPrepProb(Utilities.cleanse(ieSubj)
                    .replaceAll("_", " "), 3);

            for (String val : uriVsProbabilities) {
                if (!s.contains(val)) {
                    bw.write(ieSubj + "\t" + Utilities.utf8ToCharacter(val) + "\n");
                    // if (val.indexOf("Alfonso") != -1)
                    // System.out.println(Utilities.utf8ToCharacter(val));
                    s.add(val);
                }
            }

        }// end of for loop
        s.clear();
    }

}

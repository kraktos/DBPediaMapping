/**
 * 
 */

package de.dws.standards.baseLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class BLCompute {

    // define Logger
    static Logger logger = Logger.getLogger(BLCompute.class.getName());

    private static final String DB_NAME_SOURCE_GS = "goldStandardClean";
    private static final String DB_NAME_TARGET_BL = "NEW_BL";

    private static final String DISTINCT_IE_TRIPLES_GS = "select distinct E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ from "
            +
            DB_NAME_SOURCE_GS;

    private static final String INSERT_INTO_BL = "INSERT INTO " +
            DB_NAME_TARGET_BL +
            " (SUB,PRED,OBJ,D_SUB,D_PRED,D_OBJ,B_SUB,B_OBJ)VALUES(?,?,?,?,?,?,?,?)";

    private static final String DB_HEAD = "http://dbpedia.org/resource/";

    // stores all the distinct gold standard triples
    private static final List<String> ALL_DISTINCT_GOLD_TRIPLES = new ArrayList<String>();

    private static final String NEW_GS_FILE = "/home/arnab/Work/data/NELL/ontology/toAnnotate/toAnnotate_topAll.tsv";

    // put -1 for all
    private static final int TOPK = -1;

    private static Map<String, String> IN_MEMORY_CONCEPTS = new HashMap<String, String>();

    private static Map<Pair<String, String>, Double> IN_MEMORY_PREDICATE_MAPPINGS = new HashMap<Pair<String, String>, Double>();

    private static List<String> BL_INSERT_ROWS = new ArrayList<String>();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        // get the distinct IE triples from gold standard
        // getGoldStdIETriples();

        // get the most frequent URI
        // getMostFreqConcept();

        // save to baseline DB
        // dumpToDB();

        loadAnnotatedPredicateMappings();
        createBLFromRandomTriples();
        

        //System.out.println(BL_INSERT_ROWS.size());

    }

    private static void loadAnnotatedPredicateMappings() throws IOException {
        // load the predicate mapping file, with the mapping with atleast 5
        // instances.

        BufferedReader predicateAnnotatedReader = new BufferedReader(new FileReader(
                "/home/arnab/Work/data/NELL/ontology/conditionalPredicates.nell.dbp.tsv"));

        // load the top NELL predicate list
        BufferedReader topNellPredicateReader = new BufferedReader(new FileReader(
                "/home/arnab/Work/data/NELL/ontology/toAnnotate/Nell.csv.predicates-TOP"));

        String annotatedLine = null;
        String[] arrElems = null;
        String predicate = null;
        String dbpPredicate = null;
        double instCount = 0;
        double prob = 0;
        Pair<String, String> pair = null;

        // iterate over top predicates to fetch the top DBPedia predicates

        while ((annotatedLine = predicateAnnotatedReader.readLine()) != null) {
            arrElems = annotatedLine.split("\t");
            predicate = arrElems[0];
            dbpPredicate = arrElems[1];
            instCount = Double.parseDouble(arrElems[2]);
            prob = Double.parseDouble(arrElems[3]);    

            try {
                if (arrElems[3] != null) {
                    if (!arrElems[3].equals("INCORRECT")) {
                        if (instCount >= 5) {
                            // System.out.println(annotatedLine);
                            // create a pair of NELL and DBPedia predicates
                            pair = new Pair<String, String>(predicate, dbpPredicate);

                            IN_MEMORY_PREDICATE_MAPPINGS.put(pair, prob);
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    private static List<String> getByFirst(String arg, int topK) {
        List<String> listPredicates = new ArrayList<String>();

        for (Map.Entry<Pair<String, String>, Double> entry : IN_MEMORY_PREDICATE_MAPPINGS.entrySet()) {
            if (entry.getKey().getFirst().equals(arg)) {
                if (topK == -1)
                    listPredicates.add(entry.getKey().getSecond());
                else {
                    if (listPredicates.size() < topK)
                        listPredicates.add(entry.getKey().getSecond());
                }
            }
        }
        return listPredicates;
    }

    /**
     * takes some random input of NELL triples and computes the baseline for
     * them
     * 
     * @throws IOException
     */
    private static void createBLFromRandomTriples() throws IOException {
        BufferedReader tupleReader = new BufferedReader(new FileReader(
                "/home/arnab/Work/data/NELL/ontology/toAnnotate/Nell.sample"));

        String ieSubj = null;
        String ieRel = null;
        String ieObj = null;

        List<String> subjConcepts = null;
        List<String> objConcepts = null;
        List<String> dbpPredicates = null;

        BufferedWriter goldWriter = new BufferedWriter(new FileWriter(
                NEW_GS_FILE));

        long totalRecds = 0;
        long notMapped = 0;

        if (tupleReader != null) {
            String line;

            // init DB
            DBWrapper.init(Constants.GET_WIKI_TITLES_SQL);

            // sort the map by value
            IN_MEMORY_PREDICATE_MAPPINGS = Utilities.sortByValue(IN_MEMORY_PREDICATE_MAPPINGS);

            // iterate the the nell sample tripels
            while ((line = tupleReader.readLine()) != null) {
                try {
                    totalRecds++;

                    ieSubj = line.split("\t")[0];
                    ieRel = line.split("\t")[1];
                    ieObj = line.split("\t")[2];

                    // Plug in the top concepts
                    subjConcepts = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieSubj).replaceAll(
                            "\\_+", " "));

                    objConcepts = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieObj.replaceAll(
                            "\\_+", " ")));

                    // fetch the predicates from the in-memory map
                    dbpPredicates = getByFirst(ieRel, TOPK);

                    // if both the subject and object is map-able,
                    if (subjConcepts.size() > 0 && objConcepts.size() > 0) {
                        // System.out.print(ieSubj + "\t" + ieRel + "\t" + ieObj
                        // + "\t");
                        goldWriter.write(ieSubj + "\t" + ieRel + "\t" + ieObj + "\n");
                        for (String subjs : subjConcepts) {
                            for (String objs : objConcepts) {

                                if (dbpPredicates.size() > 0) {
                                    for (String preds : dbpPredicates) {
                                        goldWriter.write("" + "\t" + "" + "\t" + "" + "\t"
                                                + subjs.replaceAll("\\s", "_") + "\t"
                                                + preds + "\t"
                                                + objs.replaceAll("\\s", "_") + "\n");
                                    }
                                }
                                else {

                                    goldWriter.write("" + "\t" + "" + "\t" + "" + "\t"
                                            + subjs.replaceAll("\\s", "_") + "\t"
                                            + objs.replaceAll("\\s", "_") + "\n");
                                }
                            }
                        }
                    } else {
                        notMapped++;
                        // System.out.println(line);
                    }

                } catch (Exception e) {
                    System.out.println("Error while reading = " + line + " " + e.getMessage());
                }
            }

            goldWriter.close();

        }
    }

    /**
     * saves to baseline DB
     */
    private static void dumpToDB() {
        DBWrapper.init(INSERT_INTO_BL);
        for (String tuple : BL_INSERT_ROWS) {
            DBWrapper.saveToBL(tuple, "\t");
        }

        // flush residuals
        DBWrapper.saveResidualSFs();

        // shutdown DB
        DBWrapper.shutDown();

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
     */
    private static final void getMostFreqConcept() {
        List<String> subjConcepts = null;
        List<String> objConcepts = null;

        String blSubj = null;
        String blObj = null;

        String ieSubj = null;
        String ieRel = null;
        String ieObj = null;

        String goldSubj = null;
        String goldRel = null;
        String goldObj = null;

        String[] arrGoldInst = null;
        // init DB
        DBWrapper.init(Constants.GET_WIKI_TITLES_SQL);

        int cntr = 0;
        for (String goldInstance : ALL_DISTINCT_GOLD_TRIPLES) {

            arrGoldInst = goldInstance.split(DBWrapper.GS_DELIMITER);

            ieSubj = arrGoldInst[0];
            ieRel = arrGoldInst[1];
            ieObj = arrGoldInst[2];

            goldSubj = arrGoldInst[3];
            goldRel = arrGoldInst[4];
            goldObj = arrGoldInst[5];

            // for the IE subject
            if (IN_MEMORY_CONCEPTS.containsKey(ieSubj)) {
                blSubj = IN_MEMORY_CONCEPTS.get(ieSubj);
            } else {
                if (Constants.IS_NELL) { // For NELL
                    subjConcepts = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieSubj).replaceAll(
                            "_",
                            " "));
                } else { // For ReVerb
                    subjConcepts = DBWrapper.fetchWikiTitles(Utilities.removeStopWords(ieSubj
                            .replaceAll(" 's", "'s")));
                }
                if (subjConcepts.size() > 0) {
                    blSubj = subjConcepts.get(0).replaceAll("\\s", "_");
                    IN_MEMORY_CONCEPTS.put(ieSubj, Utilities.utf8ToCharacter(blSubj));
                }
            }

            // for the IE object
            if (IN_MEMORY_CONCEPTS.containsKey(ieObj)) {
                blObj = IN_MEMORY_CONCEPTS.get(ieObj);
            } else {
                if (Constants.IS_NELL) {
                    objConcepts = DBWrapper.fetchWikiTitles(Utilities.cleanse(ieObj).replaceAll(
                            "_",
                            " "));
                } else {
                    objConcepts = DBWrapper.fetchWikiTitles(Utilities.removeStopWords(ieObj
                            .replaceAll(" 's", "'s")));
                }
                if (objConcepts.size() > 0) {
                    blObj = objConcepts.get(0).replaceAll("\\s", "_");
                    IN_MEMORY_CONCEPTS.put(ieObj, Utilities.utf8ToCharacter(blObj));
                }
            }

            if (cntr++ % 100 == 0)
                System.out.println(cntr);

            logger.info(ieSubj + "\t" + ieRel + "\t" + ieObj + "\t" + goldSubj + "\t" + goldRel
                    + "\t" + goldObj + "\t"
                    + DB_HEAD + blSubj + "\t" + DB_HEAD + blObj);

            // add to the collection
            BL_INSERT_ROWS.add(ieSubj + "\t" + ieRel + "\t" + ieObj + "\t" + goldSubj + "\t"
                    + goldRel
                    + "\t" + goldObj + "\t"
                    + DB_HEAD + blSubj + "\t" + DB_HEAD + blObj);
        }// end of for loop
    }

}

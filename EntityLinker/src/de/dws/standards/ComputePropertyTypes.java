/**
 * 
 */

package de.dws.standards;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;

/**
 * A distant supervision approach to find the DBPedia types a NELL predicate is
 * associated withs
 * 
 * @author Arnab Dutta
 */
public class ComputePropertyTypes {

    private static final String DB_NAME = "PREDTYPE_DOM";
    private static final String COLUMN_NAME_TYPE = "dom_type";
    private static final String COLUMN_NAME_INST = "dom_inst";

    private static final String DISTINCT_INSTANCES_BY_PRED = "select count(distinct " +
            COLUMN_NAME_INST +
            ") from " +
            DB_NAME +
            " where pred = ?";

    private static final String DISTINCT_INST = "select count(distinct pred, " +
            COLUMN_NAME_INST +
            ") from " +
            DB_NAME +
            " where pred = ? and " +
            COLUMN_NAME_TYPE +
            " = ? ";

    private static final String DISTINCT_TYPE = "select distinct " +
            COLUMN_NAME_TYPE +
            " from " +
            DB_NAME +
            " where pred = ?";

    private static final String INSERT_PRED_TYPES = "INSERT INTO " + DB_NAME
            + " ( pred, " + COLUMN_NAME_TYPE + ", " + COLUMN_NAME_INST + ") VALUES(?, ?, ?)";

    private static final String TF_SQL = "select " + COLUMN_NAME_TYPE +
            ", count(*) as cnt from " + DB_NAME
            + " where pred = ? group by " + COLUMN_NAME_TYPE +
            " order by cnt desc";

    private static final String IDF_SQL = "select count(*) from " + DB_NAME
            + " where " + COLUMN_NAME_TYPE +
            "= ?";

    private static Map<String, List<String>> AIR_MAP = new HashMap<String, List<String>>();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // loadAirpediaData();

        // this is an one time call.. comment out after all recursive use.. I m
        // in hurry !!
        // readTheGSDump();

        String predicate = "weaponmadeincountry";
        // getTermFreqInvDocFreq(predicate);

        computeDomainConf(0);

    }

    private static void computeDomainConf(int validNELLTriplesCount) {
        double coOccCount = 0;
        int distinctInstances = 0;

        int distinctInstancesByPred = 0;

        double domConf = 0D;
        DBWrapper.init(DISTINCT_TYPE);

        String predicate = "weaponmadeincountry";

        List<String> types = DBWrapper.getTypes(predicate);

        DBWrapper.init(DISTINCT_INSTANCES_BY_PRED);

        distinctInstancesByPred = DBWrapper.getDistinctInstancesByPred(predicate);

        DBWrapper
                .init(DISTINCT_INST);

        for (String type : types) {
            coOccCount = DBWrapper.getInstances(predicate, type);

            domConf = coOccCount / (double) distinctInstancesByPred;
            System.out.println(predicate + "\t" + type + "\t" + coOccCount + "\t"
                    + distinctInstancesByPred);
        }

        DBWrapper.shutDown();

    }

    private static void getTermFreqInvDocFreq(String predicate) {

        double idf = 0;

        // get the term frequencies
        DBWrapper.init(TF_SQL);

        List<Pair<String, Long>> tfs = DBWrapper.getTfIdf(predicate);

        // close DB
        DBWrapper.shutDown();

        // initate again for the inverse document frequency
        DBWrapper.init(IDF_SQL);

        // for each frequency pair, calculate the inverse doc-frequency for the
        // given term
        for (Pair<String, Long> pair : tfs) {
            idf = DBWrapper.getInverseDocFreq(predicate, pair.getFirst());

            System.out.println("TF for = " + predicate + ", " + pair.getFirst() + " => "
                    + pair.getSecond());
            // System.out.println("IDF for =  " + pair.getFirst() + " = " +
            // idf);
            // System.out
            // .println("tf-idf for " + predicate + " and " + pair.getFirst() +
            // " = "
            // + pair.getSecond() * Math.log(idf));
        }

        DBWrapper.shutDown();

    }

    private static void loadAirpediaData() throws IOException {
        BufferedReader airPediaReader = new BufferedReader(new FileReader(
                Constants.AIRPEDIA_DUMP));

        String line;
        String arr[] = null;
        String inst = null;
        String type = null;

        List<String> listTypes = null;
        while ((line = airPediaReader.readLine()) != null) {
            // System.out.println(line);
            arr = line.split("\\s");
            inst = arr[0].replaceAll("<|>", "").trim();
            type = arr[2].replaceAll("<|>", "");

            if (AIR_MAP.containsKey(inst)) {
                listTypes = AIR_MAP.get(inst);
                if (!listTypes.contains(type))
                    listTypes.add(type);
            } else {
                listTypes = new ArrayList<String>();
                listTypes.add(type);
                AIR_MAP.put(inst, listTypes);
            }

            System.out.println(AIR_MAP.size());
        }

    }

    private static void readTheGSDump() throws IOException {
        BufferedReader goldReader = new BufferedReader(new FileReader(
                Constants.SILVER_STANDARD_DUMP));

        String gLine;
        String[] arrElmnts = null;

        String nellPredicate = null;
        String gsSubject = null;
        String gsObject = null;

        String derivedDomain = null;
        String derivedRange = null;

        List<String> listTypes;

        DBWrapper.init(INSERT_PRED_TYPES);

        while ((gLine = goldReader.readLine()) != null) {
            // System.out.println(gLine);
            arrElmnts = gLine.split("\t");
            nellPredicate = arrElmnts[1];

            gsSubject = arrElmnts[3];
            gsObject = arrElmnts[5];

            // gsSubject = gsSubject.replaceAll("\"\"", "\"");
            listTypes = SPARQLEndPointQueryAPI.getInstanceTypesAll(gsObject);

            if (listTypes.size() > 0) {
                // type assertion of DBPedia instances occurring
                // as subjects
                // System.out.println(nellPredicate + "  " + listTypes);
                DBWrapper.insertIntoPredTypes(nellPredicate, listTypes, gsObject);
            }

        }

        // save the remaining uncommitted transactions
        DBWrapper.saveResidualSFs();

        DBWrapper.shutDown();

    }

}

/**
 * 
 */

package de.dws.mapper.dbConnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.dataObject.SuggestedFactDAO;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.nlp.dao.FreeFormFactDao;
import de.dws.nlp.dao.SurfaceFormDao;
import de.dws.standards.PredicateMapper;
import de.dws.standards.randomTests.PrecisionBL;

/**
 * Wrapper class to initiate the DB operations
 * 
 * @author Arnab Dutta
 */
public class DBWrapper {

    public static final String GS_DELIMITER = "~~";

    // define Logger
    static Logger logger = Logger.getLogger(DBWrapper.class.getName());

    // DB connection instance, one per servlet
    static Connection connection = null;

    // DBCOnnection
    static DBConnection dbConnection = null;

    // prepared statement instance
    static PreparedStatement pstmt = null;

    static PreparedStatement insertPrepstmnt = null;

    static PreparedStatement insertBaseLine = null;

    static PreparedStatement fetchCountsPrepstmnt = null;

    static PreparedStatement getOccurancePredicatesPrepStmnt = null;
    
    // For precision values
    static PreparedStatement getAllPredPrepStmnt = null;
        
    static PreparedStatement getAllMatchingPredPrepStmnt = null;
    
    static PreparedStatement getAllSubPredPrepStmnt = null;
    
    static PreparedStatement getAllObjPredPrepStmnt = null;
    
    
    static int batchCounter = 0;

    public static void batchInit(){
        try {
            // instantiate the DB connection
            dbConnection = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbConnection.getConnection();

            // create a statement
            getAllPredPrepStmnt = connection.prepareStatement(PrecisionBL.ALL_INSTANCES_BY_PREDICATE);
            
            getAllMatchingPredPrepStmnt = connection.prepareStatement(PrecisionBL.ALL_MATCHING_INSTANCES_BY_PREDICATE);
            
            getAllSubPredPrepStmnt = connection.prepareStatement(PrecisionBL.SUBJECT_PRECISION_SQL);
            
            getAllObjPredPrepStmnt = connection.prepareStatement(PrecisionBL.OBJECT_PRECISION_SQL);
            
            connection.setAutoCommit(false);
            
        } catch (SQLException ex) {
            logger.error("Connection Failed for batchInit! Check output console" + ex.getMessage());
        }
    }
    
    
    /**
     * initiats the connection parameters
     * 
     * @param sql
     */
    public static void init(String sql) {

        try {
            // instantiate the DB connection
            dbConnection = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbConnection.getConnection();

            // create a statement
            pstmt = connection.prepareStatement(sql);
            connection.setAutoCommit(false);

            // for nell
            // insertPrepstmnt =
            // connection.prepareStatement(Constants.INSERT_GOLD_STANDARD);

            // for reverb
            insertPrepstmnt = connection.prepareStatement(Constants.INSERT_GOLD_STANDARD_REVERB);

            insertBaseLine = connection.prepareStatement(Constants.INSERT_BASE_LINE);
            // insertBaseLine =
            // connection.prepareStatement(Constants.INSERT_BASE_LINE_REVERB);

            fetchCountsPrepstmnt = connection.prepareStatement(Constants.GET_LINK_COUNT);
            
            
            getOccurancePredicatesPrepStmnt = connection.prepareStatement(PredicateMapper.GET_SAMPLE);

        } catch (SQLException ex) {
            logger.error("Connection Failed! Check output console" + ex.getMessage());
        }
    }

    /**
     * save an axiom. This is before they are sampled.
     * 
     * @param ieValue
     * @param dbpValue
     * @param score apriori probability
     */
    public static void saveAxiomsPrior(OWLNamedIndividual ieValue, OWLNamedIndividual dbpValue,
            double score) {

        try {
            pstmt.setString(1, ieValue.toString());
            pstmt.setString(2, dbpValue.toString());
            pstmt.setDouble(3, score);
            pstmt.setDouble(4, 0D);

            // run the query finally
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error(" exception while inserting axioms before inference.." + e.getMessage());
        }
    }

    /**
     * update the axioms. After inference is done
     * 
     * @param valueFromExtractionEngine
     * @param valueFromDBPedia
     * @param score aposteriori probability
     */
    public static void saveAxiomsPosterior(String valueFromExtractionEngine,
            String valueFromDBPedia, double score) {

        try {
            pstmt.setDouble(1, score);
            pstmt.setString(2, valueFromExtractionEngine);
            pstmt.setString(3, valueFromDBPedia);

            // run the query finally
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error(" exception while inserting axioms after inference.." + e.getMessage());
        }

    }

    /**
     * queries the dB to fetch the surface forms. Look into the table structures
     * title_2_id and link_anchors
     * 
     * @param arg
     * @return
     */
    public static List<String> fetchSurfaceForms(String arg) {
        ResultSet rs = null;
        List<String> results = null;

        try {
            pstmt.setString(1, arg);

            // run the query finally
            rs = pstmt.executeQuery();
            results = new ArrayList<String>();

            while (rs.next()) {
                results.add(rs.getString(1));
            }

        } catch (Exception e) {
            logger.error(" exception while fetching " + arg + " " + e.getMessage());
        }

        return results;
    }

    public static List<SurfaceFormDao> fetchSurfaceFormsUri(String arg) {
        ResultSet rs = null;
        List<SurfaceFormDao> results = null;

        try {
            pstmt.setString(1, arg);
            pstmt.setInt(2, Constants.ATLEAST_LINKS);
            pstmt.setInt(3, Constants.TOP_ANCHORS);
            // run the query finally
            rs = pstmt.executeQuery();
            results = new ArrayList<SurfaceFormDao>();

            while (rs.next()) {
                results.add(new SurfaceFormDao(rs.getString(1), "http://dbpedia.org/resource/"
                        + arg, rs.getInt(2)));
            }

        } catch (Exception e) {
            logger.error(" exception while fetching " + arg + " " + e.getMessage());
        }

        return results;
    }

    public static void dbRoutine(List<SurfaceFormDao> surfaceForms) {

        try {
            insertPrepstmnt = connection.prepareStatement(Constants.INSERT_SURFACE_FORMS_SQL);

            for (SurfaceFormDao dao : surfaceForms) {
                insertPrepstmnt.setString(1, dao.getUri());
                insertPrepstmnt.setString(2, dao.getForm().toLowerCase());
                insertPrepstmnt.setInt(3, dao.getCount());

                // run the query finally
                insertPrepstmnt.executeUpdate();
            }
        } catch (SQLException e) {

        }

    }

    public static void saveBaseLine(String ieArg1, String ieRel, String ieArg2,
            FreeFormFactDao dbPediaTriple) {

        try {

            logger.debug("[" + dbPediaTriple.getSurfaceSubj() + ", "
                    + dbPediaTriple.getRelationship()
                    + ", "
                    + dbPediaTriple.getSurfaceObj() + "]  =>  [" + ieArg1 + ", " + ieRel + ", "
                    + ieArg2
                    + "] ");

            insertBaseLine.setString(1, ieArg1);
            insertBaseLine.setString(2, ieRel);
            insertBaseLine.setString(3, ieArg2);
            insertBaseLine.setString(4, Utilities.utf8ToCharacter(dbPediaTriple.getSurfaceSubj()));
            insertBaseLine.setString(5, Utilities.utf8ToCharacter(dbPediaTriple.getRelationship()));
            insertBaseLine.setString(6, Utilities.utf8ToCharacter(dbPediaTriple.getSurfaceObj()));

            // insertPrepstmnt.executeUpdate();
            insertBaseLine.addBatch();
            insertBaseLine.clearParameters();

            batchCounter++;
            // logger.info(batchCounter % Constants.BATCH_SIZE);
            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches of 100
                                                            // are flushed at
                                                            // a time
                // execute batch update
                insertBaseLine.executeBatch();

                logger.info("FLUSHED TO baseLine...");
                connection.commit();
                insertBaseLine.clearBatch();
            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of base lines .." + e.getMessage());
        }

    }

    private static String stripHeaders(String arg) {
        arg = arg.replace("http://dbpedia.org/resource/", "");
        // arg = arg.replace("<http://dbpedia.org/ontology/", "");
        // arg = arg.replace("<", "");
        // arg = arg.replace(">", "");
        // TODO
        // arg = arg.replace("%", "");

        return arg;
    }

    public static void saveGoldStandard(FreeFormFactDao nellTriple, String arg1, String rel,
            String arg2) {

        ResultSet rs = null;

        int subjLinksCount = 0;
        int objLinksCount = 0;

        try {

            fetchCountsPrepstmnt.setString(1, stripHeaders(arg1));
            fetchCountsPrepstmnt.setString(2, Utilities.cleanse(nellTriple.getSurfaceSubj())
                    .replaceAll("_", " "));

            // run the query finally
            rs = fetchCountsPrepstmnt.executeQuery();

            while (rs.next()) {
                subjLinksCount = rs.getInt(1);
            }

            fetchCountsPrepstmnt.setString(1, stripHeaders(arg2));
            fetchCountsPrepstmnt.setString(2, Utilities.cleanse(nellTriple.getSurfaceObj())
                    .replaceAll("_", " "));

            // run the query finally
            rs = fetchCountsPrepstmnt.executeQuery();

            while (rs.next()) {
                objLinksCount = rs.getInt(1);
            }

            logger.debug("[" + nellTriple.getSurfaceSubj() + ", " + nellTriple.getRelationship()
                    + ", "
                    + nellTriple.getSurfaceObj() + "]  =>  [" + arg1 + ", " + rel + ", " + arg2
                    + "] " + subjLinksCount + " " + objLinksCount);

            insertPrepstmnt.setString(1, nellTriple.getSurfaceSubj());
            insertPrepstmnt.setString(2, nellTriple.getRelationship());
            insertPrepstmnt.setString(3, nellTriple.getSurfaceObj());
            insertPrepstmnt.setString(4, arg1);
            insertPrepstmnt.setString(5, rel);
            insertPrepstmnt.setString(6, arg2);
            insertPrepstmnt.setInt(7, subjLinksCount);
            insertPrepstmnt.setInt(8, objLinksCount);

            // insertPrepstmnt.executeUpdate();
            insertPrepstmnt.addBatch();
            insertPrepstmnt.clearParameters();

            batchCounter++;

            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches of 100
                                                            // are flushed at
                                                            // a time
                // execute batch update
                insertPrepstmnt.executeBatch();

                logger.info("FLUSHED TO goldStandrd ..");
                connection.commit();
                insertPrepstmnt.clearBatch();

            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of gold standards.." + e.getMessage());
        }
    }

    public static void saveResidualBaseLine() {
        try {
            if (batchCounter % Constants.BATCH_SIZE != 0) {
                insertBaseLine.executeBatch();
                logger.info("FLUSHED TO baseLine DB...");
                connection.commit();

            }
        } catch (SQLException e) {
        }
    }

    public static void saveResidualGS() {
        try {
            if (batchCounter % Constants.BATCH_SIZE != 0) {
                insertPrepstmnt.executeBatch();
                logger.info("FLUSHED TO goldStandard DB...");
                connection.commit();
            }
        } catch (SQLException e) {
        }
    }

    public static void saveResidualSFs() {
        try {
            if (batchCounter % Constants.BATCH_SIZE != 0) { // batches of 100
                pstmt.executeBatch();
                connection.commit();
                logger.info("FLUSHED TO surfaceForms DB...");
            }
        } catch (SQLException e) {
        }
    }

    public static List<String> fetchWikiTitles(String arg) {
        ResultSet rs = null;
        List<String> results = null;

        try {
            pstmt.setString(1, arg);
            // pstmt.setInt(2, Constants.ATLEAST_LINKS);
            // pstmt.setInt(2, Constants.TOP_ANCHORS);
            // run the query finally
            rs = pstmt.executeQuery();
            results = new ArrayList<String>();

            while (rs.next()) {
                results.add(rs.getString(1));
            }

        } catch (Exception e) {
            logger.error(" exception while fetching " + arg + " " + e.getMessage());
        }

        return results;
    }

    public static void shutDown() {

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (Exception excp) {
            }
        }

        if (insertPrepstmnt != null) {
            try {
                insertPrepstmnt.close();
            } catch (Exception excp) {
            }
        }

        if (fetchCountsPrepstmnt != null) {
            try {
                fetchCountsPrepstmnt.close();
            } catch (Exception excp) {
            }
        }
        dbConnection.shutDown();

    }

    public static Set<String> getAllSurfaceForms(Set<String> aLL_SURFACES, String arg) {

        try {
            pstmt.setString(1, arg);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                aLL_SURFACES.add(rs.getString(1));
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return aLL_SURFACES;

    }

    public static List<FreeFormFactDao> getTriples(
            List<FreeFormFactDao> allDupliTriples) {

        try {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                allDupliTriples.add(new FreeFormFactDao(rs.getString(1), rs.getString(2), rs
                        .getString(3)));
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return allDupliTriples;

    }

    public static Map<String, Long> getAllNellPreds(Map<String, Long> allPreds) {

        try {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                allPreds.put(rs.getString(1), rs.getLong(2));
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return allPreds;

    }

    public static long findPredMatches(String arg) {
        try {
            getAllPredPrepStmnt.setString(1, arg);
            ResultSet rs = getAllPredPrepStmnt.executeQuery();

            while (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;

    }

    public static Map<String, Long> getRankedPredicates(String predicate) {

        /*
         * Map<Long, String> rankedPredicates = new TreeMap<Long, String>(new
         * Comparator<Long>() { public int compare(Long first, Long second) {
         * return second.compareTo(first); } });
         */

        Map<String, Long> rankedPredicates = new TreeMap<String, Long>();

        try {
            pstmt.setString(1, predicate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rankedPredicates.put(rs.getString(2), rs.getLong(1));
            }

        } catch (SQLException e) {
            logger.error("Error while getting ranked list of prediucates..");
        }

        return rankedPredicates;

    }

    public static void saveSurfaceForms(String uri, String sf, double sfGivenUri) {

        try {

            pstmt.setString(1, uri);
            pstmt.setString(2, sf);
            pstmt.setDouble(3, sfGivenUri);

            pstmt.addBatch();
            pstmt.clearParameters();

            batchCounter++;
            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches are
                                                            // flushed at
                                                            // a time
                // execute batch update
                pstmt.executeBatch();

                logger.info("FLUSHED TO surfaceForm..." + batchCounter);
                connection.commit();
                pstmt.clearBatch();
            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of surfaceForms .." + e.getMessage());
        }

    }

    public static Set<String> findURIs(Set<String> aLL_URIS, String pred) {
        try {
            pstmt.setString(1, pred);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                aLL_URIS.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return aLL_URIS;
    }

    
    public static long findPerfectObjectMatches(String pred) {
        int rsCount = 0;

        try {
            getAllObjPredPrepStmnt.setString(1, pred);
            ResultSet rs = getAllObjPredPrepStmnt.executeQuery();

            int size = rs.getRow() * rs.getMetaData().getColumnCount();

            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    
    public static long findPerfectSubjectMatches(String pred) {
        int rsCount = 0;

        try {
            getAllSubPredPrepStmnt.setString(1, pred);
            ResultSet rs = getAllSubPredPrepStmnt.executeQuery();

            int size = rs.getRow() * rs.getMetaData().getColumnCount();

            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    
    public static long findPerfectMatches(String pred) {
        int rsCount = 0;

        try {
            getAllMatchingPredPrepStmnt.setString(1, pred);
            ResultSet rs = getAllMatchingPredPrepStmnt.executeQuery();

            int size = rs.getRow() * rs.getMetaData().getColumnCount();

            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static List<FreeFormFactDao> giveDupliRows(FreeFormFactDao nellTriple) {

        List<FreeFormFactDao> retList = new ArrayList<FreeFormFactDao>();

        try {

            pstmt.setString(1, nellTriple.getSurfaceSubj());
            pstmt.setString(2, nellTriple.getRelationship());
            pstmt.setString(3, nellTriple.getSurfaceObj());

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                retList.add(new FreeFormFactDao(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return retList;

    }

    public static List<String> getWrongMappingsFromEval(String pred) {
        List<String> results = new ArrayList<String>();

        try {
            pstmt.setString(1, pred);
            pstmt.setString(2, pred);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getString(3) + "|"
                        + rs.getString(4) + "|" + rs.getString(5) + "|" + rs.getString(6) + "|"
                        + rs.getString(7) + "|" + rs.getString(8) + "|" + rs.getString(9) + "|");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<String> getTopTriples(FreeFormFactDao ieTriple) {
        List<String> results = new ArrayList<String>();

        try {
            pstmt.setString(1, ieTriple.getSurfaceSubj());
            pstmt.setString(2, ieTriple.getRelationship());
            pstmt.setString(3, ieTriple.getSurfaceObj());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString(1) + GS_DELIMITER + rs.getString(2) + GS_DELIMITER
                        + rs.getString(3)
                        + GS_DELIMITER
                        + rs.getString(4) + GS_DELIMITER + rs.getString(5) + GS_DELIMITER
                        + rs.getString(6));
            }
        } catch (SQLException e) {
            logger.error("Error in getTopTriples " + e.getMessage());
        }

        return results;
    }

    public static void insertGold(String tuple) {

        String[] arr = tuple.split(GS_DELIMITER);
        try {

            pstmt.setString(1, arr[0].trim());
            pstmt.setString(2, arr[1].trim());
            pstmt.setString(3, arr[2].trim());
            pstmt.setString(4, arr[3].trim());
            pstmt.setString(5, arr[4].trim());
            pstmt.setString(6, arr[5].trim());

            pstmt.addBatch();
            pstmt.clearParameters();

            batchCounter++;
            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches are
                                                            // flushed at
                                                            // a time
                // execute batch update
                pstmt.executeBatch();

                logger.info("FLUSHED TO better goldStandard");
                connection.commit();
                pstmt.clearBatch();
            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of surfaceForms .." + e.getMessage());
        }

    }

    public static Map<Pair<String, String>, Long> getCanonVsUriPairs(
            Map<Pair<String, String>, Long> pairs) {

        long countVal = 0;

        try {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Pair<String, String> pair = new Pair<String, String>(rs.getString(1),
                        rs.getString(2));
                if (pairs.containsKey(pair)) {
                    countVal = pairs.get(pair) + 1;
                    pairs.put(pair, countVal);
                } else {
                    pairs.put(pair, 1L);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pairs;

    }

    public static List<String> getGoldTriples(List<String> allDistinctGoldTriples) {
        try {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                allDistinctGoldTriples.add(rs.getString(1) + GS_DELIMITER + rs.getString(2)
                        + GS_DELIMITER
                        + rs.getString(3)
                        + GS_DELIMITER
                        + rs.getString(4) + GS_DELIMITER + rs.getString(5) + GS_DELIMITER
                        + rs.getString(6));
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return allDistinctGoldTriples;

    }

    public static List<String> getSampleInstances(String predicate, String key) {
        List<String> results = new ArrayList<String>();

        try {
            getOccurancePredicatesPrepStmnt.setString(1, predicate);
            getOccurancePredicatesPrepStmnt.setString(2, key);

            ResultSet rs = getOccurancePredicatesPrepStmnt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString(1) + GS_DELIMITER + rs.getString(2) + GS_DELIMITER
                        + rs.getString(3)
                        + GS_DELIMITER
                        + rs.getString(4) + GS_DELIMITER + rs.getString(5) + GS_DELIMITER
                        + rs.getString(6));
            }
        } catch (SQLException e) {
            logger.error("Error in getSampleInstances " + e.getMessage());
        }

        return results;

    }

    public static void saveToBL(String tuple, String delimiter) {

        String[] arr = tuple.split(delimiter);
        try {

            pstmt.setString(1, arr[0].trim());
            pstmt.setString(2, arr[1].trim());
            pstmt.setString(3, arr[2].trim());
            pstmt.setString(4, arr[3].trim());
            pstmt.setString(5, arr[4].trim());
            pstmt.setString(6, arr[5].trim());
            pstmt.setString(7, arr[6].trim());
            pstmt.setString(8, arr[7].trim());

            pstmt.addBatch();
            pstmt.clearParameters();

            batchCounter++;
            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches are
                                                            // flushed at
                                                            // a time
                // execute batch update
                pstmt.executeBatch();

                logger.info("FLUSHED TO BL");
                connection.commit();
                pstmt.clearBatch();
            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of surfaceForms .." + e.getMessage());
        }

    }
}

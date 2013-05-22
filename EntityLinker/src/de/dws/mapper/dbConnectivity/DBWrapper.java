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

import de.dws.helper.dataObject.SuggestedFactDAO;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.nlp.dao.FreeFormFactDao;
import de.dws.nlp.dao.SurfaceFormDao;

/**
 * Wrapper class to initiate the DB operations
 * 
 * @author Arnab Dutta
 */
public class DBWrapper {

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

    static int batchCounter = 0;

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
                pstmt.executeBatch();
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
            //pstmt.setInt(2, Constants.TOP_ANCHORS);
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

    public static List<FreeFormFactDao> getAllDuplicateNellPreds(
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
            pstmt.setString(1, arg);
            ResultSet rs = pstmt.executeQuery();

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

    public static long findPerfectMatches(String pred) {
        int rsCount = 0;

        try {
            pstmt.setString(1, pred);
            ResultSet rs = pstmt.executeQuery();

            int size = rs.getRow() * rs.getMetaData().getColumnCount();

            while (rs.next()) {
                rsCount++;

                // return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rsCount;
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
}

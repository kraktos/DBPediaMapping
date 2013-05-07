/**
 * 
 */

package de.dws.mapper.dbConnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

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

            insertPrepstmnt = connection.prepareStatement(Constants.INSERT_GOLD_STANDARD);

            insertBaseLine = connection.prepareStatement(Constants.INSERT_BASE_LINE);

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
            pstmt.setInt(2, Constants.ATLEAST_LINKS);
            pstmt.setInt(3, Constants.TOP_ANCHORS);
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

    public static void saveBaseLine(String nellArg1, String nellRel, String nellArg2,
            FreeFormFactDao dbPediaTriple) {
        ResultSet rs = null;

        try {

            logger.debug("[" + dbPediaTriple.getSurfaceSubj() + ", "
                    + dbPediaTriple.getRelationship()
                    + ", "
                    + dbPediaTriple.getSurfaceObj() + "]  =>  [" + nellArg1 + ", " + nellRel + ", "
                    + nellArg2
                    + "] ");

            insertBaseLine.setString(1, nellArg1);
            insertBaseLine.setString(2, nellRel);
            insertBaseLine.setString(3, nellArg2);
            insertBaseLine.setString(4, dbPediaTriple.getSurfaceSubj());
            insertBaseLine.setString(5, dbPediaTriple.getRelationship());
            insertBaseLine.setString(6, dbPediaTriple.getSurfaceObj());

            // insertPrepstmnt.executeUpdate();
            insertBaseLine.addBatch();

            batchCounter++;
            // logger.info(batchCounter % Constants.BATCH_SIZE);
            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches of 100
                                                            // are flushed at
                                                            // a time
                // execute batch update
                insertBaseLine.executeBatch();

                logger.info("FLUSHED TO baseLine...");
            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of base lines .." + e.getMessage());
        }

    }

    public static void saveGoldStandard(FreeFormFactDao nellTriple, String arg1, String rel,
            String arg2) {

        ResultSet rs = null;

        int subjLinksCount = 0;
        int objLinksCount = 0;

        try {

            fetchCountsPrepstmnt.setString(1, arg1);
            fetchCountsPrepstmnt.setString(2, Utilities.cleanse(nellTriple.getSurfaceSubj()));

            // run the query finally
            rs = fetchCountsPrepstmnt.executeQuery();

            while (rs.next()) {
                subjLinksCount = rs.getInt(1);
            }

            fetchCountsPrepstmnt.setString(1, arg2);
            fetchCountsPrepstmnt.setString(2, Utilities.cleanse(nellTriple.getSurfaceObj()));

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
            insertPrepstmnt.setString(4, "http://dbpedia.org/resource/" + arg1);
            insertPrepstmnt.setString(5, "http://dbpedia.org/ontology/" + rel);
            insertPrepstmnt.setString(6, "http://dbpedia.org/resource/" + arg2);
            insertPrepstmnt.setInt(7, subjLinksCount);
            insertPrepstmnt.setInt(8, objLinksCount);

            // insertPrepstmnt.executeUpdate();
            insertPrepstmnt.addBatch();
            batchCounter++;
            // logger.info(batchCounter % Constants.BATCH_SIZE);
            if (batchCounter % Constants.BATCH_SIZE == 0) { // batches of 100
                                                            // are flushed at
                                                            // a time
                // execute batch update
                insertPrepstmnt.executeBatch();

                logger.info("FLUSHED TO goldStandrd ..");
            }

        } catch (SQLException e) {
            logger.error("Error with batch insertion of gold standards.." + e.getMessage());
        }
    }

    public static void saveResidualBaseLine() {
        try {
            if (batchCounter % Constants.BATCH_SIZE != 0) { // batches of 100
                pstmt.executeBatch();
                logger.info("FLUSHED TO baseLine DB...");
            }
        } catch (SQLException e) {
        }
    }

    public static void saveResiduals() {
        try {
            if (batchCounter % Constants.BATCH_SIZE != 0) { // batches of 100
                insertPrepstmnt.executeBatch();
                logger.info("FLUSHED TO goldStandard DB...");
            }
        } catch (SQLException e) {
        }
    }

    public static List<String> fetchWikiTitles(String arg) {
        ResultSet rs = null;
        List<String> results = null;

        try {
            pstmt.setString(1, arg);
            //pstmt.setInt(2, Constants.ATLEAST_LINKS);
            pstmt.setInt(2, Constants.TOP_ANCHORS);
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

}

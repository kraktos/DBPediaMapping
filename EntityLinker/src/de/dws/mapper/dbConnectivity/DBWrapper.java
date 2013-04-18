/**
 * 
 */

package de.dws.mapper.dbConnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

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

    // prepared statement instance
    static PreparedStatement pstmt = null;

    /**
     * initiats the connection parameters
     * 
     * @param sql
     */
    public static void init(String sql) {

        try {
            // instantiate the DB connection
            DBConnection dbConnection = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbConnection.getConnection();

            // create a statement
            pstmt = connection.prepareStatement(sql);

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
            logger.info(" exception while inserting axioms before inference.." + e.getMessage());
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
            logger.info(" exception while inserting axioms before inference.." + e.getMessage());
        }

    }
}

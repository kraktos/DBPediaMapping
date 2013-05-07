/**
 * 
 */

package de.dws.mapper.knowledgeBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import de.dws.helper.dataObject.SuggestedFactDAO;
import de.dws.helper.util.Constants;
import de.dws.mapper.dbConnectivity.DBConnection;

/**
 * This class provides functionalities to work upon the uncertain and certain
 * knowledge bases
 * 
 * @author Arnab Dutta
 */
public class UncertainKB implements IKnowledgeBase
{

    // logger
    static Logger logger = Logger.getLogger(UncertainKB.class.getName());

    public int createKB(Connection connection, PreparedStatement pstmt,
            List<SuggestedFactDAO> listGoldFacts, SuggestedFactDAO uncertainFact, String remoteIP)
    {

        // mostly the loop will run once, since we will have one-on-one mapping
        // between extracted fact and the gold fact

        for (SuggestedFactDAO goldFact : listGoldFacts) {
            try {
                pstmt.setString(1, uncertainFact.getSubject());
                // set the uncertain incoming fact
                pstmt.setString(2, uncertainFact.getPredicate());
                pstmt.setString(3, uncertainFact.getObject());
                pstmt.setDouble(4, uncertainFact.getConfidence());

                // set the suggested Gold fact

                pstmt.setString(5, goldFact.getSubject());

                // set the uncertain incoming fact
                pstmt.setString(6, goldFact.getPredicate());
                pstmt.setString(7, goldFact.getObject());
                
                pstmt.setString(8, remoteIP);
                // run the query finally
                pstmt.executeUpdate();

                return 0;
                
            } catch (SQLException e) {
                logger.info(" exception while inserting gold standard.." + e.getMessage());
            }
        }
        return -1;
    }

    public void createKB(Connection connection, PreparedStatement pstmt, SuggestedFactDAO fact)
    {
        try {
            pstmt.setString(1, fact.getSubject());
            // set input parameter 1
            pstmt.setString(2, fact.getPredicate()); // set input parameter 2
            pstmt.setString(3, fact.getObject()); // set input parameter 3
            pstmt.setDouble(4, (fact.getConfidence() != null) ? fact.getConfidence() : 1.0); // set
                                                                                             // input
                                                                                             // parameter
                                                                                             // 4

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error(" record exists  !!");
        }

    }

    public static void main(String[] ar) throws SQLException
    {
        Connection connection;
        DBConnection dbConnection = new DBConnection();

        // retrieve the freshly created connection instance
        connection = dbConnection.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(Constants.INSERT_FACT_SQL);

        logger.debug(" connection successful !!");

        SuggestedFactDAO d = new SuggestedFactDAO("a", "b", "c", null, true);

        new UncertainKB().createKB(connection, pstmt, d);
    }

}

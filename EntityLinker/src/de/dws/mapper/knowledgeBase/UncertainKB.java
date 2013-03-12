/**
 * 
 */
package de.dws.mapper.knowledgeBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;

/**
 * This class provides functionalities to work upon the uncertain and certain knowledge bases
 * 
 * @author Arnab Dutta
 */
public class UncertainKB implements IKnowledgeBase
{

    // logger
    static Logger logger = Logger.getLogger(UncertainKB.class.getName());

    public void createKB(Connection connection, PreparedStatement pstmt, SuggestedFactDAO fact)
    {
        try {
            pstmt.setString(1, fact.getSubject());
            // set input parameter 1
            pstmt.setString(2, fact.getPredicate()); // set input parameter 2
            pstmt.setString(3, fact.getObject()); // set input parameter 3
            pstmt.setDouble(4, (fact.getConfidence() != null) ? fact.getConfidence() : 1.0); // set input parameter 4

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

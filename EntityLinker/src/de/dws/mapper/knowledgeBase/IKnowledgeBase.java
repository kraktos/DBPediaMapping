/**
 * 
 */
package de.dws.mapper.knowledgeBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.dws.helper.dataObject.SuggestedFactDAO;

/**
 * This class provides functionalities to work upon the uncertain and certain knowledge bases
 * 
 * @author Arnab Dutta
 */
public interface IKnowledgeBase
{

    /**
     * method to create an uncertain knowledge base
     * 
     * @param connection the connection handler to create new facts
     * @param fact the fact to be inserted
     * @throws SQLException
     */
    public void createKB(Connection connection, PreparedStatement pstmt, SuggestedFactDAO fact) throws SQLException;
}

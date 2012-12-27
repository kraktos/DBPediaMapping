/**
 * 
 */
package com.mapper.dbConnectivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mapper.message.Messages;
import com.mapper.utility.Utilities;

/**
 * @author Arnab Dutta
 */
public class DBConnection
{

    // logger
    static Logger logger = Logger.getLogger(DBConnection.class.getName());

    // Connection reference
    private Connection connection = null;

    // Statement
    Statement statement = null;

    // Url to conenct to the Database
    public static String connectionURL = Messages.getString("CONNECTION_URL");

    // name of the database
    public static String dbName = Messages.getString("DB_NAME");

    // user of the database. Make sure this user is created for the DB
    public static String dbUser = Messages.getString("DB_USER");

    // password for the user
    public static String dbUserPassword = Messages.getString("DB_PWD");

    /**
     * @return the connection
     */
    public Connection getConnection()
    {
        return this.connection;
    }

    /**
     * @param connection the connection to set
     */
    public void setConnection(Connection connection)
    {
        this.connection = connection;
    }

    /**
     * @return the statement
     */
    public Statement getStatement()
    {
        return statement;
    }

    /**
     * @param statement the statement to set
     */
    public void setStatement(Statement statement)
    {
        this.statement = statement;
    }

    static {
        registerDriver();

        logger.info("PostgreSQL JDBC Driver Registered!\n");

    }

    public Connection initDB()
    {
        try {

            // setConnection(DriverManager.getConnection(connectionURL + dbName, dbUser, dbUserPassword));
            this.connection = DriverManager.getConnection(connectionURL + dbName, dbUser, dbUserPassword);

            if (this.connection != null) {
                return getConnection();

            } else {
                logger.info("Failed to make connection!");
            }

        } catch (SQLException e) {
            logger.error("Connection Failed! Check output console" + e.getMessage());
        }
        return null;

    }

    /**
     * @param statement Statement reference
     * @param resultSet ResultSet reference
     * @param queriedItem
     * @param start
     */
    public static void performDBOperation(Statement statement, ResultSet resultSet, String queriedItem,
        final String query)
    {

        Connection connection;
        PreparedStatement preparedStatement = null;

        DBConnection dbConnection = new DBConnection();
        connection = dbConnection.initDB();

        // start Timer
        final long start = System.currentTimeMillis();

        int count = 0;
        try {
            if (connection != null) {

                // set the prepared statement unknown parameters
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, Utilities.cleanseLabels(queriedItem));

                // execute the statement and fetch the result set
                resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    logger.info("no data");
                } else {

                    logger.info(" TOP MATCHES FOR \"" + queriedItem + "\"\n" + preparedStatement.toString());
                    do {
                        logger.info(resultSet.getString(1) + "   " + resultSet.getString(2) + "    "
                            + resultSet.getDouble(3));
                        count++;
                    } while (resultSet.next() && count < 3);
                }
            }

            // end Timer
            final long end = System.currentTimeMillis();

            logger.info("PROCESS COMPLETED..FETCH TIME => " + (end - start) + " ms.");

            logger.info("\n  -----------------------      ------------------------------ \n");

        } catch (SQLException e) {
            logger.error("Statement Creation Failed!" + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                    resultSet = null;
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                    preparedStatement = null;
                }
                if (connection != null) {
                    connection.close();
                    connection = null;
                }

            } catch (SQLException ex) {
                logger.error("Statement Creation Failed!" + ex.getMessage());

            }

        }
    }

    /**
	 * 
	 */
    public static void registerDriver()
    {
        try {

            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
            e.printStackTrace();
            return;

        }
    }
}

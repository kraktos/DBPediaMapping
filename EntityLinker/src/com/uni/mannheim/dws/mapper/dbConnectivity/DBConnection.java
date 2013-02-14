/**
 * 
 */
package com.uni.mannheim.dws.mapper.dbConnectivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.uni.mannheim.dws.mapper.helper.util.Utilities;
import com.uni.mannheim.dws.mapper.message.Messages;

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
     * initialize the DB in the constructor
     * @throws SQLException 
     */
    public DBConnection() throws SQLException
    {
        initDB();
    }

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

    public Connection initDB() throws SQLException
    {

        // setConnection(DriverManager.getConnection(connectionURL + dbName, dbUser, dbUserPassword));
        this.connection = DriverManager.getConnection(connectionURL + dbName, dbUser, dbUserPassword);

        if (this.connection != null) {
            return getConnection();

        } else {
            logger.info("Failed to make connection!");
        }

        return null;

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

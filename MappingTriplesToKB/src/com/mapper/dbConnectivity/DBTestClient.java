/**
 * 
 */
package com.mapper.dbConnectivity;

import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mapper.message.Messages;

/**
 * @author Arnab Dutta
 * 
 */
public class DBTestClient {

	// logger
	static Logger logger = Logger.getLogger(DBTestClient.class.getName());

	// query string to fetch only subjects and objects
	private static final String USER_QUERY_ENTITIES = Messages
			.getString("QUERY_ENTITIES");

	// query stirng to fetch properties only
	private static final String USER_QUERY_PROP = Messages
			.getString("QUERY_PROPERTIES");

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Statement statement = null;
		ResultSet resultSet = null;

		String testSubject = "kathy";
		String testPropString = "star";
		String testObject = "mise";

		logger.info("-------- PostgreSQL "
				+ "JDBC Connection Testing ------------");

		DBConnection.performDBOperation(statement, resultSet, testSubject,
				USER_QUERY_ENTITIES);

		DBConnection.performDBOperation(statement, resultSet, testPropString,
				USER_QUERY_PROP);

		DBConnection.performDBOperation(statement, resultSet, testObject,
				USER_QUERY_ENTITIES);

	}

}

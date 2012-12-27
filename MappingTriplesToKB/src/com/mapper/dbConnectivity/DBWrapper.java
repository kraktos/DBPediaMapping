/**
 * 
 */
package com.mapper.dbConnectivity;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.mapper.message.Messages;

/**
 * @author Arnab Dutta
 */

public class DBWrapper implements Runnable
{
    // logger
    static Logger logger = Logger.getLogger(DBWrapper.class.getName());

    CountDownLatch latch;

    Statement statement = null;

    ResultSet resultSet = null;

    String queryItem;

    String query;

    public DBWrapper(String queryItem, String query, CountDownLatch latch)
    {
        super();

        this.queryItem = queryItem;
        this.query = query;
        this.latch = latch;

    }

    public void run()
    {
        try {
            latch.await(); // The thread keeps waiting till it is informed
            DBConnection.performDBOperation(statement, resultSet, queryItem, query);
            cleanup();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void cleanup()
    {
        this.queryItem = null;
        this.query = null;
        this.latch = null;

    }
}

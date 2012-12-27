/**
 * 
 */
package com.mapper.dbConnectivity;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.mapper.message.Messages;

/**
 * This class is a standalone client for testing the relative scores for a given set of SPO pattern.
 * 
 * @author Arnab Dutta
 */
public class DBClient
{
    // logger
    static Logger logger = Logger.getLogger(DBClient.class.getName());

    //
    // query string to fetch only subjects and objects
    private static final String USER_QUERY_ENTITIES = Messages.getString("QUERY_ENTITIES");

    // query stirng to fetch properties only
    private static final String USER_QUERY_PROP = Messages.getString("QUERY_PROPERTIES");

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String testSubject = "eastwood";
        String testPropString = "acted in";
        String testObject = "tv series";
        CountDownLatch latch = new CountDownLatch(1);

        Thread t1 = new Thread(new DBWrapper(testSubject, USER_QUERY_ENTITIES, latch));
        Thread t2 = new Thread(new DBWrapper(testPropString, USER_QUERY_PROP, latch));
        Thread t3 = new Thread(new DBWrapper(testObject, USER_QUERY_ENTITIES, latch));
        t1.start();
        t2.start();
        t3.start();
        latch.countDown();

    }

    public static void findMatches(String subject, String property, String object)
    {
        CountDownLatch latch = new CountDownLatch(1);

        Thread t1 = new Thread(new DBWrapper(subject, USER_QUERY_ENTITIES, latch));
        Thread t2 = new Thread(new DBWrapper(property, USER_QUERY_PROP, latch));
        Thread t3 = new Thread(new DBWrapper(object, USER_QUERY_ENTITIES, latch));
        t1.start();
        t2.start();
        t3.start();
        latch.countDown();
        
        t1 = null;
        t2 = null;
        t3 = null;

    }

}

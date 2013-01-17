/**
 * 
 */
package com.mapper.relationMatcher;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.mapper.search.QueryEngine;
import com.mapper.utility.Utilities;

/**
 * This is a wrapper class on top of the DBPedia Indices. Enables to perform a search of the query term on the indexed
 * DBPedia data. Makes a call to {@link QueryEngine}. This is a multi-threaded class, essentially enhancing performance
 * by making parallel search over the indices
 * 
 * @author Arnab Dutta
 */
public class QueryAPIWrapper implements Callable // implements Runnable
    <List<ResultDAO>>
{
    // define Logger
    static Logger logger = Logger.getLogger(QueryAPIWrapper.class.getName());

    // the query term
    String queryTerm;

    long startTime;

    /**
     * @param queryTerm
     */
    public QueryAPIWrapper(String queryTerm, long start)
    {
        this.queryTerm = queryTerm;
        this.startTime = start;
    }

    @Override
    public List<ResultDAO> call() throws Exception
    {
        String topMathchedEntity = null;
        List<ResultDAO> list = null;
        try {

            // make the search operation
            list = QueryEngine.doSearch(this.queryTerm);

            // topMathchedEntity = list.get(0);

            // Let the thread sleep for a while.
            Thread.sleep(500);
            Utilities.endTimer(this.startTime, "ANSWERED IN ");

        } catch (InterruptedException e) {
            logger.error("Child interrupted.");
        } catch (Exception e) {
            logger.error("Error while searching for the term " + this.queryTerm + ";  " + e.getMessage());
        } finally {
            // list.clear();
            // list = null;
        }
        // return topMathchedEntity;
        return list;
    }

}

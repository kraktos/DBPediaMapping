/**
 * 
 */
package de.dws.mapper.wrapper;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.dws.helper.dataObject.ResultDAO;
import de.dws.helper.util.Constants;
import de.dws.mapper.engine.query.QueryEngine;

/**
 * This is a wrapper class on top of the DBPedia Indices. Enables to perform a search of the query term on the indexed
 * DBPedia data. Makes a call to {@link QueryEngine}. This is a multi-threaded class, essentially enhancing performance
 * by making parallel search over the indices
 * 
 * @author Arnab Dutta
 */
public class QueryAPIWrapper implements Callable<List<ResultDAO>>
{
    // define Logger
    static Logger logger = Logger.getLogger(QueryAPIWrapper.class.getName());

    // the query term
    String queryTerm;

    /**
     * @param queryTerm
     */
    public QueryAPIWrapper(String queryTerm)
    {
        this.queryTerm = queryTerm;
    }

    public List<ResultDAO> call() throws Exception
    {
        List<ResultDAO> list = null;
        try {
            // make the search operation
            list = QueryEngine.doSearch(this.queryTerm, new File(Constants.DBPEDIA_ENT_INDEX_DIR));

            // Let the thread sleep for a while.
            Thread.sleep(500);

        } catch (InterruptedException e) {
            logger.error("Child interrupted.");
        } catch (Exception e) {
            logger.error("Error while searching for the term " + this.queryTerm + ";  " + e.getMessage());
        }
        return list;
    }

}

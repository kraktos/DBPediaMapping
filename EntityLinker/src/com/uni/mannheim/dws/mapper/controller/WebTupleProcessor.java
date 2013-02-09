/**
 * 
 */
package com.uni.mannheim.dws.mapper.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.uni.mannheim.dws.mapper.engine.query.QueryEngine;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;

/**
 * This class is responsible for processing the tuples coming from Web Interface
 * 
 * @author Arnab Dutta
 */
public class WebTupleProcessor implements ITupleProcessor
{
    // define Logger
    static Logger logger = Logger.getLogger(WebTupleProcessor.class.getName());

    // The variable for the incoming subject query terms
    private String subject;

    // The variable for the incoming object query terms
    private String object;

    // The variable for the incoming predicate query terms
    private String predicate;

    // pool of threads to be initiated
    private ExecutorService pool;

    // return list to hold the result set(subjects and objects)
    private List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();

    // return list to hold the predicates
    private List<ResultDAO> retListPredLookUp = new ArrayList<ResultDAO>();

    private List<ResultDAO> retListPredSearch = new ArrayList<ResultDAO>();

    /**
     * @return the retListPredLookUp
     */
    public List<ResultDAO> getRetListPredLookUp()
    {
        return retListPredLookUp;
    }

    /**
     * @return the retListPredSearch
     */
    public List<ResultDAO> getRetListPredSearch()
    {
        return retListPredSearch;
    }

    /**
     * @return the retList
     */
    public List<List<ResultDAO>> getRetList()
    {
        return retList;
    }

    /**
     * @param pool
     * @param subject
     * @param object
     * @param predicate
     */
    public WebTupleProcessor(ExecutorService pool, String subject, String object, String predicate)
    {
        this.pool = pool;
        this.subject = subject;
        this.object = object;
        this.predicate = predicate;
    }

    /*
     * (non-Javadoc)
     * @see com.mapper.relationMatcher.TupleProcessor#processTuples(java.lang.String)
     */
    public void processTuples(String dataFilePath) throws IOException, InterruptedException, ExecutionException
    {

        File file = null;

        logger.info(this.subject + " | " + this.predicate + " | " + this.object);
        if (!subject.equals("Subject") && !subject.equals("") && !object.equals("Object") && !object.equals("")) {
            this.retList = QueryEngine.performSearch(this.pool, this.subject, this.object);
        }
        if (!predicate.equals("Predicate") && !predicate.equals("")) {
            // create File object of our index directory
            file = new File(Constants.DBPEDIA_PROP_INDEX_DIR);

            this.retListPredLookUp = QueryEngine.doLookUpSearch(predicate);
            this.retListPredSearch = QueryEngine.doSearch(predicate, file);
        }
    }

}

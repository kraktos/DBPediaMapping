/**
 * 
 */
package com.mapper.relationMatcher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

/**
 * This class is responsible for processing the tuples coming from Web Interface
 * 
 * @author Arnab Dutta
 */
public class WebTupleProcessor implements TupleProcessor
{
    // define Logger
    static Logger logger = Logger.getLogger(WebTupleProcessor.class.getName());

    // The variable for the incoming subject query terms
    private String subject;

    // The variable for the incoming object query terms
    private String object;

    // The variable for the incoming predicate query terms
    private String predicate;

    /**
     * @param subject
     * @param object
     * @param predicate
     */
    public WebTupleProcessor(String subject, String object, String predicate)
    {
        this.subject = subject;
        this.object = object;
        this.predicate = predicate;
    }

    /*
     * (non-Javadoc)
     * @see com.mapper.relationMatcher.TupleProcessor#processTuples(java.lang.String)
     */
    @Override
    public void processTuples(String dataFilePath) throws IOException, InterruptedException, ExecutionException
    {
        // here we will have no such data file. Just the query terms coming directly from the web interface
        

        logger.info(this.subject + " | " + this.predicate + " | " + this.object);

    }

}

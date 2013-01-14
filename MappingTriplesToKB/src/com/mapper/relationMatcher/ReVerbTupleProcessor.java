/**
 * 
 */
package com.mapper.relationMatcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.mapper.search.QueryEngine;
import com.mapper.utility.Constants;

/**
 * This class tries to parse the tuples generated from ReVerb IE engine and processes them by each tuple. See
 * {@link TupleProcessor}
 * 
 * @author Arnab Dutta
 */
public class ReVerbTupleProcessor implements TupleProcessor
{

    // define Logger
    static Logger logger = Logger.getLogger(ReVerbTupleProcessor.class.getName());

    /*
     * (non-Javadoc)
     * @see com.mapper.relationMatcher.TupleProcessor#processTuples(java.lang.String)
     */
    @Override
    public void processTuples(final String dataFilePath) throws IOException, InterruptedException, ExecutionException
    {
        // open the file stream on the file
        BufferedReader tupleReader = new BufferedReader(new FileReader(dataFilePath));
        String[] strTokens = null;
        String subject;
        String predicate;
        String object;

        if (tupleReader != null) {
            String tupleFromIE;
            while ((tupleFromIE = tupleReader.readLine()) != null) {
                // process with each of these tuples
                // logger.info(tupleFromIE + "\n");
                strTokens = tupleFromIE.split(Constants.REVERB_IE_DELIMIT);
                // strtokens[0] => id
                // strtokens[1] => Subject
                // strtokens[2] => predicate
                // strtokens[3] => object
                // strtokens[4] => Subject (almost similar to the former one but all small caps-ed)
                // strtokens[5] => stemmed and just root predicates (is grown -> to grow)
                // strtokens[6] => objects small caps-ed
                // strtokens[7] => ?
                // strtokens[8] => confidence
                // strtokens[9] => source of extraction

                // extract the subject, predicate and object out of each tuple
                subject = (strTokens[4] != null) ? strTokens[4] : "";
                predicate = (strTokens[5] != null) ? strTokens[5] : "";
                object = (strTokens[6] != null) ? strTokens[6] : "";

                logger.info(subject + " | " + predicate + " | " + object);
                // fetch the equivalent DBPedia entities
                List<String> retList = QueryEngine.performSearch(subject, object);

                // use them to fetch the predicates they are linked with
                QueryEngine.fetchPredicates(retList.get(0), retList.get(1), predicate);
                System.out.print("\n\n");
            }

            logger.info(predicateSurfaceFormsMap);

        }
    }
}

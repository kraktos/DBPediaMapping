/**
 * 
 */
package com.mapper.relationMatcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.mapper.search.QueryEngine;
import com.mapper.utility.Constants;

/**
 * This class tries to parse the tuples generated from NELL IE engine and processes them by each tuple see
 * {@link TupleProcessor}
 * 
 * @author Arnab Dutta
 */
public class NellTupleProcessor implements TupleProcessor
{

    // define Logger
    static Logger logger = Logger.getLogger(NellTupleProcessor.class.getName());

    /*
     * (non-Javadoc)
     * @see com.mapper.relationMatcher.TupleProcessor#processTuples(java.lang.String)
     */
    @Override
    public void processTuples(String dataFilePath) throws IOException, InterruptedException, ExecutionException
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
                strTokens = tupleFromIE.split(Constants.NELL_IE_DELIMIT);
                // strtokens[0] => Subject
                // strtokens[1] => predicate
                // strtokens[2] => object
                // strtokens[3] => confidence

                // extract the subject, predicate and object out of each tuple
                subject = (strTokens[0] != null) ? strTokens[0] : "";
                predicate = (strTokens[1] != null) ? strTokens[1] : "";
                object = (strTokens[2] != null) ? strTokens[2] : "";

                logger.info(subject + " | " + predicate + " | " + object);

                // fetch the equivalent DBPedia entities
                List<List<ResultDAO>> retList = QueryEngine.performSearch(subject, object);

                // use them to fetch the predicates they are linked with
                QueryEngine.fetchPredicates(retList.get(0), retList.get(1), predicate);
                System.out.print("\n\n");
            }
            logger.info(predicateSurfaceFormsMap);

            findTheBestPrediction();
        }
    }

    private void findTheBestPrediction()
    {

        logger.info("IE predicates = " + TupleProcessor.iePredicatesCountMap.size());
        logger.info("DBPedia predicates = " + TupleProcessor.dbPediaPredicatesCountMap.size() + "  "
            + TupleProcessor.dbPediaPredicatesCountMap);

        // compute jaccard for each property from IE
        for (Map.Entry<String, HashMap<String, Integer>> entry : TupleProcessor.predicateSurfaceFormsMap.entrySet()) {
            String iePredicate = entry.getKey();
            int iePredicateCardinality = TupleProcessor.iePredicatesCountMap.get(iePredicate);
            for (Map.Entry<String, Integer> en : entry.getValue().entrySet()) {
                String dbPediaPredicate = en.getKey();
                int dbPediaPropCardinlity = TupleProcessor.dbPediaPredicatesCountMap.get(dbPediaPredicate);
                int dbPediaPropLocalCardinality = en.getValue();

                //jaccard score
                double score =
                    (double) (dbPediaPropLocalCardinality)
                        / (double) (iePredicateCardinality + dbPediaPropCardinlity - dbPediaPropLocalCardinality);

                logger.info(iePredicate + " vs " + dbPediaPredicate + " => " + score);
            }
        }
    }
}

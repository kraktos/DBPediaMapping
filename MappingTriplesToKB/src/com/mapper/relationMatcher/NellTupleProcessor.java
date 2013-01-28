/**
 * 
 */
package com.mapper.relationMatcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.mapper.dataObjects.PredicatesDAO;
import com.mapper.dataObjects.ResultDAO;
import com.mapper.search.QueryEngine;
import com.mapper.utility.Constants;
import com.mapper.utility.FileUtil;

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

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

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
                object =
                    (strTokens[2] != null & strTokens[2].length() > 0) ? strTokens[2] : ((strTokens[3] != null)
                        ? strTokens[3] : "");

                logger.info(subject + " | " + predicate + " | " + object);

                // fetch the equivalent DBPedia entities
                List<List<ResultDAO>> retList = QueryEngine.performSearch(pool, subject, object);

                // use them to fetch the predicates they are linked with
                QueryEngine.fetchPredicates(retList.get(0), retList.get(1), predicate);
                System.out.print("\n\n");
            }
            logger.info(predicateSurfaceFormsMap);

            findTheBestPrediction();
        }
    }

    /**
     * compute the scores for the possible set of matches and dump them to a file
     */
    private void findTheBestPrediction()
    {
        FileWriter fw;
        double score = 0;
        PredicatesDAO[] predDaoArr;

        String iePredicate;
        String dbPediaPredicate;

        int iePredicateCardinality;
        int dbPediaPropCardinlity;
        int dbPediaPropLocalCardinality;

        try {
            fw = new FileWriter(Constants.PREDICATE_FREQ_FILEPATH);
            BufferedWriter bw = new BufferedWriter(fw);

            logger.debug("IE predicates = " + TupleProcessor.iePredicatesCountMap.size());
            logger.debug("DBPedia predicates = " + TupleProcessor.dbPediaPredicatesCountMap.size() + "  "
                + TupleProcessor.dbPediaPredicatesCountMap);

            int count = 0;
            // compute jaccard for each property from IE
            for (Map.Entry<String, HashMap<String, Integer>> entry : TupleProcessor.predicateSurfaceFormsMap.entrySet()) {
                iePredicate = entry.getKey();
                iePredicateCardinality = TupleProcessor.iePredicatesCountMap.get(iePredicate);

                predDaoArr = new PredicatesDAO[entry.getValue().size()];

                for (Map.Entry<String, Integer> en : entry.getValue().entrySet()) {
                    dbPediaPredicate = en.getKey();
                    dbPediaPropCardinlity = TupleProcessor.dbPediaPredicatesCountMap.get(dbPediaPredicate);
                    dbPediaPropLocalCardinality = en.getValue();

                    // compute jaccard score
                    score =
                        (double) (dbPediaPropLocalCardinality)
                            / (double) (iePredicateCardinality + dbPediaPropCardinlity - dbPediaPropLocalCardinality);

                    // add to a List to be flushed to File
                    predDaoArr[count++] = new PredicatesDAO(dbPediaPredicate, score);
                    logger.debug(iePredicate + " vs " + dbPediaPredicate + " => " + score);
                }
                // reset counter
                count = 0;

                // sort on scores
                Arrays.sort(predDaoArr);

                // write to file
                FileUtil.dumpToFile(bw, iePredicate, predDaoArr);
            }
            bw.close();

        } catch (IOException e) {
            logger.error(e.getMessage());

        }

    }

}

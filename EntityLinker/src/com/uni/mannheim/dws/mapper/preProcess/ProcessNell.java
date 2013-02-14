/**
 * 
 */
package com.uni.mannheim.dws.mapper.preProcess;

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

import com.uni.mannheim.dws.mapper.controller.ITupleProcessor;
import com.uni.mannheim.dws.mapper.engine.query.QueryEngine;
import com.uni.mannheim.dws.mapper.helper.dataObject.PredicatesDAO;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.helper.util.FileUtil;

/**
 * @author Arnab Dutta
 */
public class ProcessNell
{
    // define Logger
    static Logger logger = Logger.getLogger(ProcessNell.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args)
    {

        try {
            new ProcessNell().processTuples(Constants.NELL_DATA_PATH);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());

        } catch (ExecutionException e) {
            logger.error(e.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());

        }

    }

    private void processTuples(String dataFilePath) throws InterruptedException, ExecutionException, IOException
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
                // strtokens[0] => Subject; strtokens[1] => predicate; strtokens[2] => object; strtokens[3] =>
                // confidence

                // extract the subject, predicate and object out of each tuple
                subject = (strTokens[1] != null) ? strTokens[1] : "";
                predicate = (strTokens[2] != null) ? strTokens[2] : "";
                object =
                    (strTokens[3] != null & strTokens[3].length() > 0) ? strTokens[3] : ((strTokens[4] != null)
                        ? strTokens[4] : "");


                logger.info("\n\n"+subject + " | " + predicate + " | " + object);

                // fetch the equivalent DBPedia entities
                List<List<ResultDAO>> retList = QueryEngine.performSearch(pool, subject, object);

                // use them to fetch the predicates they are linked with
                QueryEngine.fetchPredicates(retList.get(0), retList.get(1), predicate);
            }
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

            logger.debug("IE predicates = " + ITupleProcessor.iePredicatesCountMap.size());
            logger.debug("DBPedia predicates = " + ITupleProcessor.dbPediaPredicatesCountMap.size() + "  "
                + ITupleProcessor.dbPediaPredicatesCountMap);

            int count = 0;
            // compute jaccard for each property from IE
            for (Map.Entry<String, HashMap<String, Integer>> entry : ITupleProcessor.predicateSurfaceFormsMap
                .entrySet()) {
                iePredicate = entry.getKey();
                iePredicateCardinality = ITupleProcessor.iePredicatesCountMap.get(iePredicate);

                predDaoArr = new PredicatesDAO[entry.getValue().size()];

                for (Map.Entry<String, Integer> en : entry.getValue().entrySet()) {
                    dbPediaPredicate = en.getKey();
                    dbPediaPropCardinlity = ITupleProcessor.dbPediaPredicatesCountMap.get(dbPediaPredicate);
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
            // close the stream writer
            bw.close();

        } catch (IOException e) {
            logger.error(e.getMessage());

        }

    }

}

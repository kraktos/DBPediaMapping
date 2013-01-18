package com.mapper.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.mapper.indexer.DBPediaIndexBuilder;
import com.mapper.query.SPARQLEndPointQueryAPI;
import com.mapper.relationMatcher.QueryAPIWrapper;
import com.mapper.relationMatcher.ResultDAO;
import com.mapper.relationMatcher.TupleProcessor;
import com.mapper.utility.Constants;
import com.mapper.utility.Utilities;

public class QueryEngine
{

    private static int TOP_K = Constants.TOPK;

    private static float SIM = Constants.SIMILARITY;

    // Default Constructor
    public QueryEngine()
    {

    }

    public static void setTopK(int topK)
    {
        TOP_K = topK;
    }

    public static void setSimilarity(float sim)
    {
        SIM = sim;
    }

    // logger
    public static Logger logger = Logger.getLogger(QueryEngine.class.getName());

    /**
     * method accepts a user query and fetches over the indexed DBPedia data
     * 
     * @param userQuery the user provided search item
     * @return A List containing the matching DBPedia Entity URI as value
     * @throws Exception
     */
    public static List<ResultDAO> doSearch(String userQuery) throws Exception
    {
        IndexReader reader = null;
        IndexSearcher searcher = null;

        Set<String> setURI = new HashSet<String>();
        List<ResultDAO> returnList = new ArrayList<ResultDAO>();

        String labelField = null;
        String uriField = null;

        long start = 0;

        try {

            // flag to determine if u need to recompute the indices
            if (Constants.INDEX_AGAIN) {
                DBPediaIndexBuilder.indexer();
            }
            // start timer
            start = Utilities.startTimer();

            // create File object of our index directory
            File file = new File(Constants.DBPEDIA_INDEX_DIR);

            // create index reader object
            // reader = IndexReader.open(FSDirectory.open(file));
            reader = DirectoryReader.open(FSDirectory.open(file));

            // create index searcher object
            searcher = new IndexSearcher(reader);

            // create the fuzzy query

            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("labelField", userQuery));

            // execute the search on top results
            TopDocs hits = searcher.search(fuzzyQuery, null, Constants.MAX_RESULTS);

            if (hits.totalHits == 0)
                throw new Exception();

            // iterate the results
            for (ScoreDoc scoredoc : hits.scoreDocs) {
                // Retrieve the matched document and show relevant details
                Document doc = searcher.doc(scoredoc.doc);

                // uriField = doc.getFieldable("uriField").stringValue();
                // labelField = doc.getFieldable("labelField").stringValue();

                uriField = doc.get("uriField");
                labelField = doc.get("labelField");
                double score = scoredoc.score / hits.getMaxScore();

                // only add the unique entries(URI and label combination)
                boolean isUnique = Utilities.checkUniqueness(setURI, uriField + labelField);
                if (isUnique) {
                    logger.info(labelField + " => " + uriField + "   " + score);
                    returnList.add(new ResultDAO(uriField, Math.round(score * 100.0)));
                    // we are interested in only the top k results
                    if (setURI.size() == TOP_K) {
                        return returnList;
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("NO MATCHING RECORDS FOUND !! ");
        } finally {
            setURI.clear();
            setURI = null;
            Utilities.endTimer(start, "QUERY ANSWERED IN ");
        }
        return returnList;
    }

    /**
     * wrapper method to spawn the actual search operation being carried out
     * 
     * @param subjFromTuple
     * @param objFromTuple
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static List<List<ResultDAO>> performSearch(final String subjFromTuple, final String objFromTuple)
        throws InterruptedException, ExecutionException
    {
        List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();
        long start = Utilities.startTimer();

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // The idea is we parallely process the two queries simultaneously and receive back the results
        // to the main thread i.e here. This is not possible with Thread class.
        Future<List<ResultDAO>> subjTask = pool.submit(new QueryAPIWrapper(subjFromTuple, start));
        Future<List<ResultDAO>> objTask = pool.submit(new QueryAPIWrapper(objFromTuple, start));

        // receive back the results from the two thread runners
        // and add them to the return collection
        retList.add(subjTask.get());
        retList.add(objTask.get());

        return retList;
    }

    @SuppressWarnings("unchecked")
    /**
     * takes a subject and object from the DBPedia and tries to find all possible set of predicates connecting
     * these two two entities (subject and object)
     * 
     * @param subject the DBPedia entity occurring as subject
     * @param object the DBPedia entity occurring as object
     * @param actualPredicate the predicate coming form IE engines
     */
    public static void fetchPredicates(final String dbPediaSubj, final String dbPediaObj,
        final String actualPredicateFromIE)
    {
        logger.info(dbPediaSubj + " " + dbPediaObj);

        String sparqlQuery =
            "select ?predicates where {{<" + dbPediaSubj + "> ?predicates <" + dbPediaObj + ">} UNION {<" + dbPediaObj
                + "> ?predicates <" + dbPediaSubj + ">}}";

        // String sparqlQuery = "select ?predicates where {<" + dbPediaSubj + "> ?predicates <" + dbPediaObj + ">}";

        logger.info(sparqlQuery);

        // fetch the result set
        ResultSet results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);

        // store in a local collection to iterate
        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        // if we have some results proceed
        if (listResults.size() > 0) {
            List<String> listVarnames = results.getResultVars();
            logger.info("'" + actualPredicateFromIE + "'" + " matches => ");
            // this is a possible set of matches for the given predicate
            for (QuerySolution querySol : listResults) {
                String matchedProp = querySol.get(listVarnames.get(0)).toString();
                logger.info(matchedProp + "  ");

                // update the count for all such possibilities for a given predicate
                updatePredicateMap(actualPredicateFromIE, matchedProp);
            }
        }
    }

    /**
     * @param actualPredicateFromIE
     * @param matchedProp
     */
    public static void updatePredicateMap(final String actualPredicateFromIE, String matchedProp)
    {
        HashMap<String, Integer> propertyVsCountMap = null;
        // the map contains the key already, then just update its value map
        if (TupleProcessor.predicateSurfaceFormsMap.containsKey(actualPredicateFromIE)) {
            // update the count of this property first
            int countIEPredicate = TupleProcessor.iePredicatesCountMap.get(actualPredicateFromIE);
            TupleProcessor.iePredicatesCountMap.put(actualPredicateFromIE, countIEPredicate + 1);

            // retrieve the whole map which is the value against "actualPredicateFromIE"
            propertyVsCountMap = TupleProcessor.predicateSurfaceFormsMap.get(actualPredicateFromIE);

            // if the current key is occurring in this map, increment its value
            if (propertyVsCountMap.containsKey(matchedProp)) {
                int value = propertyVsCountMap.get(matchedProp);
                propertyVsCountMap.put(matchedProp, value + 1);

                // add also keep a global count of the occurrence of this "matchedProp"
                int dbPediaPropCount = TupleProcessor.dbPediaPredicatesCountMap.get(matchedProp);
                TupleProcessor.dbPediaPredicatesCountMap.put(matchedProp, dbPediaPropCount + 1);

            } else { // add this new key as a possible prediction of the surface form
                propertyVsCountMap.put(matchedProp, 1);
                TupleProcessor.dbPediaPredicatesCountMap.put(matchedProp, 1);
            }
        } else { // add a new entry i.e a new predicate from IE data set
            propertyVsCountMap = new HashMap<String, Integer>();
            propertyVsCountMap.put(matchedProp, 1);
            TupleProcessor.predicateSurfaceFormsMap.put(actualPredicateFromIE, propertyVsCountMap);
            TupleProcessor.iePredicatesCountMap.put(actualPredicateFromIE, 1);
            TupleProcessor.dbPediaPredicatesCountMap.put(matchedProp, 1);
        }
    }

    public static void main(String[] ar) throws Exception
    {
        doSearch(Constants.SAMPLE_QUERY);
    }
}

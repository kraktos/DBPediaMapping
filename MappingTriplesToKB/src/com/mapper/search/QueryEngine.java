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
import com.mapper.relationMatcher.TupleProcessor;
import com.mapper.utility.Constants;
import com.mapper.utility.Utilities;

public class QueryEngine
{
    // logger
    public static Logger logger = Logger.getLogger(QueryEngine.class.getName());

    /**
     * method accepts a user query and fetches over the indexed DBPedia data
     * 
     * @param userQuery the user provided search item
     * @return A List containing the matching DBPedia Entity URI as value
     * @throws Exception
     */
    public static List<String> doSearch(final String userQuery) throws Exception
    {
        IndexReader reader = null;
        IndexSearcher searcher = null;

        Set<String> setURI = new HashSet<String>();
        List<String> returnList = new ArrayList<String>();

        String labelField = null;
        String uriField = null;

        try {

            if (Constants.INDEX_AGAIN) {
                DBPediaIndexBuilder.indexer();
            }
            long start = Utilities.startTimer();

            // create File object of our index directory
            File file = new File(Constants.DBPEDIA_INDEX_DIR);

            // create index reader object
            reader = IndexReader.open(FSDirectory.open(file));

            // create index searcher object
            searcher = new IndexSearcher(reader);

            // create the query term
            Term term = new Term("labelCapsField", userQuery.toUpperCase());
            FuzzyQuery fuzzyQuery = new FuzzyQuery(term, Constants.SIMILARITY);

            // execute the search on top results
            TopDocs hits = searcher.search(fuzzyQuery, null, Constants.MAX_RESULTS);

            if (hits.totalHits == 0)
                throw new Exception();

            for (ScoreDoc scoredoc : hits.scoreDocs) {
                // Retrieve the matched document and show relevant details
                Document doc = searcher.doc(scoredoc.doc);

                uriField = doc.getFieldable("uriField").stringValue();
                labelField = doc.getFieldable("labelField").stringValue();
                double score = scoredoc.score / hits.getMaxScore();

                // TODO
                boolean isUnique = Utilities.checkUniqueness(setURI, uriField + labelField);
                if (isUnique) {
                    // logger.info(labelField + " => " + uriField + "   " + score);
                    returnList.add(uriField);
                    // we are interested in
                    if (setURI.size() == Constants.TOPK) {
                        return returnList;
                    }
                }
            }

            Utilities.endTimer(start, "QUERY ANSWERED IN ");

        } catch (Exception ex) {

            logger.error("NO MATCHING RECORDS FOUND !! ");
        } finally {
            setURI.clear();
            setURI = null;
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
    public static List<String> performSearch(final String subjFromTuple, final String objFromTuple)
        throws InterruptedException, ExecutionException
    {
        List<String> retList = new ArrayList<String>();
        long start = Utilities.startTimer();

        // we just need two threads to perform the search
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // The idea is we parallely process the two queries simultaneously and receive back the results
        // to the main thread i.e here. This is not possible with Thread class.
        Future<String> subjTask = pool.submit(new QueryAPIWrapper(subjFromTuple, start));
        Future<String> objTask = pool.submit(new QueryAPIWrapper(objFromTuple, start));

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
     * @param subject the DBPedia entity occuring as subject
     * @param object the DBPedia entity occuring as object
     * @param actualPredicate the predicate coming form IE engines
     */
    public static void fetchPredicates(final String dbPediaSubj, final String dbPediaObj,
        final String actualPredicateFromIE)
    {
        logger.info(dbPediaSubj + " " + dbPediaObj);

        String sparqlQuery =
            "select ?predicates where {{<" + dbPediaSubj + "> ?predicates <" + dbPediaObj + ">} UNION {<" + dbPediaObj
                + "> ?predicates <" + dbPediaSubj + ">}}";
        logger.info(sparqlQuery);

        ResultSet results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);
        List<QuerySolution> listResults = ResultSetFormatter.toList(results);

        // if we have some results proceed
        if (listResults.size() > 0) {
            List<String> listVarnames = results.getResultVars();
            logger.info("'" + actualPredicateFromIE + "'" + " matches => ");
            // this is a possible set of matches for the given predicate
            for (QuerySolution querySol : listResults) {
                String key = querySol.get(listVarnames.get(0)).toString();
                logger.info(key + "  ");

                // update the count for all such possibilities for a given predicate
                updatePredicateMap(actualPredicateFromIE, key);
            }
        }

    }

    /**
     * @param actualPredicateFromIE
     * @param key
     */
    public static void updatePredicateMap(final String actualPredicateFromIE, String key)
    {
        HashMap<String, Integer> propertyVsCountMap = null;
        // the map contains the key already, then just update its value map
        if (TupleProcessor.predicateSurfaceFormsMap.containsKey(actualPredicateFromIE)) {
            // retrieve the whole map first
            propertyVsCountMap = TupleProcessor.predicateSurfaceFormsMap.get(actualPredicateFromIE);
            // if the current key is occurring in this map, increment its value
            if (propertyVsCountMap.containsKey(key)) {
                int value = propertyVsCountMap.get(key);
                propertyVsCountMap.put(key, value + 1);
            } else { // add this new key as a possible prediction of the surface form
                propertyVsCountMap.put(key, 1);
            }
        } else { // add a new entry i.e a new predicate from IE data set
            propertyVsCountMap = new HashMap<String, Integer>();
            propertyVsCountMap.put(key, 1);
            TupleProcessor.predicateSurfaceFormsMap.put(actualPredicateFromIE, propertyVsCountMap);
        }
    }

    public static void main(String[] ar) throws Exception
    {
        doSearch(Constants.SAMPLE_QUERY);
    }
}

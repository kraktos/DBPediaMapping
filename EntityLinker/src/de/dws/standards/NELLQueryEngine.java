
package de.dws.standards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.dws.mapper.controller.ITupleProcessor;
import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.engine.index.DBPediaIndexBuilder;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import de.dws.mapper.helper.dataObject.ResultDAO;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.helper.util.Utilities;
import de.dws.mapper.wrapper.QueryAPIWrapper;
import de.dws.nlp.dao.FreeFormFactDao;

/**
 * This class is an API for making query over the DBPedia indices
 * 
 * @author Arnab Dutta
 */
public class NELLQueryEngine
{

    // The top k best matching results,
    private static int TOP_K = Constants.TOPK;

    // Default Constructor
    public NELLQueryEngine()
    {

    }

    // setter for Top K parameter
    public static void setTopK(int topK)
    {
        TOP_K = topK;
    }

    // logger
    public static Logger logger = Logger.getLogger(NELLQueryEngine.class.getName());

    // DB connection instance, one per servlet
    static Connection connection = null;

    // prepared statement instance
    static PreparedStatement pstmt = null;

    /**
     * method accepts a user query and fetches over the indexed DBPedia data
     * 
     * @param subQuery the user provided search item
     * @param objQuery
     * @param file
     * @return A List containing the matching DBPedia Entity URI as value
     * @throws Exception
     */
    public static List<FreeFormFactDao> doSearch(String subQuery, String objQuery)
            throws IOException
    {

        // create File object of our index directory
        File file = new File(Constants.NELL_ENT_INDEX_DIR);

        IndexReader reader = null;
        IndexSearcher searcher = null;

        Set<String> setURI = new HashSet<String>();
        List<ResultDAO> returnList = new ArrayList<ResultDAO>();
        // allows for natural ordering on the ascending value of key
        Map<Integer, List<ResultDAO>> resultMap = new TreeMap<Integer, List<ResultDAO>>();

        long start = 0;

        TopDocs hits = null;

        try {

            // start timer
            start = Utilities.startTimer();

            // create index reader object
            // reader = IndexReader.open(FSDirectory.open(file));
            reader = DirectoryReader.open(FSDirectory.open(file));

            // create index searcher object
            searcher = new IndexSearcher(reader);

            // remove any un-necessary punctuation marks from the query
            // userQuery =
            // Pattern.compile("['_\\s]").matcher(userQuery).replaceAll("");

            // frame a query on the surname field
            BooleanQuery query = frameQuery(subQuery, objQuery);

            // execute the search on top results
            hits = searcher.search(query, null, Constants.MAX_RESULTS);

            return iterateResult(searcher, hits, subQuery);

        } catch (Exception ex) {
            logger.debug("NO MATCHING RECORDS FOUND FOR QUERY \"" + subQuery + "\" !! ");
        } finally {
            setURI.clear();
            setURI = null;
            Utilities.endTimer(start, "QUERY \"" + subQuery + "\" ANSWERED IN ");
        }
        return null;
    }

    /**
     * @param userQuery user input term
     * @param field1 first argument denoting the index field to match to
     * @return a Boolean query where both the indices should be matched
     */
    public static BooleanQuery frameQuery(String subjQuery, String objQuery)
    {
        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(new TermQuery(new Term("subjField", subjQuery.toLowerCase())),
                BooleanClause.Occur.MUST);
        subQuery.add(new TermQuery(new Term("objField", objQuery.toLowerCase())),
                BooleanClause.Occur.MUST);

        return subQuery;
    }

    /**
     * @param searcher searcher instance
     * @param setURI a set to identify the unique URI s
     * @param resultMap a result map sorted by best matches
     * @param hits document hits instance
     * @param userQuery the user input coming from web interface or extraction
     *            engines
     * @return
     * @throws IOException
     */
    public static List<FreeFormFactDao> iterateResult(IndexSearcher searcher, TopDocs hits,
            String userQuery)
            throws IOException
    {

        List<FreeFormFactDao> nellTriplesList = new ArrayList<FreeFormFactDao>();

        String subject = null;
        String predicate = null;
        String object = null;

        double score;

        // iterate over the results fetched after index search
        for (ScoreDoc scoredoc : hits.scoreDocs) {
            // Retrieve the matched document and show relevant details
            Document doc = searcher.doc(scoredoc.doc);

            subject = doc.get("subjField");
            predicate = doc.get("predField");
            object = doc.get("objField");

            score = scoredoc.score / hits.getMaxScore();

            logger.debug(subject + "  " + predicate + "  " + object + " " + score);
            nellTriplesList.add(new FreeFormFactDao(subject, predicate, object));

        }
        return nellTriplesList;
    }

    // stand alone test point
    public static void main(String[] ar) throws Exception
    {

        // flag to determine if u need to recompute the indices
        if (Constants.INDEX_AGAIN) {
            DBPediaIndexBuilder.indexer();
        }

        doSearch("mark", "diane");
    }

}

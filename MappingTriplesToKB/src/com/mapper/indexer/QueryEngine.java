package com.mapper.indexer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

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
     * @throws Exception
     */
    public static void QuerySearcher(final String userQuery) throws Exception
    {
        IndexReader reader = null;
        IndexSearcher searcher = null;

        try {
            // store the parameter value in query variable

            if (Constants.INDEX) {
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
            FuzzyQuery fuzzyQuery = new FuzzyQuery(term, .1F);

            // execute the search on top results
            TopDocs hits = searcher.search(fuzzyQuery, null, 100);

            final Set<String> set = new HashSet<String>();

            if (hits.totalHits == 0)
                throw new Exception();

            for (ScoreDoc scoredoc : hits.scoreDocs) {
                // Retrieve the matched document and show relevant details
                Document doc = searcher.doc(scoredoc.doc);
                boolean isUnique = Utilities.checkUniqueness(set, doc.getFieldable("uriField").stringValue());
                if (isUnique) {
                    logger.info(doc.getFieldable("labelField").stringValue() + " => "
                        + doc.getFieldable("uriField").stringValue() + "   " + scoredoc.score / hits.getMaxScore());

                    if (set.size() == Constants.TOPK)
                        break;
                }
            }

            Utilities.endTimer(start, "QUERY ANSWERED IN ");

        } catch (Exception ex) {

            logger.error("NO MATCHING RECORDS FOUND !! ");
        }
    }

    public static void main(String[] ar) throws Exception
    {
        QuerySearcher(Constants.SAMPLE_QUERY);
    }
}


package de.dws.standards;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import de.dws.helper.util.Constants;
import de.dws.mapper.engine.index.DBPediaIndexBuilder;
import de.dws.nlp.dao.FreeFormFactDao;

/**
 * This class is an API for making query over the indices over N3 data
 * 
 * @author Arnab Dutta
 */
public class TripleIndexQueryEngine
{

    // logger
    public static Logger logger = Logger.getLogger(TripleIndexQueryEngine.class.getName());

    /**
     * searcher instance
     */
    private final IndexSearcher searcher;

    // Default Constructor
    public TripleIndexQueryEngine(String indexLocation)
    {
        try
        {
            // create File object of our index directory
            File file = new File(indexLocation);// Constants.DBPEDIA_INFO_INDEX_DIR);

            IndexReader reader =
                    // create index reader object
                    DirectoryReader.open(FSDirectory.open(file));

            // create index searcher object
            searcher = new IndexSearcher(reader);
        } catch (IOException ioe)
        {
            throw new RuntimeException("Cannot init: " + ioe);
        }
    }

    /**
     * method accepts a user query and fetches over the indexed DBPedia data
     * 
     * @param subQuery the user provided search item
     * @param objQuery
     * @param delimit
     * @param string
     * @param file
     * @return A List containing the matching DBPedia Entity URI as value
     * @throws Exception
     */
    public List<FreeFormFactDao> doSearch(String subQuery, String objQuery, String delimit)
            throws IOException
    {
        TopDocs hits = null;

        try {
            // frame a query on the surname field
            BooleanQuery query = frameQuery(subQuery, objQuery);

            // execute the search on top results
            hits = searcher.search(query, null, Constants.MAX_RESULTS);

            return iterateResult(searcher, hits, subQuery, delimit);

        } catch (Exception ex) {
            logger.error("NO MATCHING RECORDS FOUND FOR QUERY \"" + subQuery + "\" !! ");
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
     * @param delimit
     * @return
     * @throws IOException
     */
    public static List<FreeFormFactDao> iterateResult(IndexSearcher searcher, TopDocs hits,
            String userQuery, String delimit)
            throws IOException
    {

        List<FreeFormFactDao> triplesList = new ArrayList<FreeFormFactDao>();

        String triple = null;

        double score;
        String[] elems = null;

        // iterate over the results fetched after index search
        for (ScoreDoc scoredoc : hits.scoreDocs) {
            // Retrieve the matched document and show relevant details
            Document doc = searcher.doc(scoredoc.doc);

            // retrieve the triple as a whole
            triple = doc.get("tripleField");

            score = scoredoc.score / hits.getMaxScore();

            logger.debug(triple);

            elems = triple.split(delimit);

            triplesList.add(new FreeFormFactDao(elems[0], elems[1], elems[2]));
        }
        return triplesList;
    }

    // stand alone test point
    public static void main(String[] ar) throws Exception
    {

        // flag to determine if u need to recompute the indices
        if (Constants.INDEX_AGAIN) {
            DBPediaIndexBuilder.indexer();
        }
       
        new TripleIndexQueryEngine(Constants.NELL_ENT_INDEX_DIR).doSearch("microsoft",
                "bill_gates", Constants.NELL_IE_DELIMIT);
    }

}

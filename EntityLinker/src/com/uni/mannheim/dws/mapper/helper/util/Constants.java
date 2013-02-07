/**
 * 
 */
package com.uni.mannheim.dws.mapper.helper.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * This class stores a set of constants required for the application
 * 
 * @author Arnab Dutta
 */
public class Constants
{
    public static final String DELIMIT = "\",";

    public static final String DELIMIT_IE_FILE = ",";

    // *****************DIRECTORY LOCATIONS ************************************************
    // location of all the raw CSV files dumped from DBPedia SPARQL endpoint
    public static final String DBPEDIA_DATA_DIR = "/home/arnab/Work/data/DBPedia/data";

    // location of the directory where the indices are stored
    public static final String DBPEDIA_INDEX_DIR = "/home/arnab/Work/data/DBPedia/indexFiles";

    // Delimiter to separate the URI and the lable of DBPedia entries
    public static final String DBPEDIA_DATA_DELIMIT = "~!~";

    // pnly the URIs with the following header will be used for indexing
    public static final String DBPEDIA_HEADER = "http://dbpedia.org/";

    // *****************INDEXING STRATEGIES ************************************************
    // change here to use different analyzers
    public static final Analyzer LUCENE_ANALYZER = new StandardAnalyzer(Version.LUCENE_40);

    // flag to determine whether to concat to old indices or recreate all from scratch
    public static final boolean EMPTY_INDICES = true;

    // Flag to denote if indexing is to be done or query on old indices
    public static final boolean INDEX_AGAIN = false;

    // *****************FETCH STRATEGIES ***************************************************
    // tweaking these can dramatically effect the query response time

    // percentage length of common (non-fuzzy) prefix in the user query you want to match, higher value
    // makes it to search over smaller data matches not on all of them. Here it means 80% of the query term
    // should contain in the result sets
    public static final float PREFIX_LENGTH_PERCENT = 0.1F;

    // default 50% similarity and above, lower this value to fetch even lesser similar items
    public static final float SIMILARITY = 0.1F;

    // change the value to fetch these many records
    public static final int MAX_RESULTS = 50;

    // Number of top k matching elements you wish to retrieve
    public static final int TOPK = 10;

    // Sample query to test the indexed DBPedia data
    public static final String SAMPLE_QUERY = "jaguar ";

    // *****************IE Engines output locations ***************************************************

    // location of the output file generated the IE Engine ReVerb
    public static final String IE_TUPLES_PATH = "/home/arnab/Work/data/NELL/b.csv";

    // Delimiter used to parse the ReVerb extracted tuples
    public static final String REVERB_IE_DELIMIT = "\t";

    // Delimiter used to parse the ReVerb extracted tuples
    public static final String NELL_IE_DELIMIT = ",";

    // output location of the predicate list after calculating jaccard score for each
    public static final String PREDICATE_FREQ_FILEPATH = "/home/arnab/Work/data/NELL/predFreq.txt";

    // *****************WEB INTERFACE PARAMETES***************************************************

    // If this is turned on the then the system performs a predictive search else just a simple search based on the
    // input terms
    public static final boolean PREDICTIVE_SEARCH_MODE = true;

    // only those entities with a match of value higher than this will be taken into consideration for further
    // processing
    public static final double THRESHOLD_SCORE = 80;

}

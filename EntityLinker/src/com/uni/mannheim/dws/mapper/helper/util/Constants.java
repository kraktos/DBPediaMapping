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

    /**
     * delimiter for the the CSV file coming as input from extraction engines' output
     */
    public static final String DELIMIT_IE_FILE = ",";

    /**
     * DBPedia End point URL
     */
    public static final String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql"; // "http://live.dbpedia.org/sparql";

    // *****************DIRECTORY LOCATIONS ************************************************

    /**
     * location for storing the predicate distribution patterns based on some integer values of the subjects and objects
     * of the instances
     */
    public static final String DBPEDIA_PREDICATE_DISTRIBUTION = "/home/arnab/Work/data/DBPedia/property";

    /**
     * location of all the raw CSV files dumped from DBPedia SPARQL endpoint
     */
    public static final String DBPEDIA_DATA_DIR = "/home/arnab/Work/data/DBPedia/data";

    /**
     * location of the directory where the indices for entities are stored
     */
    public static final String DBPEDIA_ENT_INDEX_DIR = "/home/arnab/Work/data/DBPedia/indexFiles";

    /**
     * location of the directory where the indices for predicates are stored
     */
    public static final String DBPEDIA_PROP_INDEX_DIR = "/home/arnab/Work/data/DBPedia/propIndexFiles";

    /**
     * Delimiter to separate the URI and the lable of DBPedia entries
     */
    public static final String DBPEDIA_DATA_DELIMIT = "~!~";

    /**
     * only the URIs with the following header will be used for indexing
     */
    public static final String DBPEDIA_HEADER = "http://dbpedia.org/";

    /**
     * Filter out the YAGO links
     */
    public static final String YAGO_HEADER = "http://dbpedia.org/class/yago";

    // *****************INDEXING STRATEGIES ************************************************

    /**
     * allowable text for indexing, do not index Chinese, Japanese, Korean, Russian etc labels
     */
    public static final String ALLOWED_ENGLISH_TEXT = "[^\\w_\\s()'.:,]";

    /**
     * Filter to remove certain punctuations from the uri
     */
    public static final String URI_FILTER = "[():,.\\s']";

    /**
     * Filter to remove certain punctuations from the labels
     */
    public static final String LABEL_FILTER = "[():,']";

    /**
     * change here to use different analyzers
     */
    public static final Analyzer LUCENE_ANALYZER = new StandardAnalyzer(Version.LUCENE_40);

    /**
     * flag to determine whether to concat to old indices or recreate all from scratch
     */
    public static final boolean EMPTY_INDICES = true;

    /**
     * Flag to denote if indexing is to be done or query on old indices
     */
    public static final boolean INDEX_AGAIN = false;

    // *****************FETCH STRATEGIES ***************************************************
    // tweaking these can dramatically effect the query response time

    /**
     * percentage length of common (non-fuzzy) prefix in the user query you want to match, higher value makes it to
     * search over smaller data matches not on all of them. Here it means 80% of the query term should contain in the
     * result sets
     */
    public static final float PREFIX_LENGTH_PERCENT = 0.1F;

    /**
     * default 50% similarity and above, lower this value to fetch even lesser similar items
     */
    public static final double SIMILARITY = 100.00;

    /**
     * change the value to fetch these many records, Lucene uses this to fetch maximum these many matching documents
     */
    public static final int MAX_RESULTS = 50;

    /**
     * Number of top k matching elements you wish to retrieve
     */
    public static final int TOPK = 10;

    /**
     * Sample query to test the indexed DBPedia data
     */
    public static final String SAMPLE_QUERY = "director";

    // *****************IE Engines output locations ***************************************************

    /**
     * location of the output file generated the IE Engine ReVerb
     */
    public static final String NELL_DATA_PATH = "/home/arnab/Work/data/NELL/Nell.csv";

    /**
     * Delimiter used to parse the ReVerb extracted tuples
     */
    public static final String REVERB_IE_DELIMIT = "\t";

    /**
     * Delimiter used to parse the ReVerb extracted tuples
     */
    public static final String NELL_IE_DELIMIT = ",";

    /**
     * output location of the predicate list after calculating jaccard score for each
     */
    public static final String PREDICATE_FREQ_FILEPATH = "/home/arnab/Work/data/NELL/predFreq_1.txt";

    // *****************WEB INTERFACE PARAMETES***************************************************

    /**
     * If this is turned on the then the system performs a predictive search else just a simple search based on the
     * input terms
     */
    public static final boolean PREDICTIVE_SEARCH_MODE = true;

    /**
     * only those entities with a match of value higher than this will be taken into consideration for further
     * processing
     */
    public static final double THRESHOLD_SCORE = 80;

    // *****************Database Parameters PARAMETES***************************************************
    public static final String INSERT_FACT_SQL =
        "INSERT INTO \"UNCERTAIN_KB\"(\"SUB\", \"PRED\", \"OBJ\", \"CONFIDENCE\") VALUES (?, ?, ?, ?)";

    public static final String INSERT_PROPERTY_DOMAIN_RANGE_SQL =
        "INSERT INTO \"PREDICATE_DOMAIN_RANGE\"(\"PREDICATE\", \"DOMAIN\", \"RANGE\") VALUES (?, ?, ?)";

}

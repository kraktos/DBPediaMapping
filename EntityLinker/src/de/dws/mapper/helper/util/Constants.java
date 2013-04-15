/**
 * 
 */

package de.dws.mapper.helper.util;

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
     * delimiter for the the CSV file coming as input from extraction engines'
     * output
     */
    public static final String DELIMIT_IE_FILE = ",";

    /**
     * DBPedia End point URL
     */
    public static final String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql";
    // "http://live.dbpedia.org/sparql";

    // *****************DIRECTORY LOCATIONS
    // ************************************************

    /**
     * location for storing the predicate distribution patterns based on some
     * integer values of the subjects and objects of the instances
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

    // *****************INDEXING STRATEGIES
    // ************************************************

    /**
     * allowable text for indexing, do not index Chinese, Japanese, Korean,
     * Russian etc labels
     */
    public static final String ALLOWED_ENGLISH_TEXT = "[^\\w_\\s()'.:,]";

    /**
     * Filter to remove certain punctuations from the uri
     */
    public static final String URI_FILTER = "[():,.\\s'-]";

    /**
     * Filter to remove certain punctuations from the labels
     */
    public static final String LABEL_FILTER = "[():,']";

    /**
     * change here to use different analyzers
     */
    public static final Analyzer LUCENE_ANALYZER = new StandardAnalyzer(Version.LUCENE_40);

    /**
     * flag to determine whether to concat to old indices or recreate all from
     * scratch
     */
    public static final boolean EMPTY_INDICES = true;

    /**
     * Flag to denote if indexing is to be done or query on old indices
     */
    public static final boolean INDEX_AGAIN = false;

    // *****************FETCH STRATEGIES
    // ***************************************************
    // tweaking these can dramatically effect the query response time

    /**
     * percentage length of common (non-fuzzy) prefix in the user query you want
     * to match, higher value makes it to search over smaller data matches not
     * on all of them. Here it means 80% of the query term should contain in the
     * result sets
     */
    public static final float PREFIX_LENGTH_PERCENT = 0.1F;

    /**
     * default 50% similarity and above, lower this value to fetch even lesser
     * similar items
     */
    public static final double SIMILARITY = 100.00;

    /**
     * change the value to fetch these many records, Lucene uses this to fetch
     * maximum these many matching documents
     */
    public static final int MAX_RESULTS = 50;

    /**
     * Number of top k matching elements you wish to retrieve
     */
    public static final int TOPK = 5;

    /**
     * Sample query to test the indexed DBPedia data
     */
    public static final String SAMPLE_QUERY = "shaw";

    // *****************IE Engines output locations
    // ***************************************************

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
     * output location of the predicate list after calculating jaccard score for
     * each
     */
    public static final String PREDICATE_FREQ_FILEPATH = "/home/arnab/Work/data/NELL/predFreq_2.txt";

    // *****************WEB INTERFACE
    // PARAMETES***************************************************

    /**
     * If this is turned on the then the system performs a predictive search
     * else just a simple search based on the input terms
     */
    public static final boolean PREDICTIVE_SEARCH_MODE = true;

    /**
     * only those entities with a match of value higher than this will be taken
     * into consideration for further processing
     */
    public static final double THRESHOLD_SCORE = 80;

    // *****************Database Parameters
    // PARAMETES***************************************************
    public static final String INSERT_FACT_SQL =
            "INSERT INTO \"UNCERTAIN_KB\"(\"SUB\", \"PRED\", \"OBJ\", \"CONFIDENCE\") VALUES (?, ?, ?, ?)";

    public static final String INSERT_PROPERTY_DOMAIN_RANGE_SQL =
            "INSERT INTO \"PREDICATE_DOMAIN_RANGE\"(\"PREDICATE\", \"DOMAIN\", \"RANGE\") VALUES (?, ?, ?)";

    public static final String GET_WIKI_STAT = "select distinct entity from stats where anchor=?";

    public static final String INSERT_GOLD_STANDARD =
            "INSERT INTO goldStandard (E_SUB, E_PRED, E_OBJ, E_CONF, D_SUB, D_PRED, D_OBJ, HOST) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * defines the batch size for the Data base operations
     */
    public static final int BATCH_SIZE = 100;

    // *****************OWL
    // PARAMETES***************************************************

    /**
     * namespace of the ontology to be used for creation of the axiom files
     */
    public static String ONTOLOGY_NAMESPACE = "http://dbpedia.org/ontology/";

    /**
     * DBPedia namespace
     */
    public static String ONTOLOGY_DBP_NS = ONTOLOGY_NAMESPACE; // + "Dbp#";

    /**
     * extraction engine namespace
     */
    public static String ONTOLOGY_EXTRACTION_NS = ONTOLOGY_NAMESPACE + "Extract#";

    /**
     * DBPedia TBOX info file
     */
    public static final String OWL_INPUT_FILE_PATH = "/home/arnab/Workspaces/SchemaMapping/EntityLinker/data/ontology/input/dbpediaGold.owl";

    /**
     * defines the confidence value namespace for the owl files
     */
    public static final String CONFIDENCE_VALUE_DEFINITION = "http://reasoner#confidence";

    /**
     * place where generated owl files are dumped. This file contains all the
     * axioms on which reasoner runs
     */
    public static final String OWLFILE_CREATED_FROM_FACTS_OUTPUT_PATH =
            "/home/arnab/Workspaces/SchemaMapping/EntityLinker/data/ontology/output/assertions.owl";

    /**
     * place where generated owl files are dumped. This file contains all the
     * axioms on which reasoner runs
     */
    public static final String OWLFILE_CREATED_FROM_ELOG_REASONER_OUTPUT_PATH =
            "/home/arnab/Workspaces/SchemaMapping/EntityLinker/data/ontology/output/aposteriori.owl";

    /**
     * Max weight an Axiom can have, basically recomputing the weights, w from
     * probability, p using the formula [p = (exp(w))/1+(exp(w))]. Assuming
     * maximum probability an axiom to be 0.999999999
     */
    public static final double AXIOM_MAX_WEIGHT = 20.7232;

    /**
     * Min weight an Axiom can have, basically recomputing the weights, w from
     * probability, p using the formula [p = (exp(w))/1+(exp(w))]. Assuming
     * minimum probability an axiom to be 0.000000001
     */
    public static final double AXIOM_MIN_WEIGHT = -20.7232;

    // from the UI you can run to create gold standard, as well as perform
    // inference. Setting it false will make it run as gold standard creation
    // mode
    public static final boolean INFERENCE_MODE = false;

    // ********** Experiments
    // *************************************************************
    /**
     * input set of data from NELL, with no intersection across triples
     */
    // public static final String NELL_DOMAIN_INPUT_FILE_PATH =
    // "/home/arnab/Work/data/NELL/all.csv";

    /**
     * input set of data from NELL, with some intersection across triples
     */
    public static final String NELL_DOMAIN_INPUT_FILE_PATH = "/home/arnab/Work/data/NELL/all.csv";

    /**
     * read the above file randomly or make it false to do sequential read
     */
    public static final boolean RANDOM_READ = false;
    /**
     * input set of random triples from NELL
     */
    public static final String NELL_RANDOM_TRIPLE_DATA_SET = "/home/arnab/Work/data/NELL/randomTriples.csv";

    /**
     * number of nell triples to be considered.
     */
    public static final int RANDOM_TRIPLES_LIMIT = 8;

}

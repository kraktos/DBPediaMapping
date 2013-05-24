/**
 * 
 */

package de.dws.helper.util;

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
    // "http://dbpedia.org/sparql";
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
     * location of the directory where the DBPEdia indices for entities are
     * stored
     */
    public static final String DBPEDIA_ENT_INDEX_DIR = "/home/arnab/Work/data/DBPedia/indexFiles";

    /**
     * location of the directory where the NELL indices for entities are stored.
     * Gold Standard creation
     */
    public static final String NELL_ENT_INDEX_DIR = "/home/arnab/Work/data/NELL/indexFiles";

    /**
     * location of the directory where the DBPedia SO indices for entities are
     * stored. Baseline creation
     */
    public static final String DBPEDIA_INFO_INDEX_DIR = "/home/arnab/Work/data/DBPedia/infoIndex";

    /**
     * location of the directory where the ReVerb indices for entities are
     * stored. Gold Standard creation
     */
    public static final String REVERB_ENT_INDEX_DIR = "/home/arnab/Work/data/ReVerb/index";

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
    public static final String REVERB_IE_DELIMIT = ";";

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
     * into consideration for further processing.
     */
    public static final double THRESHOLD_SCORE = 80;

    // *****************Database Parameters
    // PARAMETES***************************************************
    public static final String INSERT_FACT_SQL =
            "INSERT INTO \"UNCERTAIN_KB\"(\"SUB\", \"PRED\", \"OBJ\", \"CONFIDENCE\") VALUES (?, ?, ?, ?)";

    public static final String INSERT_PROPERTY_DOMAIN_RANGE_SQL =
            "INSERT INTO \"PREDICATE_DOMAIN_RANGE\"(\"PREDICATE\", \"DOMAIN\", \"RANGE\") VALUES (?, ?, ?)";

    public static final String GET_WIKI_STAT = "select distinct entity from stats where anchor=?";

    public static final String GET_LINK_COUNT = "select count(*) as cnt  from link_anchors l, title_2_id t where t.title = ? and l.anchor=? and l.target = t.id";

    /**
     * SQL to insert a gold standard instance for NELL
     */
    public static final String INSERT_GOLD_STANDARD =
            "INSERT INTO goldStandard (E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ, SUB_LINK_CNT, OBJ_LINK_CNT ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * SQL to insert a gold standard instance for ReVerb
     */
    public static final String INSERT_GOLD_STANDARD_REVERB =
            "INSERT INTO goldStandard_2 (E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ, SUB_LINK_CNT, OBJ_LINK_CNT ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    
    
    /**
     * SQL to insert a baseline instance for NELL
     */
    public static final String INSERT_BASE_LINE =
            "INSERT INTO baseLine (E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ ) VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * SQL to insert a baseline instance for REVERB
     */
    public static final String INSERT_BASE_LINE_REVERB =
            "INSERT INTO baseLine_2 (E_SUB, E_PRED, E_OBJ, D_SUB, D_PRED, D_OBJ ) VALUES (?, ?, ?, ?, ?, ?)";

    
    /**
     * SQL to insert an axiom before running inference
     */
    public static final String INSERT_AXIOM_SQL = "INSERT INTO axioms (E_ENTITY, CANDIDATE, APRIORI, APOSTERIORI) VALUES (?, ?, ?, ?)";

    /**
     * SQL to update an axiom after running inference
     */
    public static final String UPDATE_AXIOM_SQL = "UPDATE axioms SET APOSTERIORI=? WHERE  E_ENTITY=? AND CANDIDATE=?";

    /**
     * given a title, fetch top surface forms
     */
    // public static final String GET_WIKI_SURFACE_FORMS_SQL =
    // "select l.anchor as anchor, count(*) as cnt from link_anchors l, title_2_id t where t.title=? and t.id=l.target group by l.anchor having cnt > ? order by cnt desc limit ?";

    public static final String GET_WIKI_SURFACE_FORMS_SQL = "select SF, PROB from surfaceForms where URI = ? ";
    /**
     * given a surface form, fetch top titles it refers to
     */
    public static final String GET_WIKI_TITLES_SQL = "select  t.title, count(*) as cnt from link_anchors l, title_2_id t where l.anchor=? and l.target=t.id group by t.title order by cnt desc limit 2";
    		//"select URI from surfaceForms where SF =? order by PROB desc" ;
    		//"select  t.title, count(*) as cnt from link_anchors l, title_2_id t where l.anchor=? and l.target=t.id group by t.title order by cnt desc limit ?";
    

    public static final String INSERT_SURFACE_FORMS_SQL =
            "INSERT INTO surfaceForms_2_uri (uri, surface, count) VALUES (?, ?, ?)";

    public static final String INSERT_DB_SURFACE_FORMS_SQL =
            "INSERT INTO surfaceForms (URI, SF, PROB) VALUES (?, ?, ?)";

    /**
     * fetch the top matching DBPedia predicates co-occurring with a given NELL
     * predicate
     */
    public static final String GET_COOCC_PREDICATES_SQL = "select count(*) as cnt, D_PRED from goldStandardClean where E_PRED =? group by D_PRED order by cnt desc" ;
    		//"select *, count(*) as cnt from goldStandardClean where E_PRED =? group by D_PRED order by cnt desc" ;
    		//"select count(*) as cnt, D_PRED from goldStandardClean where E_PRED =? group by D_PRED order by cnt desc";

    /**
     * fetch all the NELL predicates matched with the DBPedia dataset
     */
    public static final String GET_NELL_PREDICATES = "select E_PRED, count(*) as cnt from goldStandardClean group by E_PRED order by cnt desc" ;
    		//"select predicate, count(*) as cnt from nell group by predicate order by cnt desc";

    /**
     * defines the batch size for the Data base operations
     */
    public static final int BATCH_SIZE = 10000;

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
    public static final String NELL_DOMAIN_INPUT_FILE_PATH = "/home/arnab/Work/data/NELL/portion.csv";

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

    // **************** WIKIPEDIA PARAMS ******************************

    public static final String WIKI_PAGE_HEADER = "http://en.wikipedia.org/wiki/";

    // Top surface forms for a given wikipedia page
    public static final int TOP_ANCHORS = 2;

    // anchors with atleast these many occurrence in wikipedia pointing to the
    // page
    public static final int ATLEAST_LINKS = 5;

    // take sentences with atmost these many words between them
    public static final int WORD_GAP = 5;
    
    public static final boolean IS_NELL = false;

}

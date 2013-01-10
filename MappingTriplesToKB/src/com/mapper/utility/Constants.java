/**
 * 
 */
package com.mapper.utility;

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
    // flag to determine whether to concat to old indices or recreate all from scratch
    public static final boolean EMPTY_INDICES = false;

    // Flag to denote if indexing is to be done or query on old indices
    public static final boolean INDEX_AGAIN = false;

    // *****************FETCH STRATEGIES ***************************************************
    // tweaking these can dramatically effect the query response time

    // default 50% similarity and above, lower this value to fetch even lesser similar items
    public static final float SIMILARITY = 0.6F;

    // change the value to fetch these many records
    public static final int MAX_RESULTS = 5;

    // Number of top k matching elements you wish to retrieve
    public static final int TOPK = 5;

    // Sample query to test the indexed DBPedia data
    public static final String SAMPLE_QUERY = "prison break";

}

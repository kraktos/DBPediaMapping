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

    // location of all the raw CSV files dumped from DBPedia SPARQL endpoint
    public static final String DBPEDIA_DATA_DIR = "/home/arnab/Work/data/DBPedia/dataFiles/";

    // location of the directory where the indices are stored
    public static final String DBPEDIA_INDEX_DIR = "/home/arnab/Work/data/DBPedia/indexFiles";

    // Number of top k matching elements you wish to retrieve
    public static final int TOPK = 10;

    // Sample query to test the indexed DBPedia data
    public static final String SAMPLE_QUERY = "person";

    // flag to determine whether to concat to old indices or recreate all from scratch
    public static final boolean EMPTY_INDICES = false;

    // Flag to denote if indexing is to be done or query on old indices
    public static final boolean INDEX = false;

}

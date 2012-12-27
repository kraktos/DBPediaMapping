package com.mapper.client;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.mapper.indexer.DataIndexerImpl;
import com.mapper.message.Messages;
import com.mapper.query.QueryApi;
import com.mapper.query.SPARQLEndPointQueryAPI;
import com.mapper.score.FastJoinWrapper;
import com.mapper.score.Similarity;
import com.mapper.utility.FileUtil;
import com.mapper.utility.Utilities;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class is the client for generating mappings from a given set of RDF triples to an existing Knowledge Base.
 * 
 * @author Arnab Dutta
 */

public class Main
{

    // logger
    static Logger logger = Logger.getLogger(Main.class.getName());

    private static final String QUERY = "select distinct ?b ?label where {"
        + " { <http://dbpedia.org/resource/Mel_Gibson> ?b ?c. "
        + "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}" + "UNION{"
        + "?d ?b <http://dbpedia.org/resource/Mel_Gibson>. "
        + "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}}";

    // raw IE output file
    public static String extractedFactDataSet = Messages.getString("SOURCE_FACTS_FILE_PATH");

    // output from IE engine in CSV format
    public static String ieOutputCsvFilePath = Messages.getString("IE_OUTPUT_CSV_FILE_PATH");

    private static String propTargetFilePath = Messages.getString("UNIQUE_PROP_DATA_FILE_PATH");

    public static String greppedIEOutputCsvFilePath = Messages.getString("IE_OUTPUT_PROP_FILE_PATH");

    // output location of the subjects and objects in DBPedia
    public static String dbPediaSubjAndObjFilePath = Messages.getString("DBPEDIA_SUBJECTS_FILE_PATH");

    // output location of predicates in DBPEdia
    public static String dbPediaPredicatesFilePath = Messages.getString("DBPEDIA_PREDICATES_FILE_PATH");

    // test query to prun the IE engine csv file
    public static String searchQuery = Messages.getString("SEARCH_ITEM");

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException
    {

        final String owlPath = (args.length > 0) ? args[0] : Messages.getString("OWL_FILE_PATH");
        final String dataPath = (args.length > 0) ? args[1] : Messages.getString("DATA_FILE_PATH");

        /*
         * OntologyImpl ontology = new OntologyImpl(); ontology.loadOntology(owlPath);
         */
        // ontology.readOntology();

        final long start = System.currentTimeMillis();

        //DataIndexerImpl dataIndexer = new DataIndexerImpl(dataPath);
        //dataIndexer.readData();

        logger.info("END of INDEXING DBPEDIA DATA .......");

        logger.info("STARTING PROCESSING OF RAW IE OUTPUT FILE .......");

        // SPARQLEndPointQueryAPI.queryDBPedia(QUERY);
        // findUniqueProperties();

        // Take the IE output and convert it to CSV file
       // Utilities.createCSVFilefromIEDataSet();

        logger.info("END OF CSV FILE CREATION FOR THE RAW IE OUTPUT FILE  => " + ieOutputCsvFilePath);

        // At this point, we take a tuple from the IE and start processing it by
        // matching each with the DBPedia entries

        logger.info("STARTING THE MAPPING PROCEDURE ");
        FileUtil.readIEFile(ieOutputCsvFilePath);

        // once we have both the DB Pedia data and CSV form of IE extracted
        // data, we can figure out which properties from the IE output actually
        // matches into the properties of DBPedia data
        //Utilities.createSubSetOfIEOuputTuples();

        // Task is now to take each tuples from the subset file and match with
        // the DB Pedia files
        //logger.info("STARTING TO MAP A TUPLE FROM THE SAMPLE FILE " + greppedIEOutputCsvFilePath + " BEGINS\n");
        //Utilities.mapTuple();

        //final long end = System.currentTimeMillis();

        //logger.info("PROCESS COMPLETED..EXECUTION TIME => " + (end - start) + " ms.");

    }

    /**
     * This finds the set of unique properties for a given literal. The literal may be occurring as a subject or object.
     */
    private static void findUniqueProperties()
    {
        try {
            FileWriter fstream = new FileWriter(propTargetFilePath);
            BufferedWriter out = new BufferedWriter(fstream);

            QueryApi.fetchAnswers("http://dbpedia.org/resource/Mel_Gibson", out);

            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

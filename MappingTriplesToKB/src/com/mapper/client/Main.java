package com.mapper.client;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.mapper.indexer.DataIndexerImpl;
import com.mapper.message.Messages;
import com.mapper.query.QueryApi;
import com.mapper.query.SPARQLEndPointQueryAPI;
import com.mapper.score.FastJoinWrapper;
import com.mapper.score.ScoreEngineImpl;
import com.mapper.score.Similarity;
import com.mapper.utility.FileUtil;
import com.mapper.utility.Utilities;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class is the client for generating mappings from a given set of RDF
 * triples to an existing Knowledge Base.
 * 
 * @author Arnab Dutta
 */

public class Main {

	// logger
	static Logger logger = Logger.getLogger(Main.class.getName());

	private static final String QUERY = "select distinct ?b ?label where {"
			+ " { <http://dbpedia.org/resource/Mel_Gibson> ?b ?c. "
			+ "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}"
			+ "UNION{" + "?d ?b <http://dbpedia.org/resource/Mel_Gibson>. "
			+ "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}}";

	// raw IE output file
	private static String extractedFactDataSet = Messages
			.getString("SOURCE_FACTS_FILE_PATH");

	// output from IE engine in CSV format
	public static String ieOutputCsvFilePath = Messages
			.getString("IE_OUTPUT_CSV_FILE_PATH");

	private static String propTargetFilePath = Messages
			.getString("UNIQUE_PROP_DATA_FILE_PATH");

	public static String greppedIEOutputCsvFilePath = Messages
			.getString("IE_OUTPUT_PROP_FILE_PATH");

	// output location of the subjects and objects in DBPedia
	public static String dbPediaSubjAndObjFilePath = Messages
			.getString("DBPEDIA_SUBJECTS_FILE_PATH");

	// output location of predicates in DBPEdia
	public static String dbPediaPredicatesFilePath = Messages
			.getString("DBPEDIA_PREDICATES_FILE_PATH");

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException,
			IOException, InterruptedException {

		final String owlPath = (args.length > 0) ? args[0] : Messages
				.getString("OWL_FILE_PATH");
		final String dataPath = (args.length > 0) ? args[1] : Messages
				.getString("DATA_FILE_PATH");

		/*
		 * OntologyImpl ontology = new OntologyImpl();
		 * 
		 * ontology.loadOntology(owlPath);
		 */
		// ontology.readOntology();

		final long start = System.currentTimeMillis();

		DataIndexerImpl dataIndexer = new DataIndexerImpl(dataPath);
		dataIndexer.readData();

		logger.info("END of INDEXING DBPEDIA DATA .......");

		logger.info("STARTING PROCESSING OF IE OUTPUT TUPLES .......");

		// SPARQLEndPointQueryAPI.queryDBPedia(QUERY);

		// findUniqueProperties();

		// Take the IE output and convert it to CSV file
		createCSVFilefromIEDataSet();

		logger.info("END OF CSV FILE CREATION FOR THE INPUT IE DATA  => "
				+ ieOutputCsvFilePath);

		// once we have both the DB Pedia data and CSV form of IE extracted
		// data, we can figure out which properties from the IE output actually
		// matches into the properties of DBPedia data
		createSubSetOfIEOuputTuples();

		// Task is now to take each tuples from the subset file and match with
		// the DB Pedia files
		logger.info("MAPPING OF A TUPLE FROM THE FILE "
				+ greppedIEOutputCsvFilePath + " BEGINS\n");
		Utilities.mapTuple();

		final long end = System.currentTimeMillis();

		logger.info("PROCESS COMPLETED..EXECUTION TIME => " + (end - start)
				+ " ms.");

	}

	/**
	 * Method to create a sub set of data from the data set provided by the IE
	 * engine. This is purely for test purpose. Can be removed later on.
	 * 
	 * @throws IOException
	 */
	private static void createSubSetOfIEOuputTuples() throws IOException {

		// Take the user query and extract those tuples from the CSV file
		// TODO: think of doing it in without query api.
		// At some point there would be no grepped files..we have to match
		// the entire IE output tuples
		String userQuery = "mel_gibson";
		final File file = new File(ieOutputCsvFilePath);

		FileWriter fstream = new FileWriter(greppedIEOutputCsvFilePath);
		BufferedWriter greppedIEOutput = new BufferedWriter(fstream);

		FileUtil.extractMatchingTuples(userQuery, file, greppedIEOutput);

		greppedIEOutput.close();

	}

	/**
	 * Transform a set of extracted facts output from any IE engine like NELL,
	 * and convert it to a CSV file with associated truth values of each such
	 * fact.
	 */
	private static void createCSVFilefromIEDataSet() {

		ScoreEngineImpl scoreEngine = new ScoreEngineImpl();
		scoreEngine.readExtractedFacts(extractedFactDataSet,
				ieOutputCsvFilePath);
	}

	/**
	 * This finds the set of unique properties for a given literal. The literal
	 * may be occurring as a subject or object.
	 */
	private static void findUniqueProperties() {
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

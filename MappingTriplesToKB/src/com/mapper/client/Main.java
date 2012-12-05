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

	static Logger logger = Logger.getLogger(Main.class.getName());

	private static final String QUERY = "select distinct ?b ?label where {"
			+ " { <http://dbpedia.org/resource/Mel_Gibson> ?b ?c. "
			+ "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}"
			+ "UNION{" + "?d ?b <http://dbpedia.org/resource/Mel_Gibson>. "
			+ "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}}";

	private static String extractedFactDataSet = Messages
			.getString("SOURCE_FACTS_FILE_PATH");

	private static String outputFilePath = Messages
			.getString("IE_OUTPUT_CSV_FILE_PATH");

	private static String propTargetFilePath = Messages
			.getString("UNIQUE_PROP_DATA_FILE_PATH");

	private static String propSourceFilePath = Messages
			.getString("IE_OUTPUT_PROP_FILE_PATH");

	// The top k matches of similarity
	private static int TOP_K = 5;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException,
			IOException {

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
		// dataIndexer.readData();

		// SPARQLEndPointQueryAPI.queryDBPedia(QUERY);

		// findUniqueProperties();

		// createCSVFilefromIEDataSet();

		// once we have both the DB Pedia data and CSV form of IE extracted
		// data,
		// we can figure out which properties from the IE output actually
		// matches into the properties of DBPedia data
		// createPropertySetFile();

		final long end = System.currentTimeMillis();

		// calculate scores
		computeMatch(propSourceFilePath, propTargetFilePath);

		logger.info("Execution time was " + (end - start) + " ms.");

	}

	/**
	 * 
	 * @param propSourceFilePath
	 *            The IE output properties list
	 * @param propTargetFilePath
	 *            The DBPedia properties
	 * @param tOP_K2
	 * @throws IOException
	 */
	private static void computeMatch(final String propSourceFilePath,
			String propTargetFilePath) throws IOException {

		// TODO: different similarity matches goes here

		// Measure Type I : Fast Join
		// FastJoinWrapper.join(propSourceFilePath, propTargetFilePath);

		// Measure Type II : Levenstein Edit Distance
		Similarity.extractLinesToCompare(propSourceFilePath,
				propTargetFilePath, TOP_K);
	}

	private static void createPropertySetFile() throws IOException {

		// Take the user query and extract those tuples from the CSV file
		// TODO: think of doing it in without query api
		String userQuery = "mel_gibson";
		final File file = new File(outputFilePath);

		FileWriter fstream = new FileWriter(propSourceFilePath);
		BufferedWriter outProperty = new BufferedWriter(fstream);

		FileUtil.extractMatchingTuples(userQuery, file, outProperty);

		outProperty.close();

	}

	/**
	 * Transform a set of extracted facts output from any IE engine like NELL,
	 * and convert it to a CSV file with associated truth values of each such
	 * fact.
	 */
	private static void createCSVFilefromIEDataSet() {

		ScoreEngineImpl scoreEngine = new ScoreEngineImpl();
		scoreEngine.readExtractedFacts(extractedFactDataSet, outputFilePath);
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

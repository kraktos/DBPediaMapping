package com.mapper.client;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.mapper.indexer.DataIndexerImpl;
import com.mapper.message.Messages;
import com.mapper.query.QueryApi;
import com.mapper.query.SPARQLEndPointQueryAPI;
import com.mapper.score.ScoreEngineImpl;
import java.io.*;

/**
 * This class is the client for generating mappings from a given set of RDF
 * triples to an existing Knowledge Base.
 * 
 * @author Arnab Dutta
 */

public class Main {

	private static final String QUERY = "select distinct ?b ?label where {"
			+ " { <http://dbpedia.org/resource/Mel_Gibson> ?b ?c. "
			+ "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}"
			+ "UNION{" + "?d ?b <http://dbpedia.org/resource/Mel_Gibson>. "
			+ "?b <http://www.w3.org/2000/01/rdf-schema#label> ?label}}";

	private static String extractedFactDataSet = Messages
			.getString("SOURCE_FACTS_FILE_PATH");

	private static String outputFilePath = Messages
			.getString("IE_OUTPUT_CSV_FILE_PATH");

	private static String uniquePropFilePath = Messages
			.getString("UNIQUE_PROP_DATA_FILE_PATH");

	/**
	 * @param args
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {

		Logger logger = Logger.getLogger(Main.class.getName());

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

		SPARQLEndPointQueryAPI.queryDBPedia(QUERY);

		findUniqueProperties();

		// feedTuplesTofindMatches();

		final long end = System.currentTimeMillis();

		logger.info("Execution time was " + (end - start) + " ms.");

	}

	private static void feedTuplesTofindMatches() {

		ScoreEngineImpl scoreEngine = new ScoreEngineImpl();
		scoreEngine.readExtractedFacts(extractedFactDataSet, outputFilePath);
	}

	private static void findUniqueProperties() {
		try {
			FileWriter fstream = new FileWriter(uniquePropFilePath);
			BufferedWriter out = new BufferedWriter(fstream);

			QueryApi.fetchAnswers("http://dbpedia.org/resource/Mel_Gibson", out);

			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

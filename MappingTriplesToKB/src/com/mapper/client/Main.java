package com.mapper.client;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.mapper.indexer.DataIndexerImpl;
import com.mapper.message.Messages;
import com.mapper.query.QueryApi;

/**
 * This class is the client for generating mappings from a given set of RDF
 * triples to an existing Knowledge Base.
 * 
 * @author Arnab Dutta
 */

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {

		Logger logger = Logger.getLogger(Main.class.getName());

		final String owlPath = (args.length > 0) ? args[0] : Messages
				.getString("OWL_FILE_PATH");
		final String dataPath = (args.length > 0) ? args[1] : Messages
				.getString("DATA_FILE_PATH");

		/*OntologyImpl ontology = new OntologyImpl();

		ontology.loadOntology(owlPath);*/
		// ontology.readOntology();

		final long start = System.currentTimeMillis();

		DataIndexerImpl dataIndexer = new DataIndexerImpl(dataPath);
		dataIndexer.readData();
		
		String test = "\"this is what\",\"I wanna test\",\"but, not these\",122";
		String[] tokens = test.split("\",");
		/*for(int i = 0; i < tokens.length; i++){
			//System.out.println(tokens[i].replaceAll("\"", ""));
		}*/
		

		QueryApi.fetchAnswers("http://dbpedia.org/resource/Albert_Einstein");
		
		final long end = System.currentTimeMillis();

		logger.info("Execution time was " + (end - start) + " ms.");

	}

}

/**
 * 
 */
package com.mapper.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mapper.client.Main;
import com.mapper.indexer.DataIndexerImpl;
import com.mapper.score.Similarity;

/**
 * @author Arnab Dutta
 * 
 */
public class Utilities {

	// define Logger
	static Logger logger = Logger.getLogger(Utilities.class.getName());

	static Set<Long> UNIQUE_PROPERTIES = new HashSet<Long>();

	/**
	 * Prints a map
	 * 
	 * @param map
	 * 
	 */
	public static void printMap(Map<?, ?> map) {
		for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Long, String> entry = (Entry<Long, String>) it.next();
			Long key = entry.getKey();
			String value = entry.getValue();
			logger.info(key + "  " + value);
		}
	}

	/**
	 * Iterate the list and print out the string literals for the query
	 * 
	 * @param resultList
	 * @param out
	 */
	public static void printList(List<Long> resultList, BufferedWriter out) {
		try {

			String propertyLabel = null;

			for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
				// print only the odd values
				if (listCounter % 2 != 0) {
					// this gives a set of properties for the given query
					UNIQUE_PROPERTIES.add(resultList.get(listCounter));
				}
			}
			for (Long key : UNIQUE_PROPERTIES) {
				extractPropertyFromURI(key, out);
			}
			logger.info("Unique properties  = " + UNIQUE_PROPERTIES.size()
					+ "\n");
		} finally {
			UNIQUE_PROPERTIES.clear();
		}

	}

	/**
	 * Prints a set
	 * 
	 * @param set
	 */
	public static void printSet(final Set<?> set) {
		Iterator<?> it = set.iterator();
		while (it.hasNext()) {
			logger.info(it.next());
		}
	}

	/**
	 * 
	 * @param propertyURI
	 * @param key
	 * @param out
	 * @return
	 */
	private static void extractPropertyFromURI(final Long key,
			BufferedWriter out) {

		String propertyURI = DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(key);

		FileUtil.writeToFlatFile(out, propertyURI + "\n");

	}

	/**
	 * 
	 * @param word
	 * @return
	 */
	public static String extractPredicatesFromTuples(final String word) {
		String[] entities = word.split(",");
		return entities[1];
	}

	/**
	 * Takes a set of Strings and writes to the output file
	 * 
	 * @param SET_DBPEDIA_TERMS
	 *            set of string values
	 * @param targetFilePath
	 *            putput file location
	 * @throws IOException
	 */
	public static void writeSetToFile(Set<String> SET_DBPEDIA_TERMS,
			String targetFilePath) throws IOException {

		FileWriter fstream = new FileWriter(targetFilePath);
		BufferedWriter out = new BufferedWriter(fstream);

		Iterator<?> it = SET_DBPEDIA_TERMS.iterator();
		while (it.hasNext()) {
			FileUtil.writeToFlatFile(out, it.next() + "\n");
		}

		out.close();
	}

	/**
	 * This method takes as input a tuple from the IE engine and tries to match
	 * the respective S, P, O with the ones in DBPedia
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void mapTuple() throws IOException, InterruptedException {
		// get a stream of the source tuples
		BufferedReader br = null;
		String tupleFromIE;

		// check if the pruned file is available else continue with the larger
		// CSV file from the IE engine
		try {
			br = new BufferedReader(new FileReader(
					Main.greppedIEOutputCsvFilePath));
		} catch (FileNotFoundException ex) {
			br = new BufferedReader(new FileReader(Main.ieOutputCsvFilePath));
		}

		while ((tupleFromIE = br.readLine()) != null) {
			// process with each of these tuples
			Similarity.matchTuple(tupleFromIE, Main.dbPediaSubjAndObjFilePath,
					Main.dbPediaPredicatesFilePath);

		}
	}

	/**
	 * 
	 * @param tupleFromIE
	 * @return
	 */
	public static String extractLabel(String tupleFromIE) {

		return tupleFromIE.substring(tupleFromIE.lastIndexOf(":") + 1,
				tupleFromIE.length());

	}

}

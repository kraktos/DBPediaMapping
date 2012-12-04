/**
 * 
 */
package com.mapper.utility;

import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mapper.indexer.DataIndexerImpl;

/**
 * @author Arnab Dutta
 * 
 */
public class Utilities {

	// define Logger
	static Logger logger = Logger.getLogger(Utilities.class.getName());

	static Set<Long> UNIQUE_PROPERTIES = new HashSet<Long>();

	/**
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

		// if this property exists in the map
		/*if (DataIndexerImpl.MAP_PROPERTY_LABELS.containsKey(key)) {

			// try retrieving the possible label names
			List<Long> listPropertyLabels = DataIndexerImpl.MAP_PROPERTY_LABELS
					.get(key);

			// iterate the list of labels to display
			for (Long v : listPropertyLabels) {
				String value = "";
				if (DataIndexerImpl.MAP_DBPEDIA_LITERALS.containsKey(v)) {
					value = DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(v);
				}
				FileUtil.writeToFlatFile(out, value + ", ");
			}
			FileUtil.writeToFlatFile(out, "\n");

		} else { // just output the property
			FileUtil.writeToFlatFile(out, propertyURI + "\n");
		}*/

	}

	public static String extractPredicatesFromTuples(final String word) {
		String[] entities = word.split(",");
		return entities[1];
	}

}

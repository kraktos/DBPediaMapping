/**
 * 
 */
package com.mapper.utility;

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
	 */
	public static void printList(List<Long> resultList) {
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
				extractPropertyFromURI(key);
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
	 * @return
	 */
	private static void extractPropertyFromURI(final Long key) {

		String propertyURI = DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(key);

		//logger.info(propertyURI + " => ");
		// if this property exists in the map
		if (DataIndexerImpl.MAP_PROPERTY_LABELS.containsKey(key)) {
			// try retrieving the possible label names
			List<Long> listPropertyLabels = DataIndexerImpl.MAP_PROPERTY_LABELS
					.get(key);

			// iterate the list of labels to display
			for (Long v : listPropertyLabels) {
				String value = "";
				if (DataIndexerImpl.MAP_DBPEDIA_LITERALS.containsKey(v)) {
					value = DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(v);
				}
				System.out.print(value + ",  ");
			}
			System.out.println("");
		} else { // just output the property
			System.out.println(propertyURI);
		}

	}

}

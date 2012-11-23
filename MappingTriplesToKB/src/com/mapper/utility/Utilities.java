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

			String propertyURI = null;
			String propertyLabel = null;

			for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
				// print only the odd values
				if (listCounter % 2 != 0) {
					// logger.info(resultList.get(listCounter));
					// this gives a set of properties for the given query
					// need to make it generalized
					UNIQUE_PROPERTIES.add(resultList.get(listCounter));
				}
			}
			for (Long key : UNIQUE_PROPERTIES) {
				propertyURI = DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(key);

				propertyLabel = extractPropertyFromURI(propertyURI);

				logger.info(propertyURI + " => " + propertyLabel);
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
	 * @return
	 */
	private static String extractPropertyFromURI(String propertyURI) {

		int lastIndex_slash = propertyURI.lastIndexOf("/");
		int lastIndex_hash = propertyURI.lastIndexOf("#");
		return propertyURI.substring(
				((lastIndex_hash < lastIndex_slash) ? lastIndex_slash
						: lastIndex_hash) + 1, propertyURI.length());
	}

}

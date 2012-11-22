/**
 * 
 */
package com.mapper.utility;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mapper.indexer.DataIndexerImpl;

/**
 * @author Arnab Dutta
 * 
 */
public class Utilities {

	// define Logger
	static Logger logger = Logger.getLogger(Utilities.class.getName());

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
		for (int listCounter = 0; listCounter < resultList.size(); listCounter++) {
			logger.info(DataIndexerImpl.MAP_DBPEDIA_LITERALS.get(resultList
					.get(listCounter)));

		}

	}

}

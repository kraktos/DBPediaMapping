/**
 * 
 */
package com.mapper.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mapper.indexer.DataIndexerImpl;
import com.mapper.utility.Hasher;
import com.mapper.utility.Utilities;

/**
 * API for querying the knowledge base
 * 
 * @author Arnab Dutta
 * 
 */
public class QueryApi {

	static Logger logger = Logger.getLogger(QueryApi.class.getName());

	public static void fetchAnswers(final String queryString) {

		// figure out the hashed key for the given query string
		long hashedQueryKey = Hasher.hash64(queryString);
		logger.info("query = " + queryString + "  hashed key = "
				+ hashedQueryKey);

		// use the hashed key to pull out the information for the query key
		List<Long> resultList_AsSubject = DataIndexerImpl.MAP_DBPEDIA_SUB_LITERALS
				.get(hashedQueryKey);
		List<Long> resultList_AsObject = DataIndexerImpl.MAP_DBPEDIA_OBJ_LITERALS
				.get(hashedQueryKey);

		List<Long> newList = new ArrayList<Long>(resultList_AsSubject);
		newList.addAll(resultList_AsObject);

		Utilities.printList(newList);

	}
}

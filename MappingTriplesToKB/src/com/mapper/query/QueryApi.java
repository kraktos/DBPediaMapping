/**
 * 
 */
package com.mapper.query;

import java.util.List;

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
		List<Long> resultList = DataIndexerImpl.MAP_DBPEDIA_SUB_LITERALS.get(hashedQueryKey);
		logger.info("Result set Size = " + resultList.size());
		Utilities.printList(resultList);
		
		logger.info(DataIndexerImpl.MAP_DBPEDIA_SUB_LITERALS.size());
		
	}
}

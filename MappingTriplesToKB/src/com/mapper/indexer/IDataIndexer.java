/**
 * 
 */
package com.mapper.indexer;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.mapper.client.Main;

/**
 * @author Arnab Dutta
 * 
 */
public interface IDataIndexer {

	public Logger logger = Logger.getLogger(IDataIndexer.class.getName());

	/**
	 * reads data in chunks
	 * @throws IOException 
	 */
	public void readData() throws IOException;

	/**
	 * creates indexes over the DBPedia data
	 */
	void indexData(String subject, String predicate, String object);

}

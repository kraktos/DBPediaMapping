/**
 * 
 */
package com.mapper.indexer;

import org.apache.log4j.Logger;

import com.mapper.client.Main;

/**
 * @author Arnab Dutta
 * 
 */
public interface IDataIndexer {

	public Logger logger = Logger.getLogger(Main.class.getName());

	/**
	 * reads data in chunks
	 */
	public void readData();

	/**
	 * creates indexes over the data
	 */
	public void indexData();

}

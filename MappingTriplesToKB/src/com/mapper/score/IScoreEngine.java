/**
 * 
 */
package com.mapper.score;

import org.apache.log4j.Logger;

/**
 * @author Arnab Dutta
 * 
 */
public interface IScoreEngine {

	Logger logger = Logger.getLogger(IScoreEngine.class.getName());

	public int calculateScore();

	void readExtractedFacts(String filePath, String outputFilePath);
}

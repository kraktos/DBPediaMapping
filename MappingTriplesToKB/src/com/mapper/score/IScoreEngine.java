/**
 * 
 */
package com.mapper.score;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Arnab Dutta
 * 
 */
public interface IScoreEngine {

	// logger
	Logger logger = Logger.getLogger(IScoreEngine.class.getName());

	// The top k matches of similarity
	static int TOP_K = 8;

	// measures available
	public enum MEASURE {
		DICE, LEVENSTEIN
	}

	/**
	 * 
	 * @param propSourceFilePath
	 * @param propTargetFilePath
	 * @throws IOException
	 */
	public void calculateScore(final String propSourceFilePath,
			String propTargetFilePath) throws IOException;

	void readExtractedFacts(String filePath, String outputFilePath);
}

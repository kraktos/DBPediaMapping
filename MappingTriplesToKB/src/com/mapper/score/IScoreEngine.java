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
	static int TOP_K = 5;

	// measures available
	public enum MEASURE {
		DICE, LEVENSTEIN, FASTJOIN
	}

	/**
	 * 
	 * @param propSourceFilePath
	 * @param propTargetFilePath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void calculateScore(final String propSourceFilePath,
			String propTargetFilePath) throws IOException, InterruptedException;

	void readExtractedFacts(String filePath, String outputFilePath);
}

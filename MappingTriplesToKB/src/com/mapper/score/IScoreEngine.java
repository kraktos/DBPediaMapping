/**
 * 
 */
package com.mapper.score;

/**
 * @author Arnab Dutta
 * 
 */
public interface IScoreEngine {

	public void readExtractedFacts(final String filePath);

	public int calculateScore();
}

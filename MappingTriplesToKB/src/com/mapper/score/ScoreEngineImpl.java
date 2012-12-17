/**
 * 
 */
package com.mapper.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.csvreader.CsvWriter;
import com.ibm.icu.util.Measure;
import com.mapper.utility.FileUtil;

/**
 * @author Arnab Dutta
 * 
 */
public class ScoreEngineImpl implements IScoreEngine {

	private static final String DELIMIT = "\t";// "^([^\t]+)\t([^\t]+)\t([^\t]+)";

	// map to store the top k matched results
	static Map<String, Object> topKMap = new TreeMap<String, Object>();

	/**
	 * 
	 * @param propSourceFilePath
	 *            The IE output properties list
	 * @param propTargetFilePath
	 *            The DBPedia properties
	 * @param tOP_K2
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void calculateScoreForFileInput(final String propSourceFilePath,
			String propTargetFilePath) throws IOException, InterruptedException {

		// Find the score with different measures
		Similarity.extractLinesToCompare(propSourceFilePath,
				propTargetFilePath, TOP_K, MEASURE.DICE, topKMap);
	}

	/**
	 * 
	 * @param propSourceText
	 *            The IE output texts
	 * @param propTargetFilePath
	 *            The DBPedia properties
	 * @param tOP_K2
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void calculateScoreForTextInput(final String propSourceText,
			String propTargetFilePath) throws IOException, InterruptedException {

		// Find the score with different measures
		Similarity.compareTexts(propSourceText, propTargetFilePath, TOP_K,
				MEASURE.DICE, topKMap);
	}

	/**
	 * 
	 */
	public void readExtractedFacts(String inputTSVFilePath,
			String outputCSVFilePath) {

		String strLine;

		try {
			// before we open the file check to see if it already exists
			boolean alreadyExists = new File(outputCSVFilePath).exists();
			if (alreadyExists) {
				new File(outputCSVFilePath).delete();
			}

			BufferedReader br = new BufferedReader(new FileReader(
					inputTSVFilePath));

			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter(new FileWriter(
					outputCSVFilePath, true), ',');

			// read each lines and create a CSV file out of them
			while ((strLine = br.readLine()) != null) {
				FileUtil.writeToCSV(strLine.split(DELIMIT), csvOutput);
			}

			// close the stream once done
			csvOutput.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

/**
 * 
 */
package com.mapper.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.csvreader.CsvWriter;
import com.ibm.icu.util.Measure;
import com.mapper.utility.FileUtil;

/**
 * @author Arnab Dutta
 * 
 */
public class ScoreEngineImpl implements IScoreEngine {

	private static final String DELIMIT = "\t";// "^([^\t]+)\t([^\t]+)\t([^\t]+)";

	/**
	 * 
	 * @param propSourceFilePath
	 *            The IE output properties list
	 * @param propTargetFilePath
	 *            The DBPedia properties
	 * @param tOP_K2
	 * @throws IOException
	 */
	public void calculateScore(final String propSourceFilePath,
			String propTargetFilePath) throws IOException {

		// Measure Type I : Fast Join
		// FastJoinWrapper.join(propSourceFilePath, propTargetFilePath);

		Similarity.extractLinesToCompare(propSourceFilePath,
				propTargetFilePath, TOP_K, MEASURE.LEVENSTEIN);
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

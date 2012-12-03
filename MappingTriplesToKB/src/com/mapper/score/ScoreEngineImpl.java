/**
 * 
 */
package com.mapper.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.csvreader.CsvWriter;
import com.mapper.utility.FileUtil;

/**
 * @author Arnab Dutta
 * 
 */
public class ScoreEngineImpl implements IScoreEngine {

	private static final String DELIMIT = "\t";// "^([^\t]+)\t([^\t]+)\t([^\t]+)";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mapper.score.IScoreEngine#calculateScore()
	 */
	@Override
	public int calculateScore() {
		// TODO Auto-generated method stub
		return 0;
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

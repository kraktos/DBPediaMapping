package com.mapper.utility;

import java.io.IOException;

import com.csvreader.CsvWriter;

/**
 * Class to write a csv file
 * 
 * @author Arnab Dutta
 * 
 */
public class FileUtil {

	/**
	 * 
	 * @param strTokens
	 *            array of Strings, the values to be written in each row
	 * @param csvOutput
	 *            the reference to the output CSV file
	 */
	public static void writeToCSV(final String[] strTokens,
			final CsvWriter csvOutput) {

		try {

			// write out the records
			csvOutput.write(strTokens[0]);
			csvOutput.write(strTokens[1]);
			csvOutput.write(strTokens[2]);
			csvOutput.endRecord();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

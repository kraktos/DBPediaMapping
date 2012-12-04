package com.mapper.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.csvreader.CsvWriter;
import com.mapper.client.Main;

/**
 * Class to write a csv file
 * 
 * @author Arnab Dutta
 * 
 */
public class FileUtil {

	static Logger logger = Logger.getLogger(FileUtil.class.getName());

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
			csvOutput.write(strTokens[0]);// subject
			csvOutput.write(strTokens[1]);// predicate
			csvOutput.write(strTokens[2]);// object
			csvOutput.write(strTokens[4]);// truth value: Required later for
											// aposteriori probability
											// calculation
			csvOutput.endRecord();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeToFlatFile(BufferedWriter out, String text) {
		try {
			// Create file
			out.write(text);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void extractMatchingTuples(String userQuery, File file,
			BufferedWriter outProperty) {
		Scanner input;

		try {

			input = new Scanner(file);
			while (input.hasNext()) {
				String word = input.next();
				if (word.contains(userQuery)) {
					// Take these subset of data and match them against DBPedia
					// store them somehow and match the predicates
					writeToFlatFile(outProperty,
							Utilities.extractPredicatesFromTuples(word) + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

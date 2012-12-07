/**
 * 
 */
package com.mapper.score;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.mapper.score.IScoreEngine.MEASURE;

/**
 * Core class to plug in any similarity computation algorithm
 * 
 * @author Arnab Dutta
 * 
 */
public class Similarity {

	// logger
	static Logger logger = Logger.getLogger(Similarity.class.getName());

	/**
	 * 
	 * @param leftArg
	 *            file path of source texts
	 * @param rightArg
	 *            file path of target texts
	 * @param TOP_K
	 *            customizable top k ranked matches
	 * @param measure
	 *            The measure we are interested in
	 * @param topKMap
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void extractLinesToCompare(final String leftArg,
			final String rightArg, int TOP_K, MEASURE measure,
			Map<String, Object> topKMap) throws IOException,
			InterruptedException {

		if (measure.equals(MEASURE.FASTJOIN)) {
			// Measure Type I : Fast Join
			FastJoinWrapper.join(leftArg, rightArg, TOP_K, topKMap);
		} else {

			BufferedReader leftArgBuf = new BufferedReader(
					new InputStreamReader(new FileInputStream(leftArg)));

			String lineFromSourceFile;
			String lineFromTargetFile;

			int kCounter = 0;
			double score = 0;

			// start reading from the first file
			while ((lineFromSourceFile = leftArgBuf.readLine()) != null) {

				BufferedReader rightArgBuf = new BufferedReader(
						new InputStreamReader(new FileInputStream(rightArg)));

				// simultaneously start reading from the second file
				while ((lineFromTargetFile = rightArgBuf.readLine()) != null) {

					// Measure Type II : Levenstein Edit Distance
					if (measure.equals(MEASURE.LEVENSTEIN))
						score = levensteinEditScore(lineFromSourceFile,
								lineFromTargetFile);

					// Measure Type III: Dice Coefficient
					else if (measure.equals(MEASURE.DICE))
						score = diceCoefficient(lineFromSourceFile,
								lineFromTargetFile);

					addToCollection(lineFromSourceFile, lineFromTargetFile,
							score, topKMap);
				}

				// print out the top k result mathces for a property
				printTopKMatches(TOP_K, kCounter, topKMap);

				// reset counter for next property
				kCounter = 0;

				// clear the map for next set of property matches
				topKMap.clear();
			}
		}
	}

	/**
	 * @param lineFromSourceFile
	 * @param lineFromTargetFile
	 * @param score
	 * @param topKMap
	 */
	public static void addToCollection(String lineFromSourceFile,
			String lineFromTargetFile, double score, Map<String, Object> topKMap) {
		// put them in a collection,
		topKMap.put(lineFromSourceFile + " <-> " + lineFromTargetFile,
				new Double(score));
	}

	/**
	 * @param TOP_K
	 *            customizable top k ranked matches *
	 * @param kCounter
	 *            track the top K ranks
	 * @param topKMap
	 * @return
	 */
	public static void printTopKMatches(int TOP_K, int kCounter,
			Map<String, Object> topKMap) {
		for (Iterator<String> i = sortByValue(topKMap).iterator(); i.hasNext();) {
			String key = i.next();
			logger.info(" TOP " + TOP_K + " values = " + key + ", "
					+ topKMap.get(key));
			kCounter++;
			// once top k fetched break out
			if (kCounter == TOP_K) {
				break;
			}
		}

	}

	// ************ DICE CO-EFFICIENT
	// *********************************************************//

	/**
	 * @param lineFromSourceFile
	 * @param lineFromTargetFile
	 * @return lexical similarity value in the range [0,1], Higher the better
	 *         match
	 */
	private static double diceCoefficient(final String lineFromSourceFile,
			final String lineFromTargetFile) {

		ArrayList<String> pairs1 = wordLetterPairs(lineFromSourceFile
				.toUpperCase());
		ArrayList<String> pairs2 = wordLetterPairs(lineFromTargetFile
				.toUpperCase());
		int intersection = 0;
		int union = pairs1.size() + pairs2.size();
		for (int i = 0; i < pairs1.size(); i++) {
			Object pair1 = pairs1.get(i);
			for (int j = 0; j < pairs2.size(); j++) {
				Object pair2 = pairs2.get(j);
				if (pair1.equals(pair2)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}
		return (2.0 * intersection) / union;
	}

	/**
	 * 
	 * @param str
	 * @return an ArrayList of 2-character Strings.
	 */
	private static ArrayList<String> wordLetterPairs(String str) {
		ArrayList<String> allPairs = new ArrayList<String>();

		// Tokenize the string and put the tokens/words into an array
		String[] words = str.split("\\s");
		// For each word
		for (int w = 0; w < words.length; w++) {
			// Find the pairs of characters
			String[] pairsInWord = letterPairs(words[w]);
			for (int p = 0; p < pairsInWord.length; p++) {
				allPairs.add(pairsInWord[p]);
			}
		}
		return allPairs;
	}

	/**
	 * 
	 * @param str
	 * @return an array of adjacent letter pairs contained in the input string
	 */
	private static String[] letterPairs(String str) {
		int numPairs = str.length() - 1;
		String[] pairs = new String[numPairs];
		for (int i = 0; i < numPairs; i++) {
			pairs[i] = str.substring(i, i + 2);
		}
		return pairs;
	}

	// ******************************************************************************//

	// ************ LEVENSTEIN EDIT
	// *************************************************//

	/**
	 * 
	 * @param s1
	 *            argument string 1
	 * @param s2
	 *            argument string 2
	 * @return A score denoting the similatity between two strings. Lesser the
	 *         better
	 */
	public static double levensteinEditScore(final String s1, final String s2) {

		int[][] dp = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i < dp.length; i++) {
			for (int j = 0; j < dp[i].length; j++) {
				dp[i][j] = i == 0 ? j : j == 0 ? i : 0;
				if (i > 0 && j > 0) {

					if (s1.charAt(i - 1) == s2.charAt(j - 1))
						dp[i][j] = dp[i - 1][j - 1];
					else
						dp[i][j] = Math.min(dp[i][j - 1] + 1, Math.min(
								dp[i - 1][j - 1] + 1, dp[i - 1][j] + 1));
				}
			}
		}
		return (double) 1 / (dp[s1.length()][s2.length()]);
	}

	/**
	 * 
	 * @param map
	 *            input map to sort by values
	 * @return List of keys sorted by values
	 */
	public static List<String> sortByValue(final Map<String, Object> map) {
		List<String> keys = new ArrayList<String>();
		keys.addAll(map.keySet());
		Collections.sort(keys, new Comparator<Object>() {
			@SuppressWarnings("unchecked")
			public int compare(Object o1, Object o2) {
				Object v1 = map.get(o1);
				Object v2 = map.get(o2);
				if (v1 == null) {
					return (v2 == null) ? 0 : 1;
				} else if (v1 instanceof Comparable) {
					// negative coz we want the highest value first
					return -(((Comparable<Object>) v1).compareTo(v2));
				} else {
					return 0;
				}
			}
		});
		return keys;
	}
	// ******************************************************************************//
}

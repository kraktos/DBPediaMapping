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

/**
 * Core class to plug in any similarity computation algorithm
 * 
 * @author Arnab Dutta
 * 
 */
public class Similarity {

	// logger
	static Logger logger = Logger.getLogger(Similarity.class.getName());

	// map to store the top k matched results
	static Map<String, Long> topKMap = new TreeMap<String, Long>();

	/**
	 * 
	 * @param leftArg
	 *            file path of source texts
	 * @param rightArg
	 *            file path of targets texts
	 * @param TOP_K
	 *            customizable top k ranked matches
	 * @throws IOException
	 */
	public static void computeLevenstein(final String leftArg,
			final String rightArg, int TOP_K) throws IOException {

		BufferedReader leftArgBuf = new BufferedReader(new InputStreamReader(
				new FileInputStream(leftArg)));

		String lineFromLeftFile;
		String lineFromRightFile;

		int kCounter = 0;
		int score;

		// start reading from the first file
		while ((lineFromLeftFile = leftArgBuf.readLine()) != null) {

			BufferedReader rightArgBuf = new BufferedReader(
					new InputStreamReader(new FileInputStream(rightArg)));

			// simultaneously start reading from the second file
			while ((lineFromRightFile = rightArgBuf.readLine()) != null) {

				// compute the score
				score = levensteinEditScore(lineFromLeftFile, lineFromRightFile);

				// put them in a collection,
				topKMap.put(lineFromLeftFile + " <-> " + lineFromRightFile,
						new Long(score));
			}

			// print out the top k result mathces for a property
			printTopKMatches(TOP_K, kCounter);

			// reset counter for next property
			kCounter = 0;
		}
	}

	/**
	 * @param TOP_K
	 *            customizable top k ranked matches *
	 * @param kCounter
	 *            track the top K ranks
	 * @return
	 */
	public static void printTopKMatches(int TOP_K, int kCounter) {
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

		// clear the map for next set of property matches
		topKMap.clear();
	}

	/**
	 * 
	 * @param s1
	 *            argument string 1
	 * @param s2
	 *            argument string 2
	 * @return
	 */
	public static int levensteinEditScore(final String s1, final String s2) {

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
		return dp[s1.length()][s2.length()];
	}

	/**
	 * 
	 * @param map
	 *            input map to sort by values
	 * @return List of keys sorted by values
	 */
	public static List<String> sortByValue(final Map<String, Long> map) {
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
					return ((Comparable<Object>) v1).compareTo(v2);
				} else {
					return 0;
				}
			}
		});
		return keys;
	}

}

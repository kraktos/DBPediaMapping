package com.mapper.score;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mapper.utility.Utilities;

public class FastJoinWrapper {

	// define Logger
	static Logger logger = Logger.getLogger(FastJoinWrapper.class.getName());

	public static String FASTJOIN_EXE_UNX = "/home/arnab/Work/fastjoin/linux/FastJoin";
	public static String FASTJOIN_MEASURE = "FDICE";
	public static double FASTJOIN_DELTA = 0.2;
	public static double FASTJOIN_TAU = 0.2;

	/**
	 * @param TOP_K
	 * @param topKMap
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */

	public static void join(String sourcePath, String targetPath, int TOP_K,
			Map<String, Object> topKMap) throws InterruptedException,
			IOException {

		Set<String> setSourceLabels = new HashSet<String>();
		BufferedReader bri = null;
		Process process = null;

		try {
			String line;
			String sourceLabel = "";
			String targetLabel = "";

			process = Runtime.getRuntime().exec(
					FASTJOIN_EXE_UNX + " " + FASTJOIN_MEASURE + " "
							+ FASTJOIN_DELTA + " " + FASTJOIN_TAU + " "
							+ sourcePath + " " + targetPath + "");

			bri = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			int type = 0;
			int kCounter = 0;

			double confidence = 0.0;
			while ((line = bri.readLine()) != null) {
				String[] fields = line.split(" ");
				// *** type == 1 ***
				if (type == 1) {
					sourceLabel = line;
					type = 2;
				}
				// *** type == 2 ***
				else if (type == 2) {
					targetLabel = line;

					setSourceLabels.add(sourceLabel);

					// add them in a collection to figure out top k ranks
					Similarity.addToCollection(sourceLabel, targetLabel,
							confidence, topKMap);

					type = 0;

				}
				// *** type == 0 ***
				else if (type == 0) {
					try {
						confidence = Double.parseDouble(fields[0]);
						if (confidence >= 0.0 && confidence <= 1.0) {
							type = 1;
						}
					} catch (NumberFormatException e) {
					}
				}

			} // end of while

			// Different way to print for the fast join case
			for (String str : setSourceLabels) {
				for (Iterator<String> i = Similarity.sortByValue(topKMap)
						.iterator(); i.hasNext();) {
					String key = i.next();

					Double value = (Double) topKMap.get(key);

					if (key.contains(str + " <-> ")) {

						logger.info(" TOP " + TOP_K + " values = " + key + ", "
								+ value.doubleValue());
						kCounter++;
						// once top k fetched break out
						if (kCounter == TOP_K) {
							break;
						}
					}
				}
				kCounter = 0;
			}

		} catch (Exception err) {
			err.printStackTrace();
		} finally {
			setSourceLabels.clear();
			topKMap.clear();
			bri.close();
			process.waitFor();
		}
	}
}

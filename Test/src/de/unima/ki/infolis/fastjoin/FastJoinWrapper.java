package de.unima.ki.infolis.fastjoin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FastJoinWrapper {

	public static String FASTJOIN_EXE_UNX; //"/home/arnab/Work/fastjoin/linux/FastJoin";// "E:/projects/infolis/code/uma-cs/LOHAI2/fastjoin/win32/FastJoin.exe";
	public static String FASTJOIN_MEASURE = "FDICE";
	public static double FASTJOIN_DELTA = 0.4;
	public static double FASTJOIN_TAU = 0.4;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length > 0) {
			FASTJOIN_EXE_UNX = args[0];
		}
		
		join("/home/arnab/Work/fastjoin/test/source.txt",
				"/home/arnab/Work/fastjoin/test/target.txt");
	}

	public static void join(String sourcePath, String targetPath) {
		try {
			String line;
			Process p = Runtime.getRuntime().exec(
					FASTJOIN_EXE_UNX + " " + FASTJOIN_MEASURE + " "
							+ FASTJOIN_DELTA + " " + FASTJOIN_TAU + " "
							+ sourcePath + " " + targetPath + "");
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			int type = 0;
			String sourceLabel = "";
			String targetLabel = "";
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
					//
					System.out.println(sourceLabel + " ~ " + targetLabel
							+ "  =>" + confidence);
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
			}
			bri.close();
			p.waitFor();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

}


package de.dws.helper.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileOverlap {

    private static final String GS = "/home/arnab/Work/data/experiments/reasoning/newGS/ALL.tsv";
    private static final String sample = "/home/arnab/Work/data/experiments/reasoning/newBL/";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        computeOverlap("lakeinstate");
    }

    private static void computeOverlap(String predicate) throws IOException {

        BufferedReader goldReaer = new BufferedReader(new FileReader(GS));
        BufferedReader baseReaer = new BufferedReader(new FileReader(Constants.BL));

        BufferedWriter sampleWriter = new BufferedWriter(new FileWriter(
                sample + "sample_" + predicate + ".tsv"));

        String gLine;
        String[] goldElemnts = null;
        String[] baseElemnts = null;

        String bLine;

        int counter = 0;
        // while ((bLine = baseReaer.readLine()) != null) {

        while ((gLine = goldReaer.readLine()) != null && (bLine = baseReaer.readLine()) != null) {
            goldElemnts = gLine.split("\t");
            baseElemnts = bLine.split("\t");

            if (goldElemnts[1].equals(predicate))
            {
                if (gLine.indexOf("INCORRECT") == -1 && gLine.indexOf("en.wikipedia") == -1
                        && gLine.indexOf("?") == -1 &&
                        gLine.indexOf("dbpedia.org") != -1) {
                    if (goldElemnts[0].equals(baseElemnts[0])
                            && goldElemnts[1].equals(baseElemnts[1])
                            && goldElemnts[2].equals(baseElemnts[2])) {
                        sampleWriter.write(bLine + "\n");
                        counter++;
                    }
                }
            }
        }
        sampleWriter.close();

        System.out.println(counter);
    }
}

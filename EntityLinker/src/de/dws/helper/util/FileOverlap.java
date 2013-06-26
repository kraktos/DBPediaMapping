
package de.dws.helper.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.dws.reasoner.GenericConverter;

public class FileOverlap {

    private static final String FULL_GS_DUMP = "/home/arnab/Work/data/experiments/reasoning/newGS/ALL.tsv";
    private static final String sample = "/home/arnab/Work/data/experiments/reasoning/newBL/ds_";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        computeOverlap(Constants.PREDICATE);
    }

    private static void computeOverlap(String predicate) throws IOException {

        new File(sample + predicate ).mkdir();
        
        
        BufferedReader goldReader = new BufferedReader(new FileReader(FULL_GS_DUMP));
        BufferedReader baseReaer = new BufferedReader(new FileReader(Constants.BL));

        BufferedWriter blWriter = new BufferedWriter(new FileWriter(
                sample + predicate + "/bl_" + predicate + ".tsv"));

        BufferedWriter collectiveWriter = new BufferedWriter(new FileWriter(
                sample + predicate + "/goldBL_" + predicate + ".tsv"));

        String gLine;
        String[] goldElemnts = null;
        String[] baseElemnts = null;

        String bLine;

        int counter = 0;
        // while ((bLine = baseReaer.readLine()) != null) {

        while ((gLine = goldReader.readLine()) != null && (bLine = baseReaer.readLine()) != null) {
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

                        blWriter.write(bLine + "\n");

                        collectiveWriter.write(bLine + "\t" + goldElemnts[3] + "\t"
                                + goldElemnts[4] + "\n");

                        counter++;
                    }
                }
            }
        }
        blWriter.close();
        collectiveWriter.close();
        System.out.println(counter);
    }

}

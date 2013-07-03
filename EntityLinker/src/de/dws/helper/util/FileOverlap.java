
package de.dws.helper.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.dws.mapper.dbConnectivity.DBWrapper;
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

        new File(sample + predicate).mkdir();

        BufferedReader goldReader = new BufferedReader(new FileReader(FULL_GS_DUMP));
        BufferedReader baseReaer = new BufferedReader(new FileReader(Constants.BL));

        BufferedWriter nellConfWriter = new BufferedWriter(new FileWriter(
                Constants.NELL_CONFIDENCE_FILE));

        BufferedWriter blWriter = new BufferedWriter(new FileWriter(
                sample + predicate + "/bl_" + predicate + ".tsv"));

        
        BufferedWriter collectiveWriter = new BufferedWriter(new FileWriter(
                Constants.INPUT_CSV_FILE));
        
        

        // sample + predicate + "/goldBL_" + predicate + ".tsv"));

        String gLine = null;
        String[] goldElemnts = null;
        String[] baseElemnts = null;

        String bLine = null;

        int counter = 0;
        // while ((bLine = baseReaer.readLine()) != null) {

        try {
            while ((gLine = goldReader.readLine()) != null
                    && (bLine = baseReaer.readLine()) != null) {

                goldElemnts = gLine.split("\t");
                baseElemnts = bLine.split("\t");

                if (goldElemnts[1].equals(predicate))
                {
                    
                    // gLine.indexOf("en.wikipedia") == -1
                    // gLine.indexOf("INCORRECT") == -1
                    if (gLine.indexOf("?") == -1 &&
                            gLine.indexOf("dbpedia.org") != -1) {

//                        System.out.println(gLine);
//                        System.out.println(goldElemnts.length);
//                        
                        
                        if (goldElemnts[0].equals(baseElemnts[0])
                                && goldElemnts[1].equals(baseElemnts[1])
                                && goldElemnts[2].equals(baseElemnts[2])) {

                            blWriter.write(bLine + "\n");

                           
                            //System.out.println(goldElemnts.length);
                            if(goldElemnts.length > 5){
                                collectiveWriter.write(bLine + "\t" +
                                        goldElemnts[3] + "\t"
                                        + goldElemnts[4] + "\t" + "IC" + "\n");
                                
                            }
                            
                            if(goldElemnts.length == 5){
                                collectiveWriter.write(bLine + "\t" +
                                        goldElemnts[3] + "\t"
                                        + goldElemnts[4] + "\t" + "C" + "\n");                                
                            }                               
                                

                            findConfidences(goldElemnts[0], goldElemnts[1], goldElemnts[2],
                                    nellConfWriter);

                            counter++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception with " + gLine + "  \n" + bLine);
        }

        // close the reader writer streams
        nellConfWriter.close();
        blWriter.close();
        collectiveWriter.close();
        
        System.out.println(counter);
    }

    /**
     * for the given subset of data, fetch its confidences and create a file in
     * its folder
     * 
     * @param sub
     * @param pred
     * @param obj
     * @param nellConfWriter
     * @throws IOException
     */
    private static void findConfidences(String sub, String pred, String obj,
            BufferedWriter nellConfWriter) throws IOException {
        // As we read the triples, generate the confidence values from the table

        DBWrapper.init(Constants.GET_NELL_CONF);

        List<Double> listConf = DBWrapper.fetchNELLConfidence(sub, pred, obj);

        nellConfWriter.write(sub + "\t" + pred + "\t" + obj + "\t"
                + String.valueOf(listConf.get(0)) + "\n");

        DBWrapper.shutDown();
    }

}

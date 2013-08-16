
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

    private static final String DISTINCT_TYPE_DOM = "select distinct dom_type from PREDTYPE_DOM where pred = ?";
    private static final String DISTINCT_TYPE_RAN = "select distinct ran_type from PREDTYPE_RAN where pred = ?";

    private static final String DISTINCT_INSTANCES_BY_PRED_DOM = "select count(distinct dom_inst) from PREDTYPE_DOM where pred = ?";
    private static final String DISTINCT_INSTANCES_BY_PRED_RAN = "select count(distinct ran_inst) from PREDTYPE_RAN where pred = ?";

    private static final String DISTINCT_INST_DOM = "select count(distinct pred, dom_inst) from  PREDTYPE_DOM  where pred = ? and  dom_type = ? ";
    private static final String DISTINCT_INST_RAN = "select count(distinct pred, ran_inst) from  PREDTYPE_RAN  where pred = ? and  ran_type = ? ";

    private static final String FULL_GS_DUMP = "/home/arnab/Work/data/experiments/reasoning/newGS/ALL.tsv";
    private static final String sample = "/home/arnab/Work/data/experiments/reasoning/newBL/ds_";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // here we chunk out only the data pertaining to a predicate...
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

        int validNELLTriplesCount = 0;
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

                        // System.out.println(gLine);
                        // System.out.println(goldElemnts.length);
                        //

                        if (goldElemnts[0].equals(baseElemnts[0])
                                && goldElemnts[1].equals(baseElemnts[1])
                                && goldElemnts[2].equals(baseElemnts[2])) {

                            blWriter.write(bLine + "\n");

                            // System.out.println(goldElemnts.length);
                            if (goldElemnts.length > 5) {
                                collectiveWriter.write(bLine + "\t" +
                                        goldElemnts[3] + "\t"
                                        + goldElemnts[4] + "\t" + "IC" + "\n");
                            }

                            if (goldElemnts.length == 5) {
                                collectiveWriter.write(bLine + "\t" +
                                        goldElemnts[3] + "\t"
                                        + goldElemnts[4] + "\t" + "C" + "\n");
                            }

                            findConfidences(goldElemnts[0], goldElemnts[1], goldElemnts[2],
                                    nellConfWriter);

                            validNELLTriplesCount++;

                        }
                    }
                }
            }

            // once done with this, we can calculate the domain and range
            // confidences from our distant supervision approach

            computeDomainConf(predicate, validNELLTriplesCount);
            computeRangeConf(predicate, validNELLTriplesCount);

        } catch (Exception e) {
            System.out.println("Exception with " + gLine + "  \n" + bLine);
        }

        // close the reader writer streams
        nellConfWriter.close();
        blWriter.close();
        collectiveWriter.close();

        System.out.println(validNELLTriplesCount);
    }

    private static void computeDomainConf(String predicate, int validNELLTriplesCount)
            throws IOException {
        double coOccCount = 0;
        int distinctInstancesByPred = 0;
        double domConf = 0D;

        BufferedWriter domConfMLNWriter = new BufferedWriter(
                new FileWriter(
                        Constants.DOMCONF));

        DBWrapper.init(DISTINCT_TYPE_DOM);

        List<String> types = DBWrapper.getTypes(predicate);

        DBWrapper.init(DISTINCT_INSTANCES_BY_PRED_DOM);

        distinctInstancesByPred = DBWrapper.getDistinctInstancesByPred(predicate);

        DBWrapper
                .init(DISTINCT_INST_DOM);

        for (String type : types) {
            coOccCount = DBWrapper.getInstances(predicate, type);

            domConf = (coOccCount / (double) distinctInstancesByPred) * validNELLTriplesCount;
//            System.out.println(predicate + "\t" + type + "\t" + coOccCount +
//                    "\t"
//                    + distinctInstancesByPred);

            createDomConfMLN(predicate, type, domConf, domConfMLNWriter);

        }

        domConfMLNWriter.close();

        DBWrapper.shutDown();

    }

    private static void computeRangeConf(String predicate, int validNELLTriplesCount)
            throws IOException {

        double coOccCount = 0;
        int distinctInstancesByPred = 0;
        double ranConf = 0D;

        BufferedWriter ranConfMLNWriter = new BufferedWriter(
                new FileWriter(
                        Constants.RANCONF));

        DBWrapper.init(DISTINCT_TYPE_RAN);

        List<String> types = DBWrapper.getTypes(predicate);

        DBWrapper.init(DISTINCT_INSTANCES_BY_PRED_RAN);

        distinctInstancesByPred = DBWrapper.getDistinctInstancesByPred(predicate);

        DBWrapper
                .init(DISTINCT_INST_RAN);

        for (String type : types) {
            coOccCount = DBWrapper.getInstances(predicate, type);

            ranConf = (coOccCount / (double) distinctInstancesByPred) * validNELLTriplesCount;
//            System.out.println(predicate + "\t" + type + "\t" + coOccCount +
//                    "\t"
//                    + distinctInstancesByPred);

            createRanConfMLN(predicate, type, ranConf, ranConfMLNWriter);

        }

        ranConfMLNWriter.close();

        DBWrapper.shutDown();

    }

    private static void createDomConfMLN(String predicate, String type, double domConf,
            BufferedWriter domConfMLNWriter) throws IOException {

        domConfMLNWriter.write("domConf(\"NELL#Predicate/" + predicate + "\", \"DBP#ontology/"
                + Utilities.cleanDBpediaURI(type) + "\"," + domConf + ")\n");

    }

    private static void createRanConfMLN(String predicate, String type, double ranConf,
            BufferedWriter ranConfMLNWriter) throws IOException {

        ranConfMLNWriter.write("ranConf(\"NELL#Predicate/" + predicate + "\", \"DBP#ontology/"
                + Utilities.cleanDBpediaURI(type) + "\"," + ranConf + ")\n");

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

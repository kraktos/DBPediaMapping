/**
 * 
 */

package de.dws.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.reasoner.GenericConverter;
import de.dws.reasoner.owl.OWLCreator;

/**
 * Creates a RDF file from a csv/tsv input file
 * 
 * @author Arnab Dutta
 */
public class CSVToRDF {

    /**
     * @param args
     * @throws OWLOntologyCreationException
     * @throws IOException
     */
    public static void main(String[] args) throws OWLOntologyCreationException, IOException {

        creatRDF("/home/arnab/Dropbox/PHD_WORK/data2/dataDump.tsv", "\t",
                "/home/arnab/Dropbox/PHD_WORK/data2/dataDump.owl");
    }

    private static void creatRDF(String inputCsvFile, String delimit, String outputOwlFile)
            throws OWLOntologyCreationException, IOException {
        OWLCreator owlCreator = new OWLCreator(Constants.OIE_ONTOLOGY_NAMESPACE);
        String line = null;
        String[] elements = null;

        String nellSub = null;
        String nellSubPFxd = null;

        String nellPred = null;

        String nellObj = null;
        String nellObjPFxd = null;

        String blSubj = null;
        String blObj = null;

        String goldSub = null;
        String goldObj = null;

        BufferedReader tupleReader = new BufferedReader(new FileReader(inputCsvFile));
        GenericConverter.MAP_COUNTER.clear();
        
        BufferedWriter dataReWriter = new BufferedWriter(new FileWriter(
                inputCsvFile+".cp"));
        
        while ((line = tupleReader.readLine()) != null) {
            elements = line.split(delimit);

            nellSub = Utilities.cleanse(elements[0]);
            nellPred = Utilities.cleanse(elements[1]);
            nellObj = Utilities.cleanse(elements[2]);

            nellSubPFxd = Allgn.generateUniqueURI(nellSub); //elements[3];
            nellObjPFxd = Allgn.generateUniqueURI(nellObj); //elements[4];
            goldSub = elements[5];
            goldObj = elements[6];

            blSubj = elements[7];
            blObj = elements[8];
            

//            System.out.println(elements[0] + "\t" + elements[1] + "\t" + elements[2] + "\t"
//                    + nellSubPFxd + "\t" + nellObjPFxd);
            
            dataReWriter.write(nellSub + "\t" + nellPred + "\t" + nellObj + "\t" + nellSubPFxd + "\t" + nellObjPFxd + "\t" + goldSub + "\t" + goldObj
                    + "\t" + blSubj + "\t" + blObj + "\n");
            
            if(!goldSub.trim().equals("?") && !goldObj.trim().equals("?"))
            owlCreator
                    .createAnnotatedAxioms(nellSub, nellPred, nellObj, nellSubPFxd,
                            Utilities
                                    .characterToUTF8(goldSub.replaceAll(
                                            "http://dbpedia.org/resource/", "")), nellObjPFxd,
                            Utilities
                                    .characterToUTF8(goldObj.replaceAll(
                                            "http://dbpedia.org/resource/", "")));

        }

        dataReWriter.close();
        
        // flush to file
        owlCreator.createOutput(outputOwlFile);
    }
}

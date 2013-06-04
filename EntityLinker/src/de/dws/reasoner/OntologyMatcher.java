/**
 * 
 */

package de.dws.reasoner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Arnab Dutta
 */
public class OntologyMatcher {

    // private static final String INPUT_CSV_FILE =
    // "/home/arnab/Work/data/NELL/ontology/NELL.ontology.csv";
    // private static final String OUTPUT_OWL_FILE =
    // "resources/NellOntology.owl";
    
    private static final String INPUT_CSV_FILE =
            "/home/arnab/Work/data/NELL/ontology/NELLPredMatches.csv";
    private static final String OUTPUT_OWL_FILE = "resources/NellPredMatch.owl";

//    private static final String INPUT_CSV_FILE =
//            "/home/arnab/Work/data/NELL/ontology/NELLBaseline.csv";
//    private static final String OUTPUT_OWL_FILE = "resources/NELLBaseline.owl";

    
    // define Logger
    static Logger logger = Logger.getLogger(OntologyMatcher.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("resources/log4j.properties");

        GenericConverter.convertCsvToOwl(INPUT_CSV_FILE, ",", GenericConverter.TYPE.NELL_PRED_ANNO,
                OUTPUT_OWL_FILE);

        // GenericConverter.print();
    }

}

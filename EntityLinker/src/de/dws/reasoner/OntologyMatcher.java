/**
 * 
 */

package de.dws.reasoner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.dws.helper.util.Constants;

/**
 * @author Arnab Dutta
 */
public class OntologyMatcher {

    // private static final String INPUT_CSV_FILE =
    // "/home/arnab/Work/data/NELL/ontology/NELL.ontology.csv";
    // private static final String OUTPUT_OWL_FILE =
    // "/home/arnab/Work/data/NELL/ontology/NellOntology.owl";
    // private static final String DELIMIT = "\t";

    // private static final String INPUT_CSV_FILE =
    // "/home/arnab/Work/data/NELL/ontology/NELLPredMatches.csv";
    // private static final String OUTPUT_OWL_FILE =
    // "/home/arnab/Work/data/NELL/ontology/NellPredMatch.owl";
    // private static final String DELIMIT = "\t";

    // define Logger
    static Logger logger = Logger.getLogger(OntologyMatcher.class.getName());

    /**
     * @param args
     * @throws OWLOntologyCreationException
     */
    public static void main(String[] args) throws OWLOntologyCreationException {
//        PropertyConfigurator
//                .configure("resources/log4j.properties");

        GenericConverter.convertCsvToOwl(Constants.INPUT_CSV_FILE, Constants.DELIMIT_INPUT_CSV,
                GenericConverter.TYPE.NELL_ABOX, Constants.OUTPUT_OWL_FILE);        
    }

}

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

    private static final String INPUT_CSV_FILE = "/home/arnab/Work/data/NELL/ontology/NELL.ontology.csv";
    // define Logger
    static Logger logger = Logger.getLogger(OntologyMatcher.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("resources/log4j.properties");

        GenericConverter.convertCsvToOwl(INPUT_CSV_FILE, "\t");
        //GenericConverter.print();
    }

}

/**
 * 
 */

package de.dws.experiment;

import de.dws.helper.util.Constants;
import de.dws.helper.util.FileOverlap;
import de.dws.reasoner.OntologyMatcher;
import de.dws.reasoner.mln.MLNFileGenerator;
import de.dws.standards.baseLine.GetProbability;

/**
 * @author Arnab Dutta
 */
public class ExperimentAutomation {

    public static String PREDICATE = "bookwriter";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 1) {

            System.out.println("Usage : java -jar GO.jar <property name>");
            return;
        } else {
            PREDICATE = args[0];
        }

        // Create the subset of data from the dump for the fiven predicate
        // defined in Constants.PREDICATE
        FileOverlap.main(null);

        // Run the data files to create owl file and goldStandard MLN and
        // isOfTypeConfMLN
        OntologyMatcher.main(null);

        // create the same as prior weights
        GetProbability.main(null);

        // create MLN for sameAsConf
        for (int k = 1; k <= 3; k++) {
            MLNFileGenerator.main(new String[] {
                    Constants.OUTPUT_OWL_FILE,
                    Constants.DIRECTORY + "sameAsConf.nell-dbpedia-top" + k + ".db",
                    "sameAsConf",
                    String.valueOf(k)
            });
        }

        // create MLN for propAsst
        MLNFileGenerator.main(new String[] {
                Constants.OUTPUT_OWL_FILE,
                Constants.DIRECTORY + "propAsstConf.nell.db", "propAsstConf"
        });

    }
}

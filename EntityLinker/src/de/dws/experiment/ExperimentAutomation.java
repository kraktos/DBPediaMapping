/**
 * 
 */

package de.dws.experiment;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.dws.helper.util.Constants;
import de.dws.helper.util.FileOverlap;
import de.dws.reasoner.OntologyMatcher;
import de.dws.reasoner.mln.MLNFileGenerator;
import de.dws.standards.baseLine.GetProbability;

/**
 * @author Arnab Dutta
 */
public class ExperimentAutomation {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        FileOverlap.main(null);
        OntologyMatcher.main(null);
        GetProbability.main(null);

        for (int k = 1; k <= 3; k++) {
            MLNFileGenerator.main(new String[] {
                    Constants.OUTPUT_OWL_FILE,
                    Constants.DIRECTORY + "sameAsConf.nell-dbpedia-top" + k + ".db",
                    "sameAsConf",
                    String.valueOf(k)
            });
        }

        MLNFileGenerator.main(new String[] {
                Constants.OUTPUT_OWL_FILE,
                Constants.DIRECTORY + "propAsstConf.nell.db", "propAsstConf"
        });

    }
}

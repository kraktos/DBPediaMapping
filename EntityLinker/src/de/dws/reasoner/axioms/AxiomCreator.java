/**
 * 
 */
package de.dws.reasoner.axioms;

import java.io.File;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.dws.mapper.helper.util.Constants;

/**
 * This class is responsible for creating axioms out of a input .owl file Usually the downloadable owl files (say TBox
 * infos) do not have axioms. They just contain disjointness, subclassOf information. need to parse those and create an
 * output owl file. This allows to create asioms with weights(soft constraints) and also unweighted (hard constraints)
 * 
 * @author Arnab Dutta
 */
public class AxiomCreator
{

    /**
     * logger
     */
    public Logger logger = Logger.getLogger(AxiomCreator.class.getName());

    OWLOntologyManager manager = null;

    IRI documentIRI = null;

    OWLOntology owlOntology = null;

    public AxiomCreator()
    {
        // Get hold of an ontology manager
        manager = OWLManager.createOWLOntologyManager();
    }

    public void loadOntology(final String owlPath)
    {

        File file = new File(owlPath);
        // load the local copy
        try {
            owlOntology = manager.loadOntologyFromOntologyDocument(file);

            // We can always obtain the location where an ontology was loaded from
            documentIRI = manager.getOntologyDocumentIRI(owlOntology);
            logger.info("    from: " + owlOntology); //$NON-NLS-1$

        } catch (OWLOntologyCreationException e) {
            logger.error("Esception in loading owl file " + file.getAbsolutePath() + " " + e.getMessage());
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        new AxiomCreator().loadOntology(Constants.OWL_INPUT_FILE_PATH);
    }

}

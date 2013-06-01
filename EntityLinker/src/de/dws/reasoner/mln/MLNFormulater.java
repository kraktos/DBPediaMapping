/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import de.dws.helper.util.Constants;

/**
 * This class formulates MLN ground atoms (observed variables) from a given set
 * of axioms. Currently, it accepts an .owl file and creates a model file (.mln)
 * from the bunch of axioms
 * 
 * @author Arnab Dutta
 */
public class MLNFormulater {

    /**
     * logger
     */
    public static Logger logger = Logger.getLogger(MLNFormulater.class.getName());

    /**
     * OWLOntologyManager instance
     */
    OWLOntologyManager manager = null;

    /**
     * OWLOntology instance
     */
    static OWLOntology ontology = null;

    /**
     * OWLDataFactory instance
     */
    static OWLDataFactory factory = null;

    static PrefixManager prefixPredicateIE = null;
    static PrefixManager prefixConceptIE = null;

    /**
     * location where evidences of the model are dumped
     */
    private static final String MLN_EVIDENCE_FILE = "resources/evidence.db";

    public MLNFormulater(String owlFilePath) {
        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        try {

            logger.info("Loading " + owlFilePath);

            // load the first ontology WITH the annotations
            ontology = manager.loadOntology(IRI
                    .create("file:" + owlFilePath));

            // Get hold of a data factory from the manager and
            factory = manager.getOWLDataFactory();

            // set up a prefix manager to make things easier
            prefixPredicateIE = new DefaultPrefixManager(IRI.create(
                    Constants.ONTOLOGY_EXTRACTION_PREDICATE_NS).toString());

            prefixConceptIE = new DefaultPrefixManager(IRI.create(
                    Constants.ONTOLOGY_EXTRACTION_CONCEPT_NS).toString());

        } catch (OWLOntologyCreationException e) {
            logger.error(" error loading ontology file  "
                    + owlFilePath + " " + e.getMessage());
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("resources/log4j.properties");

        try {
            new MLNFormulater(
                    "resources/NellOntology.owl")
                    .convertOWLToMLN();
        } catch (IOException e) {
            logger.error("Error reading owl file ");
        }
    }

    /**
     * @throws IOException
     */
    public void convertOWLToMLN() throws IOException {
        String arg1 = null;
        String arg2 = null;
        String axiomType = null;
        String value = null;
        String[] elements = null;

        // the file where the evidences for the MLN are written out
        FileWriter fw = new FileWriter(MLN_EVIDENCE_FILE);
        BufferedWriter bw = new BufferedWriter(fw);

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontology.getAxioms();
        for (OWLAxiom axiom : allAxioms) {

            /**
             * since it returns a set of classes, the order is never promised,
             * hence this weird way of getting the two arguments of the axiom
             */
            // separates the axiom and the arguments
            elements = axiom.toString().split("\\(");
            axiomType = elements[0];

            if (elements.length == 2) {
                if (elements[1] != null) {
                    arg1 = elements[1].split("\\s")[0];
                    arg2 = elements[1].split("\\s")[1];

                    value = alterSemantics(axiomType,
                            arg1, arg2);

                    if (value != null) {
                        bw.write(value + "(" + removeTags(arg1)
                                + ", " + removeTags(arg2) + ")\n");
                    }
                }
            }
        }
        // close the stream
        bw.close();
        logger.info("Done writing to " + MLN_EVIDENCE_FILE);
    }

    /**
     * Axioms are transformed to the semantics of the model
     * 
     * @param axiomType type of axiom
     * @param arg1 first argument
     * @param arg2 second argument
     * @return altered semantics for the axioms
     */
    private String alterSemantics(String axiomType, String arg1, String arg2) {
        if (axiomType.toString().equals("DisjointClasses"))
            return "cdis";
        if (axiomType.toString().equals("SubClassOf")
                && arg1.toString().indexOf("OpenInfoExtraction#Predicate/") != -1
                && arg2.toString().indexOf("OpenInfoExtraction#Predicate/") != -1)
            return "psub";
        if (axiomType.toString().equals("SubClassOf")
                && arg2.toString().indexOf("OpenInfoExtraction#Concept/") != -1
                && arg1.toString().indexOf("OpenInfoExtraction#Concept/") != -1)
            return "csub";

        if (axiomType.toString().equals("ObjectPropertyRange"))
            return "ran";

        if (axiomType.toString().equals("ObjectPropertyDomain"))
            return "dom";

        return null;
    }

    /**
     * cleanes of the "<" or ">" on the concepts
     * 
     * @param arg value to be cleaned
     * @return
     */
    private String removeTags(String arg) {

        arg = arg.toString().replaceAll("<", "");
        arg = arg.replaceAll(">", "");
        arg = arg.replaceAll("\\)", "");
        return "\"" + arg + "\"";
    }
}

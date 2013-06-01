/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

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
    private static final String MLN_MAT_EVIDENCE_FILE = "resources/evidenceMaterialized.db";

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
            if (true) {
                new MLNFormulater(
                        "resources/NellOntology.owl")
                        .convertOWLToMaterializedMLN();
                logger.info("Done writing to " + MLN_MAT_EVIDENCE_FILE);
            }
            else {
                new MLNFormulater(
                        "resources/NellOntology.owl")
                        .convertOWLToMLN();

                logger.info("Done writing to " + MLN_EVIDENCE_FILE);
            }

        } catch (IOException e) {
            logger.error("Error reading owl file ");
        }
    }

    /**
     * Creates a MLN from the provided input owl file. This is materialized
     * ontology.
     * 
     * @throws IOException
     */
    public void convertOWLToMaterializedMLN() throws IOException {
        String value = null;

        Set<OWLClass> setClasses = null;
        NodeSet<OWLClass> subClasses = null;
        NodeSet<OWLClass> disjClasses = null;

        // the file where the evidences for the MLN are written out
        FileWriter fw = new FileWriter(MLN_MAT_EVIDENCE_FILE);
        BufferedWriter bw = new BufferedWriter(fw);

        // Use Pellet
        PelletExplanation.setup();
        logger.info("loaded pellet ");

        // Create the reasoner and load the ontology
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);
        logger.info("loaded reasoner ");

        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);
        logger.info("loaded explaination ");

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontology.getAxioms();
        for (OWLAxiom axiom : allAxioms) {
            // if os subclass type (concepts and predicates both)
            if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF) {

                // get the classes involved, here order doesnot matter
                setClasses = axiom.getClassesInSignature();

                for (OWLClass arg1Class : setClasses) {

                    // ask reasoner who are its sub classes
                    subClasses = reasoner.getSubClasses(arg1Class, true);

                    // pair with each one of them and form a subsumption
                    // relation
                    for (Node<OWLClass> arg2Class : subClasses) {

                        value = alterSemantics(axiom.getAxiomType().toString(),
                                arg2Class.toString(), arg1Class.toString());

                        if (value != null) {
                            writeToFile(value, arg2Class.toString(),
                                    arg1Class.toString(), bw);
                        }
                    }
                }
            }

            if (axiom.getAxiomType() == AxiomType.DISJOINT_CLASSES) {
                // get the classes involved, here order doesnot matter
                setClasses = axiom.getClassesInSignature();

                for (OWLClass arg1Class : setClasses) {

                    // ask reasoner who are its disjoint classes
                    disjClasses = reasoner.getDisjointClasses(arg1Class);

                    // pair with each one of them and form a subsumption
                    // relation
                    for (Node<OWLClass> arg2Class : disjClasses) {

                        value = alterSemantics(axiom.getAxiomType().toString(),
                                arg2Class.toString(), arg1Class.toString());

                        if (value != null) {
                            writeToFile(value, arg2Class.toString(),
                                    arg1Class.toString(), bw);
                        }
                    }
                }
            }
        }
        // close the stream
        bw.close();
    }

    /**
     * Creates a MLN from the provided input owl file. This is non-materialized
     * ontology
     * 
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
                        writeToFile(value, arg1, arg2, bw);
                    }
                }
            }
        }
        // close the stream
        bw.close();
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
     * cleans of the "<" or ">" on the concepts
     * 
     * @param arg value to be cleaned
     * @return
     */
    private String removeTags(String arg) {

        arg = arg.toString().replaceAll("<", "");
        arg = arg.replaceAll(">", "");
        arg = arg.replaceAll("\\)", "");
        arg = arg.replaceAll("Node\\(", "");
        return "\"" + arg.trim() + "\"";
    }

    /**
     * @param arg1
     * @param arg2
     * @param axiomType
     * @param bw
     * @throws IOException
     */
    public void writeToFile(String axiomType, String arg1, String arg2, BufferedWriter bw)
            throws IOException {
        bw.write(axiomType + "(" + removeTags(arg1)
                + ", " + removeTags(arg2) + ")\n");
    }
}

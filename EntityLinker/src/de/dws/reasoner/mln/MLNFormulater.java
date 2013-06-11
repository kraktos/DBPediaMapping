/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
import de.dws.helper.util.Utilities;

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
    static OWLOntology ontologyABOX = null;
    static OWLOntology ontologyTBOX = null;

    /**
     * OWLDataFactory instance
     */
    static OWLDataFactory factory = null;

    /**
     * PrefixManager instance
     */

    PrefixManager prefixPredicateIE = null;
    PrefixManager prefixConceptIE = null;
    PrefixManager prefixInstanceIE = null;

    PrefixManager prefixDBPediaConcept = null;
    PrefixManager prefixDBPediaPredicate = null;
    PrefixManager prefixDBPediaInstance = null;

    /**
     * location where evidences of the model are dumped
     */
    private static final String MLN_EVIDENCE_FILE = "/home/arnab/Work/data/experiments/reasoning/evidence.db";
    private static final String MLN_MAT_EVIDENCE_FILE = "resources/evidenceMaterialized.db";

    private static final String TBOX_OWL_INPUT = "/home/arnab/Work/data/experiments/reasoning/TBOX.owl";

    private static final String ABOX_OWL_INPUT = "/home/arnab/Work/data/NELL/ontology/wrong.owl";

    // "/home/arnab/Work/data/experiments/reasoning/NELLBaseline.owl"; //
    // "/home/arnab/Work/data/NELL/ontology/wrong.owl";

    // "/home/arnab/Work/data/experiments/reasoning/wrong.owl";

    /**
     * overloaded constructor where we want frame MLN from both the ABox and
     * TBOx
     * 
     * @param tBoxOwlFile T-Box OWL file
     * @param aBoxOwlFile A-Box OWL file
     */
    public MLNFormulater(String tBoxOwlFile, String aBoxOwlFile) {
        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        try {

            // load the first ontology WITH the annotations
            ontologyABOX = manager.loadOntology(IRI
                    .create("file:" + aBoxOwlFile));

            // load the first ontology WITH the annotations
            ontologyTBOX = manager.loadOntology(IRI
                    .create("file:" + tBoxOwlFile));

            // Get hold of a data factory from the manager and
            factory = manager.getOWLDataFactory();

            // set up a prefix manager to make things easier

            prefixPredicateIE = new DefaultPrefixManager(IRI.create(
                    Constants.ONTOLOGY_EXTRACTION_PREDICATE_NS).toString());

            prefixConceptIE = new DefaultPrefixManager(IRI.create(
                    Constants.ONTOLOGY_EXTRACTION_CONCEPT_NS).toString());

            prefixInstanceIE = new DefaultPrefixManager(IRI.create(
                    Constants.ONTOLOGY_EXTRACTION_INSTANCE_NS).toString());

            prefixDBPediaConcept = new DefaultPrefixManager(IRI.create(
                    Constants.DBPEDIA_CONCEPT_NS).toString());

            prefixDBPediaPredicate = new DefaultPrefixManager(IRI.create(
                    Constants.DBPEDIA_PREDICATE_NS).toString());

            prefixDBPediaInstance = new DefaultPrefixManager(IRI.create(
                    Constants.DBPEDIA_INSTANCE_NS).toString());

        } catch (OWLOntologyCreationException e) {
            logger.error(" error loading ontology file  "
                    + tBoxOwlFile + " " + aBoxOwlFile + " " + e.getMessage());
        }
    }

    /**
     * Overloaded constructor with just a single owl file. Usually the T-Box
     * 
     * @param owlFilePath
     */
    public MLNFormulater(String owlFilePath) {
        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        try {

            logger.info("Loading " + owlFilePath);

            // load the first ontology WITH the annotations
            ontologyTBOX = manager.loadOntology(IRI
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

        if (args.length != 2)
            throw (new RuntimeException("Usage : java -jar MLN.jar <owlFile> <outputEvidenceFile>"));

        try {
            new MLNFormulater(TBOX_OWL_INPUT, ABOX_OWL_INPUT).convertOWLToMLN();
            logger.info("Done writing to " + MLN_EVIDENCE_FILE);
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
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontologyTBOX);
        logger.info("loaded reasoner ");

        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);
        logger.info("loaded explaination ");

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontologyTBOX.getAxioms();
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
        String arg3 = null;

        String axiomType = null;
        String value = null;
        String[] elements = null;
        String[] arguments = null;

        // the file where the evidences for the MLN are written out
        FileWriter fw = new FileWriter(MLN_EVIDENCE_FILE);
        BufferedWriter bw = new BufferedWriter(fw);

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontologyTBOX.getAxioms();
        System.out.println("Tbox Ontology loaded: " + ontologyTBOX);

        for (OWLAxiom axiom : allAxioms) {

            /**
             * since it returns a set of classes, the order is never promised,
             * hence this weird way of getting the two arguments of the axiom
             */
            // separates the axiom and the arguments
            elements = axiom.toString().split("\\(");
            axiomType = elements[0];

            try {
                if (elements.length == 2) {
                    if (elements[1] != null) {
                        arguments = elements[1].split("\\s");

                        if (arguments.length >= 2) {
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
            } catch (Exception e) {
                System.out.println("ontologyTBox = " + elements);
                continue;
            }
        }

        // iterate over all axioms in the loaded ontology
        allAxioms = (HashSet<OWLAxiom>) ontologyABOX.getAxioms();
        System.out.println("Abox Ontology loaded: " + ontologyABOX);

        // Set<String> s = new TreeSet<String>();
        for (OWLAxiom axiom : allAxioms) {

            /**
             * since it returns a set of classes, the order is never promised,
             * hence this weird way of getting the two arguments of the axiom
             */
            // separates the axiom and the arguments
            elements = axiom.toString().trim().split("\\(<");
            axiomType = elements[0];

            try {
                if (elements.length == 2) {
                    if (elements[1] != null) {
                        arguments = elements[1].split("\\s");

                        if (arguments.length == 2
                                || (arguments.length == 3 && arguments[2].equals(")"))) {
                            arg1 = arguments[0];
                            arg2 = arguments[1];

                            value = alterSemantics(axiomType,
                                    arg1, arg2);

                            if (value != null) {
                                writeToFile(value, arg1, arg2, bw);
                            }
                        }
                        else if (arguments.length == 3) {
                            arg1 = arguments[0];
                            arg2 = arguments[1];
                            arg3 = arguments[2];

                            value = alterSemantics(axiomType,
                                    arg1, arg2);

                            if (value != null) {

                                bw.write(value + "(" + removeTags(arg1)
                                        + ", " + removeTags(arg2) + ", " + removeTags(arg3) + ")\n");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("ontologyABox = " + elements);
                continue;
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
     * @param arg3
     * @return altered semantics for the axioms
     */
    private String alterSemantics(String axiomType, String arg1, String arg2) {

        if (axiomType.toString().equals("DisjointClasses"))
            return "cdis";
        if (axiomType.toString().equals("SubObjectPropertyOf"))
            return "psub";
        if (axiomType.toString().equals("SubClassOf") && arg2.trim().indexOf("owl:Thing") == -1)
            return "csub";

        if (axiomType.toString().equals("ObjectPropertyRange"))
            return "ran";

        if (axiomType.toString().equals("ObjectPropertyDomain"))
            return "dom";

        if (axiomType.toString().equals("ClassAssertion"))
            return "isOfType";

        if (axiomType.toString().equals("SameIndividual"))
            return "sameAsConf";

        if (axiomType.toString().equals("EquivalentObjectProperties"))
            return "equivProp";

        if (axiomType.toString().equals("AnnotationAssertion") && arg1.indexOf("rdfs:label") == -1
                && arg1.indexOf("rdfs:comment") == -1)
            return "propAsst";

        if (axiomType.toString().equals("ObjectPropertyAssertion"))
            return "propAsst";

        return null;
    }

    /**
     * cleans of the "<" or ">" on the concepts
     * 
     * @param arg value to be cleaned
     * @return
     */
    private String removeTags(String arg) {

        arg = arg.replaceAll("<", "");
        arg = arg.replaceAll(">\\)", "");
        arg = arg.replaceAll(">", "");
        arg = arg.replaceAll(",_", "__");
        arg = arg.replaceAll("'", "*");
        arg = arg.replaceAll("%", "~");
        arg = arg.replaceAll("Node\\(", "");
        arg = arg.replaceAll("\\)", "]");
        arg = arg.replaceAll("\\(", "[");
        arg = arg.replaceAll("http://dbpedia.org", "DBP#");
        arg = arg.replaceAll("http://dws/", "");
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

        if (axiomType.equals("sameAsConf")) {
            bw.write(axiomType + "(" + removeTags(arg1)
                    + ", " + removeTags(arg2) + ",1.0)\n");
        }
        else
            bw.write(axiomType + "(" + removeTags(arg1)
                    + ", " + removeTags(arg2) + ")\n");

    }
}

/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;

/**
 * @author Arnab Dutta
 */
public class MLNFileGenerator {

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

    /**
     * PrefixManager instance
     */

    PrefixManager prefixPredicateIE = null;
    PrefixManager prefixConceptIE = null;
    PrefixManager prefixInstanceIE = null;

    PrefixManager prefixDBPediaConcept = null;
    PrefixManager prefixDBPediaPredicate = null;
    PrefixManager prefixDBPediaInstance = null;

    private static Map<Pair<String, String>, Double> LINK_APRIORI_MAP = new HashMap<Pair<String, String>, Double>();
    private static final String APRIORI_PROB_FILE = "/home/arnab/Work/data/NELL/ontology/sameAsAPriori.txt";

    private static final String NELL_CONFIDENCE_FILE = "/home/arnab/Work/data/NELL/ontology/NELLBaselineConf.csv";
    private static Map<String, Double> NELL_CONFIDENCE_MAP = new HashMap<String, Double>();

    /**
     * constructor with owl input file
     * 
     * @param owlFileInput
     */
    public MLNFileGenerator(String owlFileInput) {
        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        try {

            // load the first ontology WITH the annotations
            ontology = manager.loadOntology(IRI
                    .create("file:" + owlFileInput));

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
                    + owlFileInput + " " + e.getMessage());
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String owlFileInput = null;
        String outputEvidence = null;
        String axiomType = null;

        if (args.length != 3)
            throw (new RuntimeException(
                    "Usage : java -jar MLN.jar <inputFilePath> <outputFilePath> <axiomType>"));
        else {
            owlFileInput = args[0];
            outputEvidence = args[1];
            axiomType = args[2];

            System.out.println("Loading apriori confidences file...");
            loadAprioriConfidencesFile();

            System.out.println("Loading NELL confidences file...");
            loadNELLConfidencesFile();

            new MLNFileGenerator(owlFileInput).generateMLN(outputEvidence,
                    axiomType);

            System.out.println("Done writing to " + outputEvidence);
        }
    }

    private static void loadNELLConfidencesFile() throws IOException {

        String sub = null;
        String pred = null;
        String obj = null;
        double conf = 0D;

        String strLine;

        FileInputStream file = new FileInputStream(NELL_CONFIDENCE_FILE);
        BufferedReader input = new BufferedReader
                (new InputStreamReader(file));

        while ((strLine = input.readLine()) != null) {
            try {
                sub = strip(strLine.split("\t")[0]);
                pred = strip(strLine.split("\t")[1]);
                obj = strip(strLine.split("\t")[2]);
                conf = Double.valueOf(strip(strLine.split("\t")[8]));
                NELL_CONFIDENCE_MAP.put(sub + "\t" + pred + "\t" + obj, new Double(conf));
            } catch (Exception e) {
                continue;
            }
        }

        // System.out.println("Loading NELL confidence file completed..." +
        // NELL_CONFIDENCE_MAP);

    }

    /**
     * load the apriori probabilities of the uris given a surface from. Source:
     * WikiPrep
     * 
     * @throws IOException
     */
    private static void loadAprioriConfidencesFile() throws IOException {

        String sf = null;
        String uri = null;
        String conf = null;
        String strLine;

        Pair<String, String> pair = null;

        FileInputStream file = new FileInputStream(APRIORI_PROB_FILE);
        BufferedReader input = new BufferedReader
                (new InputStreamReader(file));

        while ((strLine = input.readLine()) != null) {
            sf = strip(strLine.split("\t")[0]);
            uri = strip(strLine.split("\t")[1]);
            conf = strip(strLine.split("\t")[2]);

            pair = new Pair<String, String>(sf, uri);
            LINK_APRIORI_MAP.put(pair, Double.parseDouble(conf));
        }

        System.out.println("Loading confidence file completed...");

    }

    private static String strip(String arg) {
        arg = arg.replaceAll("\"", "").replaceAll(":_", "__");
        arg = arg.substring(arg.indexOf(":") + 1, arg.length());
        return arg;
    }

    /**
     * key method to generate the MLn evidence files
     * 
     * @param outputEvidence
     * @param axiomType
     * @throws IOException
     */
    private void generateMLN(String outputEvidence, String axiomType) throws IOException {
        Double conf;

        String[] elements = null;
        String[] arguments = null;

        String arg1 = null;
        String arg2 = null;
        String arg3 = null;

        String value = null;

        Pair<String, String> pair = null;

        // the file where the evidences for the MLN are written out
        FileWriter fw = new FileWriter(outputEvidence);
        BufferedWriter bw = new BufferedWriter(fw);

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontology.getAxioms();
        System.out.println("Ontology loaded from " + ontology);

        Set<String> set = new HashSet<String>();

        for (OWLAxiom axiom : allAxioms) {
            /**
             * since it returns a set of classes, the order is never promised,
             * hence this weird way of getting the two arguments of the axiom
             */
            set.add(axiom.getAxiomType().toString());

            if (axiom.getAxiomType().toString().equals(getAxiomType(axiomType))) {

                // separates the axiom and the arguments
                elements = axiom.toString().split("\\(");

                try {
                    if (elements.length >= 2) {
                        if (elements[1] != null) {
                            arguments = elements[1].split("\\s");

                            arg1 = elements[1].split("\\s")[0];
                            arg2 = elements[1].split("\\s")[1];

                            if (arguments.length == 2) {

                                if (arg2.indexOf("owl:Thing") == -1
                                        && arg1.indexOf("http://schema.org") == -1
                                        && arg2.indexOf("http://schema.org") == -1) {

                                    writeToFile(axiomType, arg1, arg2, null, bw);
                                }

                            } else {
                                if (axiomType.equals("sameAsConf")) {
                                    pair = new Pair<String, String>(cleanse(arg2), cleanse(arg1));
                                    conf = LINK_APRIORI_MAP.get(pair);
                                    // System.out.println(pair + " " + conf);
                                    if (conf != null)
                                        writeToFile(axiomType, arg1, arg2, String.valueOf(conf), bw);
                                }
                                else {
                                    arg3 = elements[1].split("\\s")[2];

                                    if (axiomType.equals("propAsstConf")) {
                                        conf = NELL_CONFIDENCE_MAP.get(cleanse(arg2) + "\t"
                                                + cleanse(arg1) + "\t"
                                                + cleanse(arg3));

                                        bw.write(axiomType + "(" + removeTags(arg1) + ", "
                                                + removeTags(arg2) + ", "
                                                + removeTags(arg3) + ", " + conf + ")\n");
                                    } else
                                        writeToFile(axiomType, arg1, arg2, arg3, bw);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("ontologyTBox = " + elements);
                    continue;
                }
            }
        }

        System.out.println("Axioms found in this ontology...");
        for (String str : set)
            System.out.println("\t" + str);

        // close the stream
        bw.close();

    }

    /**
     * Given a axiomtype cast in into the OWL type
     * 
     * @param axiomType
     * @return
     */
    private String getAxiomType(String axiomType) {
        if (axiomType.equals("csub"))
            return "SubClassOf";
        else if (axiomType.equals("cdis"))
            return "DisjointClasses";
        else if (axiomType.equals("ran"))
            return "ObjectPropertyRange";
        else if (axiomType.equals("dom"))
            return "ObjectPropertyDomain";
        else if (axiomType.equals("psub"))
            return "SubObjectPropertyOf";
        else if (axiomType.equals("psub"))
            return "SubObjectPropertyOf";
        else if (axiomType.equals("equivProp"))
            return "EquivalentObjectProperties";
        else if (axiomType.equals("propAsst"))
            return "ObjectPropertyAssertion";
        else if (axiomType.equals("propAsstConf"))
            return "ObjectPropertyAssertion";
        else if (axiomType.equals("sameAsConf"))
            return "SameIndividual";
        else if (axiomType.equals("isOfType"))
            return "ClassAssertion";

        return axiomType;
    }

    /**
     * @param arg1
     * @param arg2
     * @param axiomType
     * @param arg3
     * @param bw
     * @throws IOException
     */
    public void writeToFile(String axiomType, String arg1, String arg2, String arg3,
            BufferedWriter bw)
            throws IOException {

        if (arg3 == null || arg3.equals(")")) {
            bw.write(axiomType + "(" + removeTags(arg1)
                    + ", " + removeTags(arg2) + ")\n");
        }
        else {
            if (isNumeric(arg3)) {
                bw.write(axiomType + "(" + removeTags(arg1) + ", " + removeTags(arg2) + ", "
                        + Double.parseDouble(arg3) + ")\n");
            }
            else {
                bw.write(axiomType + "(" + removeTags(arg1) + ", " + removeTags(arg2) + ", "
                        + removeTags(arg3) + ")\n");
            }
        }

    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
                                                // '-' and decimal.
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
        arg = arg.replaceAll("http://dbpedia.org/", "DBP#");
        arg = arg.replaceAll("http://dws/OIE", "NELL");
        return "\"" + arg.trim() + "\"";
    }

    /**
     * reformat before writing to evidence file
     * 
     * @param arg
     * @return
     */
    private String cleanse(String arg) {
        arg = arg.replaceAll("http://dbpedia.org/resource/", "");
        arg = arg.replaceAll("http://dws/OIE#Instance/", "");
        arg = arg.replaceAll("http://dws/OIE#Predicate/", "");

        arg = arg.replaceAll("<", "");
        arg = arg.replaceAll(">\\)", "");
        arg = arg.replaceAll(">", "");
        if (Character.isDigit(arg.charAt(arg.length() - 1))) {
            arg = arg.replaceAll("_[0-9]+/*\\.*[0-9]*", "");
        }
        return Utilities.utf8ToCharacter(arg);
    }
}

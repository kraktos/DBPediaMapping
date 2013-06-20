/**
 * 
 */

package de.dws.reasoner.mln;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.reasoner.GenericConverter;

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

    private static Map<Pair<String, String>, Double> SAMEAS_LINK_APRIORI_MAP = new HashMap<Pair<String, String>, Double>();
    private static final String APRIORI_PROB_FILE = "/home/arnab/Work/data/NELL/ontology/sameAsAPriori.txt";

    private static final String NELL_CONFIDENCE_FILE = Constants.INPUT_CSV_FILE;
    private static final String NELL_PRED_CONF_FILE = "/home/arnab/Work/data/NELL/ontology/NELLPredMatchesConf.csv";

    private static Map<String, Double> NELL_FACTS_CONFIDENCE_MAP = new HashMap<String, Double>();

    private static Map<String, Double> NELL_PRED_MAPPING_MAP = new HashMap<String, Double>();

    static String topK = null;

    DecimalFormat decimalFormatter = new DecimalFormat("0.00000");

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

        if (args.length < 3)
            throw (new RuntimeException(
                    "Usage : java -jar MLN.jar <inputFilePath> <outputFilePath> <axiomType>"));
        else {
            owlFileInput = args[0];
            outputEvidence = args[1];
            axiomType = args[2];

            try {
                // this comes only with sameAsConf, so may be null, then take
                // top 1 : default case
                topK = args[3];
            } catch (Exception e) {
                topK = "1";
            }
            System.out
                    .println("Loading apriori confidence values of NELL surface forms from WikiPrep data...");
            loadSameAsConfidences();
            System.out.println("Loading Completed...");

            System.out
                    .println("Loading confidence values of the NELL Triples occuring in the gold standard ...");
            loadNELLConfidences();
            System.out.println("Loading Completed...");

            System.out
                    .println("Loading confidence values of the NELL to DBPedia Predicate matchings from the annotated file ...");
            loadNELLPredConfidences();
            System.out.println("Loading Completed...");

            new MLNFileGenerator(owlFileInput).generateMLN(outputEvidence,
                    axiomType);

            System.out.println("Done writing to " + outputEvidence);
        }
    }

    private static void loadNELLPredConfidences() throws IOException {

        String nellPred = null;
        String dbpPred = null;
        double conf = 0D;

        String strLine;

        FileInputStream file = new FileInputStream(NELL_PRED_CONF_FILE);
        BufferedReader input = new BufferedReader
                (new InputStreamReader(file));

        while ((strLine = input.readLine()) != null) {
            try {
                nellPred = (strLine.split("\t")[0]);
                dbpPred = (strLine.split("\t")[1]);
                conf = Double.valueOf(strLine.split("\t")[2]);

                // if (obj.indexOf("jeffrey_eugenides") != -1)
                // System.out.println("ADDING TO MAP = \n " + sub + "\t" + pred
                // + "\t" + obj);

                // if (nellPred.indexOf("journalistwritesforpublication") != -1)
                // System.out.println("");

                NELL_PRED_MAPPING_MAP.put(
                        nellPred.trim() + "\t"
                                + dbpPred.trim().replaceAll("http://dbpedia.org/ontology/", ""),
                        new Double(conf));
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("Loading Predicate confidences completed..."
                + NELL_PRED_MAPPING_MAP.size());

    }

    private static void loadNELLConfidences() throws IOException {

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

                // if (obj.indexOf("jeffrey_eugenides") != -1)
                // System.out.println("ADDING TO MAP = \n " + sub + "\t" + pred
                // + "\t" + obj);

                NELL_FACTS_CONFIDENCE_MAP.put(sub + "\t" + pred + "\t" + obj, new Double(conf));
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("Loading NELL confidences completed...");

    }

    /**
     * load the apriori probabilities of the uris given a surface from. Source:
     * WikiPrep
     * 
     * @throws IOException
     */
    private static void loadSameAsConfidences() throws IOException {

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
            SAMEAS_LINK_APRIORI_MAP.put(pair, Double.parseDouble(conf));
        }

        System.out.println("Loading completed...");

    }

    private static String strip(String arg) {
        arg = arg.replaceAll("\"", "").replaceAll(":_", "~3A_");
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

        String formattedConf = null;

        String value = null;

        Pair<String, String> pair = null;

        // the file where the evidences for the MLN are written out
        FileWriter fw = new FileWriter(outputEvidence);
        BufferedWriter bw = new BufferedWriter(fw);

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontology.getAxioms();
        System.out.println("Ontology loaded from " + ontology);

        Set<String> set = new HashSet<String>();
        Set<OWLClass> setOWLClasses = null;

        BufferedWriter isOfTypeEvidenceWriter = new BufferedWriter(
                new FileWriter(
                        Constants.IS_OF_TYPE_DBPEDIA_EVIDENCE));

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
                    if (axiomType.equals("cdis")) {

                        setOWLClasses = axiom.getClassesInSignature();
                        createDisjointClasses(setOWLClasses, axiomType, bw);

                    } else if (elements.length >= 2) {
                        if (elements[1] != null) {
                            arguments = elements[1].split("\\s");

                            arg1 = elements[1].split("\\s")[0];
                            arg2 = elements[1].split("\\s")[1];

                            if (arguments.length == 2) { // Class Assertions
                                if (axiomType.equals("psubConf")) {

                                    // System.out.println(cleanse(arg1) + "\t"
                                    // + cleanse(arg2));
                                    conf = NELL_PRED_MAPPING_MAP.get(cleanse(arg1) + "\t"
                                            + cleanse(arg2));
                                    if (conf == null)
                                        conf = NELL_PRED_MAPPING_MAP.get(cleanse(arg2) + "\t"
                                                + cleanse(arg1));

                                    conf = (conf == null) ? 0.0 : conf;
                                    // // System.out.println(cleanse(arg1) +
                                    // "  " + cleanse(arg2) + " "
                                    // + conf);

                                    bw.write(axiomType + "(" + removeTags(arg1) + ", "
                                            + removeTags(arg2) + ", " + conf + ")\n");

                                }
                                else if (axiomType.equals("isOfTypeConf")) {

                                    System.out.println(cleanse(arg2) + "\t"
                                            + cleanse(arg1));
                                    conf = NELL_FACTS_CONFIDENCE_MAP.get(cleanse(arg2) + "\t"
                                            + "generalizations" + "\t"
                                            + cleanse(arg1));

                                    if (conf != null)
                                        bw.write(axiomType + "(" + removeTags(arg1) + ", "
                                                + removeTags(arg2) + ", " + conf + ")\n");

                                } else if (axiomType.equals("isOfType")) {

                                    conf = NELL_FACTS_CONFIDENCE_MAP.get(cleanse(arg2) + "\t"
                                            + "generalizations" + "\t"
                                            + cleanse(arg1));

                                    if (conf == null)
                                        writeToFile(axiomType, arg1, arg2, null, bw);
                                }
                                else if (arg2.indexOf("owl:Thing") == -1
                                        && arg1.indexOf("http://schema.org") == -1
                                        && arg2.indexOf("http://schema.org") == -1) {

                                    writeToFile(axiomType, arg1, arg2, null, bw);
                                }

                            } else {
                                if (axiomType.equals("sameAsConf")) {
                                    // if (arg2.indexOf("devo") != -1)
                                    // System.out.println("");

                                    // pair = new Pair<String,
                                    // String>(cleanse(arg2), cleanse(arg1));

                                    // conf = SAMEAS_LINK_APRIORI_MAP.get(pair);
                                    int cntr = 0;

                                    if (topK != null) {

                                        Map<Pair<String, String>, Double> mapPairs = getPairs(cleanse(arg2));

                                        // generateDBPediaTypeMLN(mapPairs,
                                        // isOfTypeEvidenceWriter);

                                        for (Map.Entry<Pair<String, String>, Double> en : mapPairs
                                                .entrySet()) {

                                            generateDBPediaTypeMLN(en.getKey(),
                                                    isOfTypeEvidenceWriter);

                                            if (cntr++ < Integer.parseInt(topK)) {

                                                conf = en.getValue();
                                                formattedConf = decimalFormatter
                                                        .format(conf);

                                                conf = Double.parseDouble(decimalFormatter
                                                        .format(conf));

                                                arg1 = "DBP#resource/" + en.getKey().getSecond();

                                                if (conf != null)
                                                    writeToFile(axiomType, arg1, arg2,
                                                            formattedConf, bw);
                                            }
                                        }
                                    }
                                }
                                else {
                                    arg3 = elements[1].split("\\s")[2];

                                    if (axiomType.equals("propAsstConf")) {

                                        if (arg3.indexOf("jeffrey_eugenides")
                                        != -1)
                                            System.out.println(" GETTING FROM MAP \n = "
                                                    + cleanse(arg2) + "\t"
                                                    + cleanse(arg1) + "\t"
                                                    + cleanse(arg3));

                                        conf = NELL_FACTS_CONFIDENCE_MAP.get(cleanse(arg2) + "\t"
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
                    System.out.println("ontologyTBox = " + " " + e.getMessage() + " " + axiom);
                    continue;
                }
            }
        }

        System.out.println("Axioms found in this ontology...");
        for (String str : set)
            System.out.println("\t" + str);

        // close the stream
        bw.close();
        isOfTypeEvidenceWriter.close();

    }

    private void generateDBPediaTypeMLN(Pair<String, String> pair,
            BufferedWriter isOfTypeEvidenceWriter) {
        List<String> listTypes;

        String dbPediaInstance = null;

        // for (Map.Entry<Pair<String, String>, Double> entry : pair.entrySet())
        // {

        dbPediaInstance = pair.getSecond();
        if (dbPediaInstance.indexOf("Raffael_Caetano_de_Ara") != -1)
            System.out.println("");

        // get DBPedia types
        listTypes = GenericConverter.getInstanceTypes(dbPediaInstance);

        if (listTypes.size() > 0) {
            // System.out.println(dbPediaInstance + " =>  " + listTypes);
            for (String type : listTypes) {
                try {
                    isOfTypeEvidenceWriter.write("isOfType(\"DBP#ontology/"
                            + type
                            + "\", "
                            + removeTags("DBP#resource/"
                                    + dbPediaInstance)
                            + ")\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // }

    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    static Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o2, Object o1) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * returns the top concepts of the given surface form
     * 
     * @param surfaceForm
     * @return
     */
    private Map<Pair<String, String>, Double> getPairs(String surfaceForm) {
        Map<Pair<String, String>, Double> mapPair = new HashMap<Pair<String, String>, Double>();
        for (Map.Entry<Pair<String, String>, Double> entry : SAMEAS_LINK_APRIORI_MAP.entrySet()) {
            if (entry.getKey().getFirst().equals(surfaceForm)) {
                mapPair.put(entry.getKey(), entry.getValue());
            }
        }

        mapPair = sortByValue(mapPair);
        return mapPair;
    }

    private void createDisjointClasses(Set<OWLClass> setOWLClasses, String axiomType,
            BufferedWriter bw) throws IOException {
        for (OWLClass class1 : setOWLClasses) {
            for (OWLClass class2 : setOWLClasses) {
                if (!class1.toString().equals(class2.toString())) {
                    if (class1.toString().indexOf("http://schema.org") == -1 &&
                            class2.toString().indexOf("http://schema.org") == -1 &&
                            class1.toString().indexOf("http://www.w3.org/") == -1 &&
                            class2.toString().indexOf("http://www.w3.org/") == -1) {
                        writeToFile(axiomType, class1.toString(), class2.toString(), null, bw);
                    }
                }
            }
        }
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
        else if (axiomType.equals("psubConf"))
            return "SubObjectPropertyOf";
        else if (axiomType.equals("psub"))
            return "SubObjectPropertyOf";
        else if (axiomType.equals("equivProp"))
            return "EquivalentObjectProperties";
        else if (axiomType.equals("propAsst"))
            return "ObjectPropertyAssertion";
        else if (axiomType.equals("propAsstConf"))
            return "ObjectPropertyAssertion";
        else if (axiomType.equals("sameAsConf")) {
            return "SameIndividual";
        }
        else if (axiomType.equals("isOfType"))
            return "ClassAssertion";
        else if (axiomType.equals("isOfTypeConf"))
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
                        + arg3 + ")\n");
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

        arg = arg.replaceAll("_:", "");
        arg = arg.replaceAll("<", "");
        arg = arg.replaceAll(">\\)", "");
        arg = arg.replaceAll(">", "");
        arg = arg.replaceAll(",", "~2C");
        arg = arg.replaceAll("'", "*");
        arg = arg.replaceAll("%", "~");

        arg = arg.replaceAll("~28", "[");
        arg = arg.replaceAll("~29", "]");
        arg = arg.replaceAll("~27", "*");

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
        arg = arg.replaceAll("http://dbpedia.org/ontology/", "");
        arg = arg.replaceAll("http://dws/OIE#Instance/", "");
        arg = arg.replaceAll("http://dws/OIE#Predicate/", "");
        arg = arg.replaceAll("http://dws/OIE#Concept/", "");

        arg = arg.replaceAll("<", "");
        arg = arg.replaceAll(">\\)", "");
        arg = arg.replaceAll(">", "");
        arg = arg.replaceAll("_:", "");
        arg = Utilities.utf8ToCharacter(arg);
        while (Character.isDigit(arg.charAt(arg.length() - 1))) {
            arg = arg.substring(0, arg.length() - 1);
            if (arg.charAt(arg.length() - 1) == Constants.POST_FIX.charAt(0))
                return Utilities.utf8ToCharacter(arg.substring(0, arg.length() - 1));
        }
        return Utilities.utf8ToCharacter(arg);
    }
}

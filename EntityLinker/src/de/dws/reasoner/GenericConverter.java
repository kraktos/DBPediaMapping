/**
 * 
 */

package de.dws.reasoner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import de.dws.reasoner.owl.OWLCreator;

/**
 * This class is converter class for converting specially owl/csv files into
 * markov logic networks formulae
 * 
 * @author Arnab Dutta
 */
public class GenericConverter {

    // define Logger
    static Logger logger = Logger.getLogger(GenericConverter.class.getName());

    // data structure to hold the pairs of NELL categories and relations and its
    // hierarchy
    static Map<String, List<Pair<String, String>>> NELL_CATG_RELTNS = new HashMap<String, List<Pair<String, String>>>();

    private static int cntr = 0;

    // how domain property is defined in NELL
    private static final String DOMAIN_DEFN = "domain";

    // how range is defined in NELL
    private static final String RANGE_DEFN = "range";

    // disjointness property definition
    private static final String DISJOINT_DEFN = "mutexpredicates";

    // inverse property definition
    private static final String INVERSE_DEFN = "inverse";

    // is it a concept or predicate is defined by this relation ..
    private static final String MEMBER_TYPE_DEFN = "memberofsets";

    // subsumption definition
    private static final String SUBSUMP_DEFN = "generalizations";

    public static long ENTITY_COUNTER = 0;

    public static Map<String, Long> MAP_COUNTER = new HashMap<String, Long>();

    private static final Map<String, String> URI_2_ENTITY_MAP = new HashMap<String, String>();

    public enum TYPE {
        NELL_ONTO, NELL_PRED_ANNO, NELL_CLASS_ANNO, NELL_ABOX; // ; is optional
    }

    // ..and by these four values
    // private static final String CATG_TYPE_DEFN_I = "concept:rtwcategory";
    private static final String CATG_TYPE_DEFN_II = "rtwcategory";
    // private static final String REL_TYPE_DEFN_I = "concept:rtwrelation";
    private static final String REL_TYPE_DEFN_II = "rtwrelation";

    public static final String URI_CANONICAL_FILE = "resources/uri2CanonMapping.tsv";

    private static Map<Pair<String, String>, Double> NELL_CLASS_ASSERT_MAP = new HashMap<Pair<String, String>, Double>();
    private static Set<String> uniqueEntities = new HashSet<String>();

    private static final String NELL_CLASS_ASSERTIONS_FILE = "/home/arnab/Work/data/NELL/NellClassAsserions.csv";

    /**
     * converts a csv file to owl file
     * 
     * @param inputCsvFile input file
     * @param delimit delimiter of input file
     * @param outputOwlFile
     * @param
     */
    public static void convertCsvToOwl(String inputCsvFile, String delimit, TYPE type,
            String outputOwlFile) {
        // If we are processing the baseline vs nell triple, no need to load it
        // in memory
        // since, we can readily convert the rows to a set of assertion
        // statements

        if (!type.equals(TYPE.NELL_ABOX)) {
            loadCsvInMemory(inputCsvFile, delimit);

            try {
                createOwlFile(type, outputOwlFile);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
        }
        else { // deal NELL ABox stuff separately
            System.out.println("Loading confidence values of the NELL class assertions ...");
            loadNELLClassAssertConfidences();
            System.out.println("Loading Completed...");

            try {
                readCsvAndCreateOwlFile(inputCsvFile, delimit,
                        outputOwlFile);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * creates the owl file reading the contents of the csv file.
     * 
     * @param inputCsvFile input csv file
     * @param delimit delimiter of csv file
     * @param outputOwlFile output owl file path
     * @throws OWLOntologyCreationException
     */
    private static void readCsvAndCreateOwlFile(String inputCsvFile, String delimit,
            String outputOwlFile)
            throws OWLOntologyCreationException {

        String line = null;
        BufferedReader tupleReader;
        String[] elements = null;

        // Nell specific variables
        String nellSub = null;
        String nellSubType = null;

        String nellPred = null;

        String nellObj = null;
        String nellObjType = null;

        String blSubjInst = null;
        String blObjInst = null;

        String goldSub = null;
        String goldObj = null;

        List<String> listTypes = null;

        OWLCreator owlCreator = new OWLCreator(Constants.OIE_ONTOLOGY_NAMESPACE);

        try {
            tupleReader = new BufferedReader(new FileReader(inputCsvFile));
            logger.info("Reading " + inputCsvFile + " ... Please wait !!");

            // the file where the evidences for the MLN are written out

            BufferedWriter goldEvidenceWriter = new BufferedWriter(new FileWriter(
                    Constants.GOLD_MLN_EVIDENCE));

            BufferedWriter isOfTypeEvidenceWriter = new BufferedWriter(new FileWriter(
                    Constants.IS_OF_TYPE_CONF_NELL_EVIDENCE));

            if (tupleReader != null) {
                while ((line = tupleReader.readLine()) != null) {
                    elements = line.split(delimit);
                    if (elements.length >= 8) {

                        // get the individual NELL subject, predicate and object
                        nellSub = getInst(elements[0]);
                        nellPred = elements[1];
                        nellObj = getInst(elements[2]);

                        goldSub = elements[3].trim();
                        goldObj = elements[5].trim();

                        blSubjInst = removeHeader(elements[6]);
                        blSubjInst = Utilities.characterToUTF8(blSubjInst);

                        blObjInst = removeHeader(elements[7]);
                        blObjInst = Utilities.characterToUTF8(blObjInst);

                        nellSubType = getType(elements[0]);
                        nellObjType = getType(elements[2]);

                        if (!nellSubType.equals(nellSub))
                            nellSub = generateUniqueURI(nellSub, elements[0], elements[1],
                                    elements[2]);

                        if (!nellObjType.equals(nellObj))
                            nellObj = generateUniqueURI(nellObj, elements[0], elements[1],
                                    elements[2]);

                        logger.info(nellSubType + " " + nellSub + " " +
                                nellObjType + " " + nellObj
                                + " -- " + blSubjInst +
                                " " + blObjInst);

                        // create a property assertion on the nell triple
                        if (nellPred != null && nellSub != null && nellObj != null
                                && !nellPred.equals("generalizations"))
                            owlCreator.createPropertyAssertion(nellPred, nellSub, nellObj);

                        // check for the types of the instances, if any
                        if (!nellSubType.equals(nellSub))
                            createTypeOfMLN(nellSub, isOfTypeEvidenceWriter);

                        if (!nellObjType.equals(nellObj))
                            createTypeOfMLN(nellObj, isOfTypeEvidenceWriter);

                        // get types of subject instances
                        getTypes(blSubjInst, owlCreator);

                        // type assertion of NELL instances as subjects
                        // if (!nellSubType.equals(nellSub))
                        // owlCreator.createIsTypeOf(nellSub, nellSubType);

                        // same as between NELL and DBpedia instance as
                        // subjects
                        if (!nellSubType.equals(nellSub))
                            owlCreator.createSameAs(nellSub, blSubjInst);

                        // get types of subject instances
                        getTypes(blObjInst, owlCreator);

                        // type assertion of NELL instances as objects
                        // if (!nellObjType.equals(nellObj))
                        // owlCreator.createIsTypeOf(nellObj, nellObjType);

                        // same as between NELL and DBpedia instance as
                        // objects
                        if (!nellObjType.equals(nellObj))
                            owlCreator.createSameAs(nellObj, blObjInst);

                        // Create Gold MLN
                        createGoldMLN(goldSub, nellSub, goldEvidenceWriter);
                        if (!nellPred.equals("generalizations")) {
                            createGoldMLN(goldObj, nellObj, goldEvidenceWriter);
                        }
                    }
                }

                goldEvidenceWriter.close();
                isOfTypeEvidenceWriter.close();
                
                dumpToLocalFile(GenericConverter.URI_2_ENTITY_MAP, URI_CANONICAL_FILE);

                // flush to file
                owlCreator.createOutput(outputOwlFile);
            }
        } catch (IOException e) {
            logger.info("Error processing " + line + " " + e.getMessage());
        }
    }

    private static String createTypeOfMLN(String entity, BufferedWriter isOfTypeEvidenceWriter) {

        String type;

        String tempEntity = entity.replaceAll(Constants.POST_FIX + "[0-9]", "");

        for (Map.Entry<Pair<String, String>, Double> entry : NELL_CLASS_ASSERT_MAP.entrySet()) {
            if (entry.getKey().getFirst().equals(tempEntity)) {
                type = entry.getKey().getSecond();
                try {
                    if (!uniqueEntities.contains(entity + type)) {
                        isOfTypeEvidenceWriter.write("isOfTypeConf(\"NELL#Concept/"
                                + type + "\", \"NELL#Instance/" + entity
                                + "\", " + entry.getValue() + ")\n");

                        uniqueEntities.add(entity + type);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    private static void createGoldMLN(String gold, String nell, BufferedWriter bw)
            throws IOException {

        String nellFiltered = nell.substring(nell.indexOf(":") + 1, nell.length());
        String goldFiltered = gold.replaceAll("http://dbpedia.org/resource/", "");

        // if (gold.indexOf("Carrie_") != -1)
        // System.out.println("");

        goldFiltered = Utilities.characterToUTF8(goldFiltered);
        goldFiltered = goldFiltered.replaceAll("%", "~");
        goldFiltered = goldFiltered.replaceAll("~28", "[");
        goldFiltered = goldFiltered.replaceAll("~29", "]");
        goldFiltered = goldFiltered.replaceAll("~27", "*");

        bw.write("sameAs(\"DBP#resource/" + goldFiltered + "\", \"NELL#Instance/" + nellFiltered
                + "\")\n");

    }

    /**
     * dump the uri to canonical form mappings to some local file
     * 
     * @param uri2EntityMap
     * @param path
     * @throws IOException
     */
    private static void dumpToLocalFile(Map<String, String> uri2EntityMap, String path)
            throws IOException {
        String key = null;
        String value = null;

        FileWriter fw = new FileWriter(path);
        BufferedWriter bw = new BufferedWriter(fw);

        for (Map.Entry<String, String> entry : uri2EntityMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            bw.write("OIE#Instance/" + key + "\t" + value + "\n");
        }

        // clear the counter map
        GenericConverter.MAP_COUNTER.clear();

        // close the writer stream
        bw.close();
    }

    /**
     * takes a nell/reverb instance and creates an unique URI out of it. So if
     * multiple times an entity occurs, each one will have different uris.
     * 
     * @param nellInst
     * @param classInstance
     * @param elements2
     * @param elements
     * @return
     */
    private static String generateUniqueURI(String nellInst, String sub, String pred, String obj) {
        // check if this URI is already there
        if (GenericConverter.MAP_COUNTER.containsKey(nellInst)) {
            long value = GenericConverter.MAP_COUNTER.get(nellInst);
            GenericConverter.MAP_COUNTER.put(nellInst, value + 1);

            // create an unique URI because same entity already has been
            // encountered before
            nellInst = nellInst + Constants.POST_FIX + String.valueOf(value + 1);

        } else {
            GenericConverter.MAP_COUNTER.put(nellInst, 1L);
        }

        GenericConverter.URI_2_ENTITY_MAP.put(nellInst, sub + "\t" + pred + "\t" + obj);
        return nellInst;
    }

    /**
     * @param blInst
     * @param owlCreator
     */
    public static void getTypes(String blInst, OWLCreator owlCreator) {
        List<String> listTypes;

//        if(blInst.indexOf("Pierre") != -1 )
//            System.out.println("");
        
        // get DBPedia types
        listTypes = getInstanceTypes(Utilities.utf8ToCharacter(blInst));

        if (listTypes.size() > 0) {
            // type assertion of DBPedia instances occurring
            // as subjects
            owlCreator.createIsTypeOf(blInst, listTypes);
        }
    }

    /**
     * get type of a given instance
     * 
     * @param inst instance
     * @return list of its type
     */
    public static List<String> getInstanceTypes(String inst) {
        List<String> result = new ArrayList<String>();
        String sparqlQuery = null;
        
        try {
            ResultSet results = null;
            sparqlQuery = "select ?val where{ <http://dbpedia.org/resource/" + inst
                    + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?val} ";

            // fetch the result set
            results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);
            List<QuerySolution> listResults = ResultSetFormatter.toList(results);

            for (QuerySolution querySol : listResults) {
                if (querySol.get("val").toString().indexOf(Constants.DBPEDIA_CONCEPT_NS) != -1)
                    result.add(removeHeader(querySol.get("val").toString()));
            }
        } catch (Exception e) {
            logger.info("problem with " + sparqlQuery + " " + e.getMessage());
        }
        return result;
    }

    private static String getInst(String arg) {

        if (arg.indexOf(":") != -1)
            return arg.substring(arg.indexOf(":") + 1, arg.length());
        else
            return arg;
    }

    private static String getType(String arg) {
        if (arg.indexOf(":") != -1)
            return arg.substring(0, arg.indexOf(":"));
        else
            return arg;
    }

    /**
     * creates the owl file reading the contents of the csv file loaded in
     * memory. Basically, the csv input file can be the NELL ontology or the
     * predicate mapping annotated file, so we require slightly different way of
     * parsing the files
     * 
     * @param type type of inpput file, NELL or annotated file
     * @param outputOwlFile
     * @throws OWLOntologyCreationException
     */
    private static void createOwlFile(TYPE type, String outputOwlFile)
            throws OWLOntologyCreationException {
        String key = null;
        String domain = null;
        String range = null;
        String inverse = null;

        boolean isConcept = false;

        // for NELL ontology
        List<String> listDisjClasses = null;
        List<String> supCls = null;

        // for Nell predicate matches
        List<String> subClasses = null;
        List<String> supClasses = null;
        List<String> equivClasses = null;

        OWLCreator owlCreator = new OWLCreator(Constants.OIE_ONTOLOGY_NAMESPACE);

        for (Map.Entry<String, List<Pair<String, String>>> entry : NELL_CATG_RELTNS.entrySet()) {
            // get the key
            key = entry.getKey();

            if (type.equals(TYPE.NELL_ONTO)) {
                // check if this is a concept or predicate
                isConcept = isConcept(key);
                // get its super type
                supCls = isTypeOf(key);
                // ******* FOR RELATIONS
                // **********************************************
                // only predicates will have domain range restrictions and
                // inverses
                if (!isConcept) {
                    // get domain and range
                    domain = getDomain(key);
                    range = getRange(key);

                    if (domain != null && range != null) {
                        logger.info(domain + "  " + key + "  " + range);

                        // create an ontology with these values
                        // domain range restriction
                        owlCreator.creatDomainRangeRestriction(key.replaceAll(":", "_"),
                                domain.replaceAll(":", "_"), range.replaceAll(":", "_"));
                    }

                    // get the inverse
                    inverse = getInverse(key);

                    // NO INVERSE AS OF NOW
                    // if (inverse != null) {
                    // // inverse
                    // owlCreator.createInverseRelations(key.replaceAll(":",
                    // "_"),
                    // inverse.replaceAll(":", "_"));
                    //
                    // }

                    if (supCls != null) {
                        owlCreator.createSubsumption(key.replaceAll(":", "_"),
                                supCls, 0);
                    }
                }
                // ******* FOR CONCEPTS
                // ***********************************************
                if (isConcept) {
                    listDisjClasses = getDisjointClasses(key);

                    // disjoint
                    owlCreator.createDisjointClasses(key.replaceAll(":", "_"), listDisjClasses);

                    if (supCls != null) {
                        owlCreator.createSubsumption(key.replaceAll(":", "_"),
                                supCls, 1);
                    }
                }
            }

            // ****** FOR THE NELL PREDICATES MATCHINGS
            // *****************************
            if (type.equals(TYPE.NELL_PRED_ANNO)) {
                // idea is get the set of predicates with subsumption relations
                // or equivalence relation
                subClasses = getSubClasses(key);
                if (subClasses.size() > 0)
                    logger.info(key + "  " + subClasses);

                supClasses = getSupClasses(key);
                if (supClasses.size() > 0)
                    logger.info(key + "  " + supClasses);

                equivClasses = getEquivClasses(key);
                if (equivClasses.size() > 0)
                    logger.info(key + "  " + equivClasses);

                // with these bunch of sub and super classes, create subsumption
                // relations as an owl ontology
                owlCreator.createCrossDomainSubsumption(key, supClasses, 0, 0);
                owlCreator.createCrossDomainSubsumption(key, subClasses, 1, 0);

                // add create the equivalent properties
                owlCreator.createEquivalentProperties(key, equivClasses);
            }
        }

        // flush to file
        owlCreator.createOutput(outputOwlFile);
    }

    /**
     * takes a csv file and converts to a an owl ontology
     * 
     * @param filePath path of the input file
     * @param delimiter file delimiter
     * @param dataType
     */
    private static void loadCsvInMemory(String filePath, String delimiter) {

        String line = null;
        String[] elements = null;

        Pair<String, String> pair = null;
        List<Pair<String, String>> listPairs = null;

        BufferedReader tupleReader;

        try {
            // get the reader
            tupleReader = new BufferedReader(new FileReader(filePath));

            // swipe through the file and read lines
            if (tupleReader != null) {
                while ((line = tupleReader.readLine()) != null) {
                    elements = line.split(delimiter);

                    // processing logic for NELL
                    if (elements.length == 3) {

                        // create a custom data structure with these
                        // elements
                        if (!elements[2].equals("INCORRECT")
                                && elements[0].indexOf("concept:") == -1) {
                            pair = new Pair<String, String>(elements[1], elements[2]);

                            if (NELL_CATG_RELTNS.containsKey(elements[0])) {
                                // get the list for this key
                                listPairs = NELL_CATG_RELTNS.get(elements[0]);

                                // check if the pair exists in the list,
                                // else add it
                                if (!listPairs.contains(pair)) {
                                    listPairs.add(pair);
                                }
                            } else {
                                listPairs = new ArrayList<Pair<String, String>>();
                                listPairs.add(pair);
                                NELL_CATG_RELTNS.put(elements[0], listPairs);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.info("Error processing " + line + " " + e.getMessage());
        }
    }

    // ****************** FOR NELL ONTOLOGY *****************************

    /**
     * get super concept/property of a given property
     * 
     * @param arg property
     * @return immediate super concept
     */
    private static List<String> isTypeOf(String arg) {

        List<String> retList = new ArrayList<String>();

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getFirst().equals(SUBSUMP_DEFN))
                retList.add(pair.getSecond().replaceAll(":", "_"));
        }
        return retList;
    }

    /**
     * get inverse property of a given property
     * 
     * @param arg property
     * @return inverse
     */
    private static String getInverse(String arg) {
        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getFirst().equals(INVERSE_DEFN))
                return pair.getSecond();
        }
        // if there is no inverse definition,
        return null;
    }

    /**
     * returns the range of the given predicate
     * 
     * @param arg predicate
     * @return range
     */
    public static String getRange(String arg) {

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getFirst().equals(RANGE_DEFN))
                return pair.getSecond();
        }
        // if there is no range definition, occurs for classes
        return null;
    }

    /**
     * returns the domain of the given predicate
     * 
     * @param arg predicate
     * @return domain
     */
    public static String getDomain(String arg) {

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getFirst().equals(DOMAIN_DEFN))
                return pair.getSecond();
        }
        // if there is no domain definition, occurs for classes
        return null;
    }

    /**
     * returns is it is a concept or relation
     * 
     * @param arg class or predicate
     * @return true if concept, false if predicate
     */
    public static boolean isConcept(String arg) {

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getFirst().equals(MEMBER_TYPE_DEFN)
                    &&
                    pair.getSecond().equals(
                            CATG_TYPE_DEFN_II))
                return true;
        }
        return false;
    }

    /**
     * returns list of disjoint classes/predicates
     * 
     * @param arg class or predicate
     * @return List of disjoint classes or predicates
     */
    public static List<String> getDisjointClasses(String arg) {

        List<String> retList = new ArrayList<String>();

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getFirst().equals(DISJOINT_DEFN))
                retList.add(pair.getSecond());
        }
        return retList;
    }

    // ****************** FOR NELL PREDICATE MATCHES
    // *****************************

    /**
     * get super classes(in DBPedia) of the nell property
     * 
     * @param arg nell predicate
     * @return list of super classes
     */
    private static List<String> getSubClasses(String arg) {
        List<String> retList = new ArrayList<String>();

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getSecond().equals("SUPPROP"))
                retList.add(removeHeader(pair.getFirst()));
        }
        return retList;
    }

    /**
     * get sub classes(in DBPedia) of the nell property
     * 
     * @param arg nell predicate
     * @return list of sub classes
     */
    private static List<String> getSupClasses(String arg) {
        List<String> retList = new ArrayList<String>();

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getSecond().equals("SUBPROP"))
                retList.add(removeHeader(pair.getFirst()));
        }
        return retList;
    }

    /**
     * get quivalent classes for a given class
     * 
     * @param arg provide class
     * @return equivalent class
     */
    private static List<String> getEquivClasses(String arg) {
        List<String> retList = new ArrayList<String>();

        List<Pair<String, String>> list = NELL_CATG_RELTNS.get(arg);
        for (Pair<String, String> pair : list) {
            if (pair.getSecond().equals("EQUIV"))
                retList.add(removeHeader(pair.getFirst()));
        }
        return retList;
    }

    // ***************************************************************
    /**
     * removes the DBpedia header uri information
     * 
     * @param arg
     * @return
     */
    private static String removeHeader(String arg) {
        return arg.replaceAll(Constants.DBPEDIA_PREDICATE_NS, "").replaceAll(
                Constants.DBPEDIA_INSTANCE_NS, "").replaceAll(":_", "__").replaceAll("\"", ""); // TODO
    }

    public static void print() {
        for (Map.Entry<String, List<Pair<String, String>>> entry : NELL_CATG_RELTNS.entrySet()) {
            String key = entry.getKey();
            List<Pair<String, String>> list = entry.getValue();

            for (Pair<String, String> pair : list) {
                String first = pair.getFirst();
                String secnd = pair.getSecond();

                logger.info(key + "  " + first + "  " + secnd);
            }
        }
    }

    private static void loadNELLClassAssertConfidences() {

        String strLine = null;

        String sub = null;
        String pred = null;
        String obj = null;
        double conf = 0D;
        try {
            FileInputStream file = new FileInputStream(NELL_CLASS_ASSERTIONS_FILE);
            BufferedReader input = new BufferedReader
                    (new InputStreamReader(file));

            Pair<String, String> pairEntity = null;

            while ((strLine = input.readLine()) != null) {

                sub = (strLine.split(",")[0]);
                sub = sub.substring(sub.indexOf(":") + 1, sub.length());

                pred = (strLine.split(",")[1]);
                obj = (strLine.split(",")[2]);
                conf = Double.valueOf((strLine.split(",")[3]));

                pairEntity = new Pair<String, String>(sub, obj);

                // if (NELL_CLASS_ASSERT_MAP.containsKey(pairEntity))
                // System.out.println(pairEntity + " " +
                // NELL_CLASS_ASSERT_MAP.get(pairEntity));
                // else
                NELL_CLASS_ASSERT_MAP.put(pairEntity, conf);

            }
        } catch (IOException ex) {
            System.out.println("Exception with line = " + strLine + "  " + ex.getMessage());
        }
    }
}

/**
 * 
 */

package de.dws.reasoner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Constants;
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

    private static final String NELL_ONTOLOGY_FILE = "resources/NellOntology.owl";

    // data structure to hold the pairs of NELL categories and relations and its
    // hierarchy
    static Map<String, List<Pair<String, String>>> NELL_CATG_RELTNS = new HashMap<String, List<Pair<String, String>>>();

    private static final String DOMAIN_DEFN = "domain";
    private static final String RANGE_DEFN = "range";

    private static final String DISJOINT_DEFN = "mutexpredicates";

    private static final String INVERSE_DEFN = "inverse";

    // is it a concept or predicate is defined by this relation ..
    private static final String MEMBER_TYPE_DEFN = "memberofsets";

    // ..and by these four values
    private static final String CATG_TYPE_DEFN_I = "concept:rtwcategory";
    private static final String CATG_TYPE_DEFN_II = "rtwcategory";
    private static final String REL_TYPE_DEFN_I = "concept:rtwrelation";
    private static final String REL_TYPE_DEFN_II = "rtwrelation";

    /**
     * converts a csv file to owl file
     * 
     * @param inputCsvFile input file
     * @param delimit delimiter of input file
     */
    public static void convertCsvToOwl(String inputCsvFile, String delimit) {
        loadCsvInMemory(inputCsvFile, delimit);

        try {
            createOwlFile();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates the owl file reading the contents of the csv file loaded in
     * memory
     * 
     * @throws OWLOntologyCreationException
     */
    private static void createOwlFile() throws OWLOntologyCreationException {
        String key = null;
        String domain = null;
        String range = null;
        String inverse = null;

        boolean isConcept = false;

        List<String> listDisjClasses = null;

        OWLCreator owlCreator = new OWLCreator(Constants.OIE_ONTOLOGY_NAMESPACE);

        for (Map.Entry<String, List<Pair<String, String>>> entry : NELL_CATG_RELTNS.entrySet()) {
            key = entry.getKey();
            inverse = getInverse(key);

            isConcept = isConcept(key);

            if (!isConcept) {
                domain = getDomain(key);
                range = getRange(key);
                if (domain != null && range != null) {
                    logger.info(domain + "  " + key + "  " + range);

                    // create an ontology with these values
                    // domain range restriction
                    owlCreator.creatDomainRangeRestriction(key.replaceAll(":", "_"),
                            domain.replaceAll(":", "_"), range.replaceAll(":", "_"));
                }

                if (inverse != null) {
                    // inverse
                    owlCreator.createInverseRelations(key.replaceAll(":", "_"),
                            inverse.replaceAll(":", "_"));

                }
            }

            if (isConcept) {
                listDisjClasses = getDisjointClasses(key);
                
                //disjoint
                owlCreator.createDisjointClasses(key.replaceAll(":", "_"), listDisjClasses);
            }

        }

        // flush to file
        owlCreator.createOutput(NELL_ONTOLOGY_FILE);
    }

    /**
     * takes a csv file and converts to a an owl ontology
     * 
     * @param filePath path of the input file
     * @param delimiter file delimiter
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

                    if (elements.length == 3) {
                        // logger.info(elements[0] + "=== " + elements[1] +
                        // "  ===  " + elements[2]);

                        // create a custom data structure with these elements
                        pair = new Pair<String, String>(elements[1], elements[2]);

                        if (NELL_CATG_RELTNS.containsKey(elements[0])) {
                            // get the list for this key
                            listPairs = NELL_CATG_RELTNS.get(elements[0]);

                            // check if the pair exists in the list, else add it
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
        } catch (IOException e) {
            logger.info("Error processing " + line + " " + e.getMessage());
        }
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
                    (pair.getSecond().equals(CATG_TYPE_DEFN_I) || pair.getSecond().equals(
                            CATG_TYPE_DEFN_II)))
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

}

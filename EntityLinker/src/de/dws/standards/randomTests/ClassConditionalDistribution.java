/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.dws.helper.dataObject.Pair;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.mapper.engine.query.SPARQLEndPointQueryAPI;

/**
 * Class responsible for finding the conditional distribution of a DBPedia Class
 * given a NEll clas type. Only Nell, has theschema information, ReVerb don't.
 * 
 * @author Arnab Dutta
 */
public class ClassConditionalDistribution {

    // define Logger
    static Logger logger = Logger.getLogger(ClassConditionalDistribution.class.getName());

    // set of nell subjects and objects and dbpedia URIs they point to
    private static final String ALL_CANON_ENTITIES = "select E_SUB as a, D_SUB as b from goldStandardClean union select E_OBJ as a, D_OBJ as b from goldStandardClean";

    // a DBPedia URI, Nell concept pair with a count of their occurrence
    // frequency
    private static final Map<Pair<String, String>, Long> PAIR_DBPURI_NELLCONCPT = new HashMap<Pair<String, String>, Long>();

    // a DBPedia class, Nell Class pair with a count of their occurrence
    // frequency
    private static Map<Pair<String, String>, Long> PAIR_DBPCLASS_NELLCLASS = new HashMap<Pair<String, String>, Long>();

    // Nell Class pair with a count of their occurrence
    // frequency
    private static Map<String, Long> NELL_CLASS = new HashMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {

        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        // load the pairs of nell concepts and DBP uris
        loadCanonicalFormToConcept();

        // iterate the collection and find the type of the URIs
        fetchDBPediaTypes();

        // iterate over the pair wise frequency collection to find the
        // conditional prob.
        computeConditionalProb();

    }

    /**
     * finds the conditional probability od DBPedia class given NELL Class
     */
    private static void computeConditionalProb() {
        for (Map.Entry<Pair<String, String>, Long> entry : PAIR_DBPCLASS_NELLCLASS.entrySet()) {
            Pair<String, String> pair = entry.getKey();
            long count = entry.getValue();

            String ieClass = pair.getSecond();
            long ieClassCount = NELL_CLASS.get(ieClass);

            double condProb = (double) count / (double) ieClassCount;

            // logger.info(ieClass + "  " + ieClassCount );
            logger.info(pair.getFirst() + "," + ieClass + "," + condProb + "");
        }
    }

    /**
     * load the pairs of NELL concepts and DBP uris
     */
    private static void loadCanonicalFormToConcept() {

        DBWrapper.init(ALL_CANON_ENTITIES);
        DBWrapper.getCanonVsUriPairs(PAIR_DBPURI_NELLCONCPT);

        System.out.println(PAIR_DBPURI_NELLCONCPT.size());
    }

    /**
     * iterate the collection and find the type of the URIs
     */
    private static void fetchDBPediaTypes() {
        String canonicalForm = null;
        String uri = null;
        long occCount = 0;

        ResultSet results = null;
        List<QuerySolution> listResults = null;
        String dbPediaClass = null;

        String sparql = null;

        Pair<String, String> pair = null;
        long val = 0;

        for (Map.Entry<Pair<String, String>, Long> entry : PAIR_DBPURI_NELLCONCPT.entrySet()) {

            /*
             * if (c++ == 100) break;
             */
            canonicalForm = getIEClass(entry.getKey().getFirst());
            uri = entry.getKey().getSecond();
            uri = clean(uri);
            occCount = entry.getValue();

            sparql = "select distinct ?val where {<" +
                    uri +
                    "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?val}";
            try {
                // find the type of this entity
                results = SPARQLEndPointQueryAPI
                        .queryDBPediaEndPoint(sparql);

                listResults = ResultSetFormatter.toList(results);

                // for a possible entity there can be multiple types, Person,
                // writer, agent etc
                for (QuerySolution querySol : listResults) {
                    dbPediaClass = querySol.get("val").toString();

                    if (dbPediaClass.startsWith("http://dbpedia.org/ontology/")) {

                        pair = new Pair<String, String>(dbPediaClass,
                                canonicalForm);

                        if (PAIR_DBPCLASS_NELLCLASS.containsKey(pair)) {
                            val = PAIR_DBPCLASS_NELLCLASS.get(pair) + occCount;
                            PAIR_DBPCLASS_NELLCLASS.put(pair, val);
                        } else {
                            PAIR_DBPCLASS_NELLCLASS.put(pair, occCount);
                        }

                        if (NELL_CLASS.containsKey(canonicalForm)) {
                            val = NELL_CLASS.get(canonicalForm) + occCount;
                            NELL_CLASS.put(canonicalForm, Long.valueOf(val));
                        } else {
                            NELL_CLASS.put(canonicalForm, occCount);
                        }

                    }
                }

            } catch (Exception e) {
                System.out.println(sparql);
                continue;
            }
        }

    }

    /**
     * helper function to eliminate special characters from DBPdepia entity to
     * make SPARQL query possible
     * 
     * @param arg DBPedia uri
     * @return converted uri to UTF-8
     */
    private static String clean(String arg) {
        arg = arg.replace("http://dbpedia.org/resource/", "").trim();
        arg = Utilities.characterToUTF8(arg);
        arg = "http://dbpedia.org/resource/" + arg;
        return arg;
    }

    /**
     * get the class information from the concept name. Usually preceeding the
     * name of the entity with a ":" delimiter, E.g. city:london
     * 
     * @param key concept name
     * @return
     */
    private static String getIEClass(String key) {
        if (key.indexOf(":") != -1)
            return key.substring(0, key.indexOf(":"));
        else
            return key;
    }

}

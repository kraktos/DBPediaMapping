/**
 * 
 */

package de.dws.reasoner.inference;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;

import de.dws.mapper.helper.dataObject.ResultDAO;
import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.helper.util.Utilities;
import de.dws.reasoner.axioms.Axiom;
import de.dws.reasoner.axioms.AxiomCreator;
import de.elog.Application;

/**
 * This class reads the owl file generated after reasoning to find the all
 * inferencing tasks. It can give the MAP state and as well as the a-posteriori
 * probability of the possible triples
 * 
 * @author Arnab Dutta
 */
public class Inference {

    /**
     * logger
     */
    public static Logger logger = Logger.getLogger(Inference.class.getName());

    /**
     * OWLOntologyManager instance
     */
    OWLOntologyManager manager = null;

    /**
     * OWLOntology instance
     */
    OWLOntology ontology = null;

    /**
     * OWLDataFactory instance
     */
    static OWLDataFactory factory = null;

    /**
     * PrefixManager instance
     */
    PrefixManager prefixDBPedia = null;

    static PrefixManager prefixIE = null;

    Map<Double, Set<OWLEntity>> map = new TreeMap<Double, Set<OWLEntity>>(new Comparator<Double>()
    {
        public int compare(Double first, Double second)
        {
            return second.compareTo(first);
        }
    });

    /**
     * 
     */
    public Inference() {
        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        try {
            // load the first ontology WITH the annotations
            ontology = manager.loadOntology(IRI
                    .create("file:" + Constants.OWLFILE_CREATED_FROM_ELOG_REASONER_OUTPUT_PATH));

            // Get hold of a data factory from the manager and
            factory = manager.getOWLDataFactory();

            // set up a prefix manager to make things easier
            prefixDBPedia = new DefaultPrefixManager(IRI.create(Constants.ONTOLOGY_DBP_NS)
                    .toString());
            prefixIE = new DefaultPrefixManager(IRI.create(Constants.ONTOLOGY_EXTRACTION_NS)
                    .toString());

        } catch (OWLOntologyCreationException e) {
            logger.error(" error loading ontology file  "
                    + Constants.OWLFILE_CREATED_FROM_ELOG_REASONER_OUTPUT_PATH);
        }

    }

    private static String getARandomTriple() throws FileNotFoundException {
        File f = new File("/home/arnab/Work/data/NELL/writerwasbornincity.csv");
        String result = null;
        Random rand = new Random();
        int n = 0;
        for (Scanner sc = new Scanner(f); sc.hasNext();)
        {
            ++n;
            String line = sc.nextLine();
            if (rand.nextInt(n) == 0)
                result = line;
        }

        logger.info(result);
        return result;

    }

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] argsa) throws FileNotFoundException {

        // **************** reason with Elog
        // **********************************************************
        String[] args = new String[4];
        args[0] = "-sm";
        args[1] = "-s1000000";
        args[2] = "-i40";
        args[3] = "/home/arnab/Workspaces/SchemaMapping/EntityLinker/data/ontology/output/assertions.owl";
        
        logger.info(" \nSTARTING ELOG REASONER ... ");
        try {
            Application.main(args);
        } catch (Exception e) { 
            logger.error("exception while reasoning with ELOG");
        }

        
        logger.info(" STARTING INFERENCE BASED ON SAMPLED PROBABILITIES ... ");
        findRanking(argsa[0], argsa[1], argsa[2]);
    }

    /**
     * @param prop
     * @param obj
     * @param subj
     */
    public static void findRanking(String subj, String prop, String obj) {

        double sumProb = 0;

        Map<Double, Set<OWLEntity>> mapSubjects = new
                Inference().getRankedMatches(factory.getOWLNamedIndividual(
                        subj, prefixIE));
        Map<Double, Set<OWLEntity>> mapObjects = new
                Inference().getRankedMatches(factory.getOWLNamedIndividual(
                        obj, prefixIE));

        Map<Double, Set<OWLEntity>> mapProps = new Inference().getRankedMatches(factory
                .getOWLNamedIndividual(
                        prop, prefixIE));

        Map<Double, List<Set<OWLEntity>>> rankedMap = new Inference().getRankedTriples(mapSubjects,
                mapProps, mapObjects);

        logger.info("\n Ranked triples ..\n");

        // need to normaliz the probabilities
        // fetch the sum of all the probabilities
        for (Entry<Double, List<Set<OWLEntity>>> entry : rankedMap.entrySet()) {
            sumProb = entry.getKey() + sumProb;
        }

        // logger.info(sumProb);
        for (Entry<Double, List<Set<OWLEntity>>> entry : rankedMap.entrySet()) {
            // logger.info(entry.getKey() + " "+ sumProb + "  " + entry.getKey()
            // / sumProb);
            logger.info(entry.getKey() / sumProb + " = " + entry.getValue());
        }
    }

    /**
     * ranks the triples using joint probability
     * 
     * @param mapSubjects
     * @param mapProps
     * @param mapObjects
     * @return
     */
    private Map<Double, List<Set<OWLEntity>>> getRankedTriples(
            Map<Double, Set<OWLEntity>> mapSubjects,
            Map<Double, Set<OWLEntity>> mapProps, Map<Double, Set<OWLEntity>> mapObjects) {

        List<Set<OWLEntity>> listTriples = null;

        Map<Double, List<Set<OWLEntity>>> map = new TreeMap<Double, List<Set<OWLEntity>>>(
                new Comparator<Double>()
                {
                    public int compare(Double first, Double second)
                    {
                        return second.compareTo(first);
                    }
                });

        // iterate subject candidates
        for (Entry<Double, Set<OWLEntity>> entrySubjs : mapSubjects.entrySet()) {
            Set<OWLEntity> sub = entrySubjs.getValue();
            double subConf = entrySubjs.getKey();

            // iterate properties candidates
            for (Entry<Double, Set<OWLEntity>> entryProps : mapProps.entrySet()) {
                Set<OWLEntity> pred = entryProps.getValue();
                double predConf = entryProps.getKey();

                // iterate objects candidates
                for (Entry<Double, Set<OWLEntity>> entryObjs : mapObjects.entrySet()) {
                    Set<OWLEntity> obj = entryObjs.getValue();
                    double objConf = entryObjs.getKey();

                    double jointProbability = subConf * predConf * objConf;

                    // can have some custom data structure.. TODO
                    listTriples = new ArrayList<Set<OWLEntity>>();
                    listTriples.add(sub);
                    listTriples.add(pred);
                    listTriples.add(obj);

                    map.put(jointProbability, listTriples);
                }
            }
        }
        return map;
    }

    /**
     * get ranked candidate matches
     * 
     * @param termQuery
     * @return Map<Double, OWLAxiom> of axioms sorted by the probabilities
     */
    private Map<Double, Set<OWLEntity>> getRankedMatches(OWLEntity termQuery) {

        Set<OWLEntity> setEntity = null;
        Set<OWLEntity> tempSetEntity = null;

        // iterate over all axioms in the loaded ontology
        HashSet<OWLAxiom> allAxioms = (HashSet<OWLAxiom>) ontology.getAxioms();
        for (OWLAxiom axiom : allAxioms) {

            // take the same as links and same properties
            if (axiom.getAxiomType() == AxiomType.EQUIVALENT_OBJECT_PROPERTIES
                    || axiom.getAxiomType() == AxiomType.SAME_INDIVIDUAL) {

                // set of owlEntity
                setEntity = axiom.getSignature();
                tempSetEntity = setEntity;

                // iterate and extract the named individuals
                for (OWLEntity entity : setEntity) {

                    // remove the entries in the set which are not the named
                    // individuals
                    if (!entity.getIRI().toString().startsWith(Constants.ONTOLOGY_NAMESPACE)) {
                        tempSetEntity.remove(entity);
                    }
                    // if it is the matching individual, then remove it too and
                    // add the rest to the map.
                    // basically you end up adding the matching candidate
                    if (entity.toString().equals(termQuery.toString())) {
                        tempSetEntity.remove(entity);
                        map.put(getConfidenceValue(axiom), tempSetEntity);
                    }
                }
            }
        }

        // display the top ranked mathces for this term.
        for (Entry<Double, Set<OWLEntity>> entry : map.entrySet()) {
            logger.debug(entry.getKey() + " = " + entry.getValue());
        }

        return map;
    }

    /**
     * returns the confidence by extracting it out of the axiom
     * 
     * @param axiom OWLAxiom instance
     * @return confidence value
     */
    private Double getConfidenceValue(OWLAxiom axiom)
    {
        for (OWLAnnotation annotation : axiom.getAnnotations()) {
            OWLAnnotationValue annValue = annotation.getValue();

            OWLLiteral literalValue = (OWLLiteral) annValue;
            if (literalValue.isDouble()) {
                return literalValue.parseDouble();
            }
        }
        return null;
    }

}

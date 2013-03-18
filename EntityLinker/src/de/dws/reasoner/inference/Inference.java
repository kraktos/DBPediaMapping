/**
 * 
 */

package de.dws.reasoner.inference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    // Ontology namespace
    private static String ontologyNS = "http://www.semanticweb.org/ontologies/FactOntology/";

    // DBPedia namespace
    private static String DBPediaNS = "http://www.semanticweb.org/ontologies/FactOntology/Dbp#";

    // extraction engine namespace
    private static String extractionEngineNS = "http://www.semanticweb.org/ontologies/FactOntology/Extract#";

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
            prefixDBPedia = new DefaultPrefixManager(IRI.create(DBPediaNS).toString());
            prefixIE = new DefaultPrefixManager(IRI.create(extractionEngineNS).toString());

        } catch (OWLOntologyCreationException e) {
            logger.error(" error loading ontology file  "
                    + Constants.OWLFILE_CREATED_FROM_ELOG_REASONER_OUTPUT_PATH);
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        Map<Double, Set<OWLEntity>> mapSubjects = new
                Inference().getRankedMatches(factory.getOWLNamedIndividual(
                        "churchill", prefixIE));
        Map<Double, Set<OWLEntity>> mapObjects = new
                Inference().getRankedMatches(factory.getOWLNamedIndividual(
                        "einstein", prefixIE));

        Map<Double, Set<OWLEntity>> mapProps = new Inference().getRankedMatches(factory
                .getOWLNamedIndividual(
                        "spouse", prefixIE));

        Map<Double, List<Set<OWLEntity>>> m = new Inference().getRankedTriples(mapSubjects,
                mapProps, mapObjects);

        logger.info("\n Ranked triples ..\n");
        for (Entry<Double, List<Set<OWLEntity>>> entry : m.entrySet()) {
            logger.info(entry.getKey() + " = " + entry.getValue());
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
                    if (!entity.getIRI().toString().startsWith(ontologyNS)) {
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

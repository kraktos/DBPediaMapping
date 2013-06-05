/**
 * 
 */

package de.dws.reasoner.owl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.reasoner.axioms.Axiom;

/**
 * @author Arnab Dutta
 */
public class OWLCreator {

    /**
     * logger
     */
    public Logger logger = Logger.getLogger(OWLCreator.class.getName());

    /**
     * OWLOntologyManager instance
     */
    OWLOntologyManager manager = null;

    /**
     * OWLOntology instance
     */
    OWLOntology ontology = null;

    /**
     * IRI instance
     */
    IRI ontologyIRI = null;

    /**
     * OWLDataFactory instance
     */
    OWLDataFactory factory = null;

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
     * a list of Axioms, essentially these are weighted and hence soft
     * constraints
     */
    List<Axiom> listAxioms = new ArrayList<Axiom>();

    /**
     * @throws OWLOntologyCreationException
     */
    public OWLCreator(String nameSpace) throws OWLOntologyCreationException {

        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        // create Iri
        ontologyIRI = IRI.create(nameSpace);

        // create ontology
        ontology = manager.createOntology(ontologyIRI);

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
    }

    /**
     * @return the ontology
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * takes two instances and creates a same as link between them
     * 
     * @param nellInst nell instance
     * @param blInst dbpedia instance
     */
    public void createSameAs(String nellInst, String blInst) {
        OWLNamedIndividual arg1Value = factory.getOWLNamedIndividual(
                nellInst, prefixInstanceIE);

        OWLNamedIndividual arg2Value = factory.getOWLNamedIndividual(
                blInst, prefixDBPediaInstance);

        // create a same as link between subjects
        OWLSameIndividualAxiom sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(
                arg1Value, arg2Value);

        // add it to list of soft constraints with some weight
        manager.addAxiom(ontology, sameAsIndividualAxiom);

    }

    /**
     * creates a class assertion given an instance and its type
     * 
     * @param inst
     * @param classType
     */
    public void createIsTypeOf(String inst, String classType) {

        OWLClass type = factory.getOWLClass(classType, prefixConceptIE);
        OWLNamedIndividual instance = factory.getOWLNamedIndividual(inst, prefixInstanceIE);

        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(
                type, instance);

        // add to the manager as hard constraints
        manager.addAxiom(ontology, classAssertion);
    }

    /**
     * creates a class assertion given an instance and its list of types
     * 
     * @param inst instance of a class
     * @param classType types of the instance
     */
    public void createIsTypeOf(String inst, List<String> classType) {
        OWLClass type = null;
        OWLClassAssertionAxiom classAssertion = null;
        OWLNamedIndividual instance = factory.getOWLNamedIndividual(inst, prefixDBPediaInstance);

        for (String instType : classType) {
            type = factory.getOWLClass(instType, prefixDBPediaConcept);

            classAssertion = factory.getOWLClassAssertionAxiom(
                    type, instance);

            // add to the manager as hard constraints
            manager.addAxiom(ontology, classAssertion);
        }

    }

    /**
     * disjoint classes creation
     * 
     * @param key
     * @param listDisjClasses
     */
    public void createDisjointClasses(String key, List<String> listDisjClasses) {
        OWLDisjointClassesAxiom disjointClassesAxiom = null;
        OWLClass disClass = null;

        OWLClass ieProperty = factory.getOWLClass(key, prefixConceptIE);

        for (String cls : listDisjClasses) {
            disClass = factory.getOWLClass(cls.replaceAll(":", "_"), prefixConceptIE);

            disjointClassesAxiom = factory.getOWLDisjointClassesAxiom(
                    ieProperty, disClass);

            // add to the manager as hard constraints
            manager.addAxiom(ontology, disjointClassesAxiom);
        }
    }

    /**
     * create inverse relation on a predicate
     * 
     * @param predicate
     * @param inverse
     */
    public void createInverseRelations(String predicate, String inverse) {
        OWLObjectProperty ieProperty = factory.getOWLObjectProperty(
                predicate, prefixPredicateIE);

        OWLObjectProperty ieInverseProperty = factory.getOWLObjectProperty(
                inverse, prefixPredicateIE);

        OWLInverseObjectPropertiesAxiom inverseAxiom = factory.getOWLInverseObjectPropertiesAxiom(
                ieProperty, ieInverseProperty);

        // add to the manager as hard constraints
        manager.addAxiom(ontology, inverseAxiom);
    }

    public void createEquivalentProperties(String key, List<String> equivClasses) {
        OWLObjectProperty arg1Prop = null;
        OWLObjectProperty arg2Prop = null;
        OWLEquivalentObjectPropertiesAxiom equivPropAxiom = null;

        arg1Prop = factory.getOWLObjectProperty(key, prefixPredicateIE);
        for (String clss : equivClasses) {
            arg2Prop = factory.getOWLObjectProperty(clss, prefixDBPediaPredicate);
            equivPropAxiom = factory.getOWLEquivalentObjectPropertiesAxiom(arg1Prop, arg2Prop);
            manager.addAxiom(ontology, equivPropAxiom);
        }

    }

    /**
     * creates a subsumption relation across NELL and DBPedia
     * 
     * @param key NELL predicate
     * @param classes list of classes(super or sub)
     * @param identifier denotes if first argument is specific or 2nd argument.
     *            0: key subsumes list. 1: list subsumes key
     */
    public void createCrossDomainSubsumption(String key, List<String> classes, int identifier,
            int isClass) {

        OWLObjectProperty subsumpPropCls = null;
        OWLObjectProperty propCls = null;
        OWLSubObjectPropertyOfAxiom subPropAxiom = null;

        // predicate subsumptions
        if (isClass == 0) {
            // key subsumes list of super classes
            for (String supClass : classes) {
                subsumpPropCls = factory.getOWLObjectProperty(key, prefixPredicateIE);
                propCls = factory.getOWLObjectProperty(supClass, prefixDBPediaPredicate);

                if (identifier == 0)
                    subPropAxiom = factory.getOWLSubObjectPropertyOfAxiom(subsumpPropCls, propCls);

                if (identifier == 1)
                    subPropAxiom = factory.getOWLSubObjectPropertyOfAxiom(propCls, subsumpPropCls);

                // add to the manager as hard constraints
                manager.addAxiom(ontology, subPropAxiom);

                // add it to list of soft constraints with high probability
                // listAxioms.add(new Axiom(subPropAxiom,
                // Utilities.convertProbabilityToWeight(1.0)));
            }
        }
    }

    /**
     * creates a subsumption axiom between two classes in NELL ontology itself
     * 
     * @param subsumes child class
     * @param supCls parent class
     * @param isClass is a predicate or a concept
     */
    public void createSubsumption(String subsumes, List<String> supCls, int isClass) {

        OWLClass subsumpCls = null;
        OWLClass cls = null;
        OWLSubClassOfAxiom subClassAxiom = null;

        OWLObjectProperty subsumpPropCls = null;
        OWLObjectProperty propCls = null;
        OWLSubObjectPropertyOfAxiom subPropAxiom = null;

        if (isClass == 1) {
            for (String supClass : supCls) {
                subsumpCls = factory.getOWLClass(subsumes, prefixConceptIE);
                cls = factory.getOWLClass(supClass, prefixConceptIE);

                subClassAxiom = factory.getOWLSubClassOfAxiom(subsumpCls, cls);

                // add to the manager as hard constraints
                manager.addAxiom(ontology, subClassAxiom);
            }
        }
        if (isClass == 0) {
            for (String supClass : supCls) {
                subsumpPropCls = factory.getOWLObjectProperty(subsumes, prefixPredicateIE);
                propCls = factory.getOWLObjectProperty(supClass, prefixPredicateIE);

                subPropAxiom = factory.getOWLSubObjectPropertyOfAxiom(subsumpPropCls, propCls);

                // add to the manager as hard constraints
                manager.addAxiom(ontology, subPropAxiom);
            }
        }
    }

    /**
     * create domain range restriction on a property
     * 
     * @param predicate
     * @param domain
     * @param range
     */
    public void creatDomainRangeRestriction(String predicate, String domain, String range) {

        OWLObjectProperty ieProperty = factory.getOWLObjectProperty(
                predicate, prefixPredicateIE);

        // also add domain range restriction on the property
        OWLClass domainCls = factory.getOWLClass(IRI
                .create(Constants.ONTOLOGY_EXTRACTION_CONCEPT_NS
                        + domain));
        OWLClass rangeCls = factory.getOWLClass(IRI
                .create(Constants.ONTOLOGY_EXTRACTION_CONCEPT_NS + range));

        OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(
                ieProperty,
                domainCls);
        OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(ieProperty,
                rangeCls);

        // add to the manager as hard constraints
        manager.addAxiom(ontology, domainAxiom);
        manager.addAxiom(ontology, rangeAxiom);
    }

    /**
     * writes the ontology to a file
     * 
     * @param path
     */
    public void createOutput(String path)
    {
        // Dump the ontology to a file
        File file = new File(path);
        try {
            manager.saveOntology(getOntology(), IRI.create(file.toURI()));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } finally {
            logger.info("Axiom file created at : "
                    + path);
        }
    }

    /*public static void merge(String inputOwlFile1, String inputOwlFile2, String mergedOutputOwlFile)
            throws OWLOntologyCreationException {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(inputOwlFile1));

        OWLOntology ontology2 =
                man.loadOntologyFromOntologyDocument(new File(
                        inputOwlFile2));

        // Create our ontology merger
        OWLOntologyMerger merger = new OWLOntologyMerger(man);

        System.out.println("Loaded onto 2");

        IRI mergedOntologyIRI = IRI.create("http://www.semanticweb.com/mymergedont");
        OWLOntology merged = merger.createMergedOntology(man, mergedOntologyIRI);

        System.out.println("Loaded and merged");

    }*/

}

/**
 * 
 */
package de.dws.reasoner.axioms;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import de.dws.mapper.helper.dataObject.ExtractionEngineFactDao;
import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;

/**
 * This class serves as a point of converting a given set of facts into .owl file. This step is important since, we want
 * to apply a reasoner on top of these axioms. We use OWL API for the purpose. A detailed documentation for the API can
 * be found at {@link http://owlapi.sourceforge.net/documentation.html}. This allows to create asioms with weights(soft
 * constraints) and also unweighted (hard constraints)
 * 
 * @author Arnab Dutta
 */
public class AxiomCreator
{

    /**
     * logger
     */
    public Logger logger = Logger.getLogger(AxiomCreator.class.getName());

    // Some namespace
    private static String namespace = "http://www.semanticweb.org/ontologies/FactOntology/";

    OWLOntologyManager manager = null;

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
    PrefixManager prefix = null;

    /**
     * a list of Axioms, essentially these are weighted and hence soft constraints
     */
    List<Axiom> listAxioms = new ArrayList<Axiom>();

    /**
     * 
     */
    public AxiomCreator()
    {
        manager = OWLManager.createOWLOntologyManager();

        // create Iri
        ontologyIRI = IRI.create(namespace);

        // Get hold of a data factory from the manager and
        factory = manager.getOWLDataFactory();

        // set up a prefix manager to make things easier
        prefix = new DefaultPrefixManager(ontologyIRI.toString());
    }

    /**
     * method takes a Set of {@link SuggestedFactDAO} and also the actual fact from Extraction engine and creates all
     * possible axioms out of those Object assertions, sameAs axioms etc
     * 
     * @param dbPediaFacts Set of {@link SuggestedFactDAO}
     * @param ieFactDAo fact from Extraction engine
     * @throws OWLOntologyCreationException
     */
    public void createOwlFromFacts(Set<SuggestedFactDAO> dbPediaFacts, ExtractionEngineFactDao ieFactDAo)
        throws OWLOntologyCreationException
    {
        // create an ontology
        OWLOntology ontology = manager.createOntology(ontologyIRI);

        // iterate for each suggested fact, create assertions
        // for (SuggestedFactDAO suggesfaFactDAO : dbPediaFacts) {

        SuggestedFactDAO fact = new SuggestedFactDAO("DBPEinstein", "DBPspouse", "DBPMileva", 5D, true);

        SuggestedFactDAO nellFact = new SuggestedFactDAO("NellEinstein", "Nellspouse", "NellMileva", 5D, true);

        // TODO : works without TBOx info, sampler more concerned with inconsistency rather than incoherency
        // createTBOxAxioms(ontology);

        // creates all assertions from the matched DBPedia fact
        createFactAssertion(fact, ontology, fact.getConfidence());

        // creates all assertions from the matched IE fact
        createFactAssertion(nellFact, ontology, nellFact.getConfidence());

        // creates all individual fact assertions by saying subjects from two facts are same
        createFactSameAsAssertions(fact, nellFact, ontology);

        annotateAxioms(ontology);
        // }

        // output to a file
        createOutput(manager, ontology);
    }

    /**
     * this method annotates the axioms with the weights.
     * 
     * @param ontology {@link OWLOntology} instance
     */
    private void annotateAxioms(OWLOntology ontology)
    {
        // holds a set of annotations
        HashSet<OWLAnnotation> annotationSet = new HashSet<OWLAnnotation>();

        // the annotation property we will use for the fact confidences
        OWLAnnotationProperty annotationProbability =
            factory.getOWLAnnotationProperty(IRI.create(Constants.CONFIDENCE_VALUE_DEFINITION));

        for (Axiom axiom : listAxioms) {

            // create an annotation
            OWLAnnotation owlAnnotation =
                factory.getOWLAnnotation(annotationProbability, factory.getOWLLiteral(axiom.getConfidence()));

            // add them to a set
            annotationSet.add(owlAnnotation);

            // get the owl axiom from the set
            OWLAxiom annotatedAxiom = axiom.getAxiom().getAnnotatedAxiom(annotationSet);

            // add to the manager
            manager.addAxiom(ontology, annotatedAxiom);

            // clear the set
            annotationSet.clear();
        }
    }

    private void createTBOxAxioms(OWLOntology ontology)
    {
        OWLClass subClass = factory.getOWLClass(IRI.create(ontologyIRI + "#SuperSubject"));
        OWLClass objClass = factory.getOWLClass(IRI.create(ontologyIRI + "#SuperObject"));

        OWLDisjointClassesAxiom disjointClassesAxiom = factory.getOWLDisjointClassesAxiom(subClass, objClass);
        manager.addAxiom(ontology, disjointClassesAxiom);

    }

    /**
     * create same as links between two facts (one from IE the other from DBPedia) for each individual subjects, pr
     * 
     * @param fact
     * @param nellFact
     * @param ontology
     */
    private void createFactSameAsAssertions(SuggestedFactDAO fact, SuggestedFactDAO nellFact, OWLOntology ontology)
    {
        // fetch the individual subjects
        OWLNamedIndividual dbSubj = factory.getOWLNamedIndividual(fact.getSubject(), prefix);
        OWLNamedIndividual ieSubj = factory.getOWLNamedIndividual(nellFact.getSubject(), prefix);

        // create a same as link between subjects
        OWLSameIndividualAxiom sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(dbSubj, ieSubj);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(sameAsIndividualAxiom, 5));

        // fetch the individual objects
        OWLNamedIndividual dbObj = factory.getOWLNamedIndividual(fact.getObject(), prefix);
        OWLNamedIndividual ieObj = factory.getOWLNamedIndividual(nellFact.getObject(), prefix);

        // create a same as link between objects
        sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(dbObj, ieObj);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(sameAsIndividualAxiom, 5));

        // fetch the properties
        OWLObjectProperty dbProp = factory.getOWLObjectProperty(fact.getPredicate(), prefix);
        OWLObjectProperty ieProp = factory.getOWLObjectProperty(nellFact.getPredicate(), prefix);

        // create a same as/ is equivalent link between properties
        OWLEquivalentObjectPropertiesAxiom equivPropertyAxiom =
            factory.getOWLEquivalentObjectPropertiesAxiom(dbProp, ieProp);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(equivPropertyAxiom, 5));

    }

    /**
     * this method creates assertions of the form : subject and object are related with the property and also its
     * negative assertion
     * 
     * @param fact
     * @param ontology
     * @param confidence
     */
    private void createFactAssertion(SuggestedFactDAO fact, OWLOntology ontology, Double confidence)
    {
        double negativeConfidence = computeNegativeConfidence(confidence);

        // specify the <S> <P> <O> by getting hold of the necessary individuals and object property
        OWLNamedIndividual subject = factory.getOWLNamedIndividual(fact.getSubject(), prefix);
        OWLNamedIndividual object = factory.getOWLNamedIndividual(fact.getObject(), prefix);
        OWLObjectProperty property = factory.getOWLObjectProperty(fact.getPredicate(), prefix);

        // To specify that <S P O> we create an object property assertion and add it to the ontology
        OWLAxiom positivePropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(property, subject, object);

        // create the negative axiom for the fact
        // this essentially generates a conflict
        OWLAxiom negativePropertyAxiom = factory.getOWLNegativeObjectPropertyAssertionAxiom(property, subject, object);

        listAxioms.add(new Axiom(positivePropertyAssertion, confidence));
        listAxioms.add(new Axiom(negativePropertyAxiom, negativeConfidence));

        logger.info(confidence + "  " + negativeConfidence);
    }

    /**
     * method computes the negative assertion weight given the positive assertion weight. It works like this: Suppose:
     * fact with confidence w, has probability p, given by exp(w)/(1+ exp(w)). the probability of the negative assertion
     * is given by (1-p). hence from this value we can back calculate the weight/confidence of the negative assertion.
     * It turns out that just setting the negative of the original weight for the negative assertions does the job
     * (Simple maths !)
     * 
     * @param confidence
     * @return weight of the negated assertions
     */
    private double computeNegativeConfidence(Double confidence)
    {
        return (0 - confidence);
    }

    /**
     * writes the ontology to a file
     * 
     * @param manager
     * @param ontology
     */
    private void createOutput(OWLOntologyManager manager, OWLOntology ontology)
    {
        // Dump the ontology to file
        File file = new File(Constants.OWLFILE_CREATED_FROM_FACTS_OUTPUT_PATH);
        try {
            manager.saveOntology(ontology, IRI.create(file.toURI()));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }

    /**
     * stand alone entry point
     * 
     * @param args
     * @throws OWLOntologyCreationException
     */
    public static void main(String[] args) throws OWLOntologyCreationException
    {
        Set<SuggestedFactDAO> setFacts = new TreeSet<SuggestedFactDAO>();
        new AxiomCreator().createOwlFromFacts(setFacts, null);

    }

}

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
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
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

import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.helper.util.Utilities;

/**
 * This class serves as a point of converting a given set of facts into .owl
 * file. This step is important since, we want to apply a reasoner on top of
 * these axioms. We use OWL API for the purpose. A detailed documentation for
 * the API can be found at {@link http
 * ://owlapi.sourceforge.net/documentation.html}. This allows to create asioms
 * with weights(soft constraints) and also unweighted (hard constraints)
 * 
 * @author Arnab Dutta
 */
public class AxiomCreator
{

    /**
     * logger
     */
    public Logger logger = Logger.getLogger(AxiomCreator.class.getName());

    // Ontology namespace
    private static String ontologyNS = "http://www.semanticweb.org/ontologies/FactOntology/DbPedia/";

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
    PrefixManager prefixDBPedia = null;

    PrefixManager prefixIE = null;

    /**
     * a list of Axioms, essentially these are weighted and hence soft
     * constraints
     */
    List<Axiom> listAxioms = new ArrayList<Axiom>();

    /**
     * @throws OWLOntologyCreationException
     */
    public AxiomCreator() throws OWLOntologyCreationException
    {

        // create the manager
        manager = OWLManager.createOWLOntologyManager();

        // create Iri
        ontologyIRI = IRI.create(ontologyNS);

        // Get hold of a data factory from the manager and
        factory = manager.getOWLDataFactory();

        // set up a prefix manager to make things easier
        prefixDBPedia = new DefaultPrefixManager(IRI.create(DBPediaNS).toString());
        prefixIE = new DefaultPrefixManager(IRI.create(extractionEngineNS).toString());

    }

    /**
     * method takes a Set of {@link SuggestedFactDAO} and also the actual fact
     * from Extraction engine and creates all possible axioms out of those
     * Object assertions, sameAs axioms etc
     * 
     * @param listFacts Set of {@link SuggestedFactDAO}
     * @param ieFactDAo fact from Extraction engine
     * @throws OWLOntologyCreationException
     */
    public void createOwlFromFacts(List<SuggestedFactDAO> dbPediaFacts, SuggestedFactDAO ieFact)
            throws OWLOntologyCreationException
    {

        // create an ontology
        OWLOntology ontology = manager.createOntology(ontologyIRI);

        // creates the object property assertion from the matched IE fact
        createObjectPropertyAssertions(ieFact, prefixIE);

        // iterate for each suggested fact, create assertions
        for (SuggestedFactDAO dbPediaFact : dbPediaFacts) {

            logger.info(" Processing " + dbPediaFact.toString());

            // creates all assertions from the matched DBPedia fact
            createObjectPropertyAssertions(dbPediaFact,
                    prefixDBPedia);

            // creates all individual fact assertions by saying subjects from
            // two facts are same
            createSameAsAssertions(dbPediaFact, ieFact, ontology);

        }
        // annotate the axioms
        annotateAxioms(ontology);

        // output to a file
        createOutput(ontology);
    }

    /**
     * Overloaded function
     * 
     * @param candidateSubjs Candidate list for possible subjects
     * @param candidatePreds Candidate list for possible predicates
     * @param candidateObjs Candidate list for possible objects
     * @param uncertainFact Uncertain Extraction engine fact
     * @throws OWLOntologyCreationException
     */
    public void createOwlFromFacts(String[] candidateSubjs, String[] candidatePreds,
            String[] candidateObjs, SuggestedFactDAO uncertainFact)
            throws OWLOntologyCreationException {

        // create an ontology
        OWLOntology ontology = manager.createOntology(ontologyIRI);

        // create same as links with the extraction engine extract and the
        // candidate subjects and objects
        createSameAsAssertions(candidateSubjs, uncertainFact.getSubject());
        createSameAsAssertions(candidateObjs, uncertainFact.getObject());

        // create same as links with the extraction engine extract and the
        // candidate properties
        createPropEquivAssertions(candidatePreds, uncertainFact.getPredicate());

        // creates the object property assertion from the matched IE fact
        createObjectPropertyAssertions(uncertainFact, prefixIE);

        // explicitly define that all the candidates are different from each
        // other
        createDifferentFromAssertions(candidateSubjs);
        createDifferentFromAssertions(candidateObjs);

        // annotate the axioms
        annotateAxioms(ontology);

        // output to a file
        createOutput(ontology);

    }

    /**
     * method creates a all different from axiom of the candidate list
     * 
     * @param candidateSubjs collection of possible candidates
     */
    private void createDifferentFromAssertions(String[] candidates) {

        Set<OWLNamedIndividual> setIndividuals = new TreeSet<OWLNamedIndividual>();

        for (String candidate : candidates) {

            // create the owl individual
            OWLNamedIndividual dbCandidateValue = factory.getOWLNamedIndividual(
                    Utilities.prun(candidate),
                    prefixDBPedia);

            // add to a set
            setIndividuals.add(dbCandidateValue);
        }

        // add the bunch of distinct individual to the axiom
        OWLDifferentIndividualsAxiom diffInds = factory
                .getOWLDifferentIndividualsAxiom(setIndividuals);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(diffInds, convertProbabilityToWeight(1.0)));
    }

    /**
     * similar to createSameAsAssertions, but for properties we should have
     * equivalent property link, analogous to sameAs link
     * 
     * @param candidatePreds possible predicates
     * @param predicate extracted predicate
     */
    private void createPropEquivAssertions(String[] candidatePreds, String predicate) {

        // iterate through the possible list of candidates and as many
        // equivalent property links
        for (String possibleCandidate : candidatePreds) {

            // fetch the properties
            OWLObjectProperty dbProperty = factory.getOWLObjectProperty(
                    Utilities.prun(possibleCandidate),
                    prefixDBPedia);
            OWLObjectProperty ieProperty = factory.getOWLObjectProperty(
                    predicate, prefixIE);

            // create a same as/ is equivalent link between properties
            OWLEquivalentObjectPropertiesAxiom equivPropertyAxiom =
                    factory.getOWLEquivalentObjectPropertiesAxiom(dbProperty, ieProperty);

            // add it to list of soft constraints
            listAxioms.add(new Axiom(equivPropertyAxiom, convertProbabilityToWeight(1.0)));// TODO

        }
    }

    /**
     * overridden method. Takes a list of candidate mathces and an extracted
     * text. Creates same as links between the extracted text to the candidates
     * with a score given by the score of matching
     * 
     * @param candidates collection of possible matches
     * @param extractedValue extracted value from Nell or freeverb etc
     */
    private void createSameAsAssertions(String[] candidates, String extractedValue) {

        // iterate through the possible list of candidates and as many same as
        // links between them
        double count = 0;
        for (String possibleCandidate : candidates) {

            count = count - 0.1;
            // fetch the individual subjects
            OWLNamedIndividual dbValue = factory.getOWLNamedIndividual(
                    Utilities.prun(possibleCandidate),
                    prefixDBPedia);
            OWLNamedIndividual ieValue = factory.getOWLNamedIndividual(
                    extractedValue, prefixIE);

            // create a same as link between subjects
            OWLSameIndividualAxiom sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(
                    dbValue, ieValue);

            // add it to list of soft constraints
            listAxioms
                    .add(new Axiom(sameAsIndividualAxiom, convertProbabilityToWeight(1.0 + count))); // TODO
        }
    }

    /**
     * @param prob
     * @return
     */
    private double convertProbabilityToWeight(double prob) {
        // smoothing
        if (prob >= 1)
            prob = 0.99;
        return Math.log(prob / (1 - prob));
    }

    /**
     * create same as links between two facts (one from IE the other from
     * DBPedia) for each individual subjects, pr
     * 
     * @param fact
     * @param nellFact
     * @param ontology
     */
    private void createSameAsAssertions(SuggestedFactDAO fact, SuggestedFactDAO nellFact,
            OWLOntology ontology)
    {
        // fetch the individual subjects
        OWLNamedIndividual dbSubj = factory.getOWLNamedIndividual(fact.getSubject(), prefixDBPedia);
        OWLNamedIndividual ieSubj = factory.getOWLNamedIndividual(nellFact.getSubject(), prefixIE);

        // create a same as link between subjects
        OWLSameIndividualAxiom sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(dbSubj,
                ieSubj);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(sameAsIndividualAxiom, 5));

        // fetch the individual objects
        OWLNamedIndividual dbObj = factory.getOWLNamedIndividual(fact.getObject(), prefixDBPedia);
        OWLNamedIndividual ieObj = factory.getOWLNamedIndividual(nellFact.getObject(), prefixIE);

        // create a same as link between objects
        sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(dbObj, ieObj);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(sameAsIndividualAxiom, 5));

        // fetch the properties
        OWLObjectProperty dbProp = factory.getOWLObjectProperty(fact.getPredicate(), prefixDBPedia);
        OWLObjectProperty ieProp = factory.getOWLObjectProperty(nellFact.getPredicate(), prefixIE);

        // create a same as/ is equivalent link between properties
        OWLEquivalentObjectPropertiesAxiom equivPropertyAxiom =
                factory.getOWLEquivalentObjectPropertiesAxiom(dbProp, ieProp);

        // add it to list of soft constraints
        listAxioms.add(new Axiom(equivPropertyAxiom, 5));

    }

    /**
     * this method creates assertions of the form : subject and object are
     * related with the property and also its negative assertion
     * 
     * @param fact {@link SuggestedFactDAO} instance
     * @param prefix ontology prefix
     */
    private void createObjectPropertyAssertions(SuggestedFactDAO fact, PrefixManager prefix)
    {
        // double negativeConfidence =
        // computeNegativeConfidence(fact.getConfidence());

        // specify the <S> <P> <O> by getting hold of the necessary individuals
        // and object property
        OWLNamedIndividual subject = factory.getOWLNamedIndividual(fact.getSubject(), prefix);
        OWLNamedIndividual object = factory.getOWLNamedIndividual(fact.getObject(), prefix);
        OWLObjectProperty property = factory.getOWLObjectProperty(fact.getPredicate(), prefix);

        // To specify that <S P O> we create an object property assertion and
        // add it to the ontology
        OWLAxiom positivePropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(property,
                subject, object);

        listAxioms.add(new Axiom(positivePropertyAssertion, convertProbabilityToWeight(fact
                .getConfidence())));

        // create the negative axiom for the fact
        // this essentially generates a conflict
        // OWLAxiom negativePropertyAxiom =
        // factory.getOWLNegativeObjectPropertyAssertionAxiom(property, subject,
        // object);
        // listAxioms.add(new Axiom(negativePropertyAxiom, negativeConfidence));

    }

    /**
     * method computes the negative assertion weight given the positive
     * assertion weight. It works like this: Suppose: fact with confidence w,
     * has probability p, given by exp(w)/(1+ exp(w)). the probability of the
     * negative assertion is given by (1-p). hence from this value we can back
     * calculate the weight/confidence of the negative assertion. It turns out
     * that just setting the negative of the original weight for the negative
     * assertions does the job (Simple maths !)
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
     * @param ontology
     */
    private void createOutput(OWLOntology ontology)
    {
        // Dump the ontology to a file
        File file = new File(Constants.OWLFILE_CREATED_FROM_FACTS_OUTPUT_PATH);
        try {
            manager.saveOntology(ontology, IRI.create(file.toURI()));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } finally {
            logger.info("Axiom file created at : "
                    + Constants.OWLFILE_CREATED_FROM_FACTS_OUTPUT_PATH);

        }
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

            logger.info("Annotating " + axiom.toString());

            // create an annotation
            OWLAnnotation owlAnnotation =
                    factory.getOWLAnnotation(annotationProbability,
                            factory.getOWLLiteral(axiom.getConfidence()));

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

        OWLDisjointClassesAxiom disjointClassesAxiom = factory.getOWLDisjointClassesAxiom(subClass,
                objClass);
        manager.addAxiom(ontology, disjointClassesAxiom);

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
        // new AxiomCreator().createOwlFromFacts(setFacts, null);

    }

}

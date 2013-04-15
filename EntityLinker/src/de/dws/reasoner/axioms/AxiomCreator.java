/**
 * 
 */

package de.dws.reasoner.axioms;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import de.dws.mapper.helper.dataObject.ResultDAO;
import de.dws.mapper.helper.dataObject.SuggestedFactDAO;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.helper.util.Utilities;

/**
 * This class serves as a point of converting a given set of facts into .owl
 * file. This step is important since, we want to apply a reasoner on top of
 * these axioms. We use OWL API for the purpose. A detailed documentation for
 * the API can be found at {@link http
 * ://owlapi.sourceforge.net/documentation.html}. This allows to create axioms
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

        File file = new File(Constants.OWL_INPUT_FILE_PATH);

        // Now load the local copy
        ontology = manager.loadOntologyFromOntologyDocument(file);

        // create Iri
        ontologyIRI = IRI.create(Constants.ONTOLOGY_NAMESPACE);

        // Get hold of a data factory from the manager and
        factory = manager.getOWLDataFactory();

        // set up a prefix manager to make things easier
        prefixDBPedia = new DefaultPrefixManager(IRI.create(Constants.ONTOLOGY_DBP_NS).toString());
        prefixIE = new DefaultPrefixManager(IRI.create(Constants.ONTOLOGY_EXTRACTION_NS).toString());

    }

    /**
     * @return the ontology
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * Overloaded function
     * 
     * @param candidateSubjs Candidate list for possible subjects
     * @param candidatePreds Candidate list for possible predicates
     * @param candidateObjs Candidate list for possible objects
     * @param uncertainFact Uncertain Extraction engine fact
     * @param entityTypesMap Map containing the entity type information
     * @throws OWLOntologyCreationException
     */
    public void createOwlFromFacts(List<ResultDAO> candidateSubjs, List<ResultDAO> candidatePreds,
            List<ResultDAO> candidateObjs, SuggestedFactDAO uncertainFact,
            Map<String, List<String>> entityTypesMap)
            throws OWLOntologyCreationException {

        // create an ontology
        // OWLOntology ontology = manager.createOntology(ontologyIRI);

        // File file = new File(Constants.OWL_INPUT_FILE_PATH);

        // Now load the local copy
        // OWLOntology ontology =
        // manager.loadOntologyFromOntologyDocument(file);
        //System.out.println("Loaded ontology: " + getOntology());

        // create domain range restriction on the IE property
        // creatDomainRangeRestriction(ontology, uncertainFact.getPredicate());

        // create same as links with the extraction engine extract and the
        // candidate subjects and objects
        createSameAsAssertions(ontology, candidateSubjs,
                uncertainFact.getSubject(), entityTypesMap);
        createSameAsAssertions(ontology, candidateObjs,
                uncertainFact.getObject(), entityTypesMap);
        // create same as links with the extraction engine extract and the
        // candidate properties
        createPropEquivAssertions(candidatePreds, uncertainFact.getPredicate());

        // creates the object property assertion from the IE fact
        createObjectPropertyAssertions(uncertainFact, prefixIE);

        // explicitly define that all the candidates are different from each
        // other
        createDifferentFromAssertions(candidateSubjs);
        createDifferentFromAssertions(candidateObjs);

        // annotate the axioms
       // annotateAxioms();

        // output to a file
        //createOutput();

        // pause few seconds for the output axiom files to be
        // created
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.info(listAxioms.size());
    }

    private void creatDomainRangeRestriction(OWLOntology ontology, String predicate) {

        OWLObjectProperty ieProperty = factory.getOWLObjectProperty(
                predicate, prefixIE);

        // also add domain range restriction on the property
        OWLClass domain = factory.getOWLClass(IRI.create(ontologyIRI + "Person"));
        OWLClass range = factory.getOWLClass(IRI.create(ontologyIRI + "Place"));

        OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(
                ieProperty,
                domain);
        OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(ieProperty,
                range);

        // add to the manager as hard constraints
        manager.addAxiom(ontology, domainAxiom);
        manager.addAxiom(ontology, rangeAxiom);
    }

    /**
     * method creates a all different from axiom of the candidate list
     * 
     * @param candidateSubjs collection of possible candidates
     */
    private void createDifferentFromAssertions(List<ResultDAO> candidates) {

        Set<OWLNamedIndividual> setIndividuals = new TreeSet<OWLNamedIndividual>();

        for (ResultDAO candidate : candidates) {

            // create the owl individual
            OWLNamedIndividual dbCandidateValue = factory.getOWLNamedIndividual(
                    Utilities.prun(candidate.getFieldURI()),
                    prefixDBPedia);

            // add to a set
            setIndividuals.add(dbCandidateValue);
        }

        // add the bunch of distinct individual to the axiom
        OWLDifferentIndividualsAxiom diffInds = factory
                .getOWLDifferentIndividualsAxiom(setIndividuals);

        // add it to list of soft constraints with high probability
        listAxioms.add(new Axiom(diffInds, convertProbabilityToWeight(1.0)));
    }

    /**
     * similar to createSameAsAssertions, but for properties we should have
     * equivalent property link, analogous to sameAs link
     * 
     * @param candidatePreds possible predicates
     * @param predicate extracted predicate
     */
    private void createPropEquivAssertions(List<ResultDAO> candidatePreds, String predicate) {

        OWLObjectProperty ieProperty = factory.getOWLObjectProperty(
                predicate, prefixIE);

        // iterate through the possible list of candidates and as many
        // equivalent property links
        for (ResultDAO possibleCandidate : candidatePreds) {

            // fetch the properties
            OWLObjectProperty dbProperty = factory.getOWLObjectProperty(
                    Utilities.prun(possibleCandidate.getFieldURI()),
                    prefixDBPedia);

            // create a same as/ is equivalent link between properties
            OWLEquivalentObjectPropertiesAxiom equivPropertyAxiom =
                    factory.getOWLEquivalentObjectPropertiesAxiom(dbProperty, ieProperty);

            // add it to list of soft constraints
            listAxioms.add(new Axiom(equivPropertyAxiom,
                    convertProbabilityToWeight(possibleCandidate.getScore())));

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
    private void createSameAsAssertions(OWLOntology ontology, List<ResultDAO> candidates,
            String extractedValue,
            Map<String, List<String>> entityTypesMap) {

        // iterate through the possible list of candidates and as many same as
        // links between them
        for (ResultDAO possibleCandidate : candidates) {

            // fetch the individual subjects
            OWLNamedIndividual dbValue = factory.getOWLNamedIndividual(
                    Utilities.prun(possibleCandidate.getFieldURI()),
                    prefixDBPedia);
            OWLNamedIndividual ieValue = factory.getOWLNamedIndividual(
                    extractedValue, prefixIE);

            // create a same as link between subjects
            OWLSameIndividualAxiom sameAsIndividualAxiom = factory.getOWLSameIndividualAxiom(
                    dbValue, ieValue);

            // create the type of axioms. these are hard constraints
            double score = generateIsTypeOfAssertions(ontology, possibleCandidate, entityTypesMap);

            // add it to list of soft constraints
            listAxioms
                    .add(new Axiom(sameAsIndividualAxiom,
                            convertProbabilityToWeight(score)));
        }
    }

    private double generateIsTypeOfAssertions(OWLOntology ontology, ResultDAO possibleCandidate,
            Map<String, List<String>> entityTypesMap) {

        try {
            List<String> typesOfThisEntity = entityTypesMap.get(possibleCandidate.getFieldURI());

            // type information may be missing for some

            if (typesOfThisEntity != null && typesOfThisEntity.size() > 0) {
                // create the individual candidate
                OWLNamedIndividual dbValue = factory.getOWLNamedIndividual(
                        Utilities.prun(possibleCandidate.getFieldURI()),
                        prefixDBPedia);
                for (String type : typesOfThisEntity) {
                    OWLClass typeOfClass = factory.getOWLClass(IRI.create(ontologyIRI
                            + Utilities.prun(type)));

                    // create the is type of assertion
                    OWLClassAssertionAxiom classAssertionAx = factory.getOWLClassAssertionAxiom(
                            typeOfClass, dbValue);

                    // add to the manager
                    manager.addAxiom(ontology, classAssertionAx);
                }
                return possibleCandidate.getScore();
            }
        } catch (Exception e) {
            logger.info("Exception in " + e.getMessage());
        }
        return 0;
    }

    /**
     * @param prob
     * @return
     */
    private double convertProbabilityToWeight(double prob) {
        // smoothing
        if (prob >= 1)
            prob = 0.99;
        if (prob <= 0)
            prob = 0.01;

        double conf = Math.log(prob / (1 - prob));
        // logger.info(prob + " => " + conf);
        return conf;
    }

    /*
     * /** create same as links between two facts (one from IE the other from
     * DBPedia) for each individual subjects, pr
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
     */
    public void createOutput()
    {
        // Dump the ontology to a file
        File file = new File(Constants.OWLFILE_CREATED_FROM_FACTS_OUTPUT_PATH);
        try {
            manager.saveOntology(getOntology(), IRI.create(file.toURI()));
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
    public void annotateAxioms()
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
            manager.addAxiom(getOntology(), annotatedAxiom);

            // clear the set
            annotationSet.clear();
        }

    }

    /*
     * private void createTBoxAxioms(OWLOntology ontology, List<String>
     * classNames) { OWLClass subClass = null; OWLClass objClass = null;
     * OWLDisjointClassesAxiom disjointClassesAxiom = null; for (int i = 0; i <
     * classNames.size(); i++) { for (int j = i + 1; j < classNames.size(); j++)
     * { subClass = factory.getOWLClass(IRI.create(ontologyIRI +
     * classNames.get(i))); objClass =
     * factory.getOWLClass(IRI.create(ontologyIRI + classNames.get(j)));
     * disjointClassesAxiom = factory.getOWLDisjointClassesAxiom(subClass,
     * objClass); manager.addAxiom(ontology, disjointClassesAxiom); } } }
     */

    /**
     * add disjointness axiom
     * 
     * @param ontology
     */
    /*
     * private void createTBoxAxioms(OWLOntology ontology) { OWLClass subClass =
     * factory.getOWLClass(IRI.create(ontologyIRI + "Person")); OWLClass
     * objClass = factory.getOWLClass(IRI.create(ontologyIRI + "Work"));
     * OWLDisjointClassesAxiom disjointClassesAxiom =
     * factory.getOWLDisjointClassesAxiom(subClass, objClass);
     * manager.addAxiom(ontology, disjointClassesAxiom); subClass =
     * factory.getOWLClass(IRI.create(ontologyIRI + "Organisation")); objClass =
     * factory.getOWLClass(IRI.create(ontologyIRI + "Work"));
     * disjointClassesAxiom = factory.getOWLDisjointClassesAxiom(subClass,
     * objClass); manager.addAxiom(ontology, disjointClassesAxiom); }
     */

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

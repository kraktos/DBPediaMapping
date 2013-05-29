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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import de.dws.helper.util.Constants;
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
     * create inverse relation on a predicate
     * 
     * @param predicate
     * @param inverse
     */
    public void createInverseRelations(String predicate, String inverse) {
        OWLObjectProperty ieProperty = factory.getOWLObjectProperty(
                predicate, prefixIE);

        OWLObjectProperty ieInverseProperty = factory.getOWLObjectProperty(
                inverse, prefixIE);

        OWLInverseObjectPropertiesAxiom inverseAxiom = factory.getOWLInverseObjectPropertiesAxiom(
                ieProperty, ieInverseProperty);

        // add to the manager as hard constraints
        manager.addAxiom(ontology, inverseAxiom);
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
                predicate, prefixIE);

        // also add domain range restriction on the property
        OWLClass domainCls = factory.getOWLClass(IRI.create(Constants.ONTOLOGY_EXTRACTION_NS
                + domain));
        OWLClass rangeCls = factory.getOWLClass(IRI
                .create(Constants.ONTOLOGY_EXTRACTION_NS + range));

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

}

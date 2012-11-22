/**
 * 
 */
package com.mapper.utility;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * @author arnab
 * 
 */
public class OntologyImpl implements IOntology {

	OWLOntologyManager manager = null;
	IRI documentIRI = null;
	OWLOntology owlOntology = null;

	public OntologyImpl() {
		// Get hold of an ontology manager
		manager = OWLManager.createOWLOntologyManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mapper.utility.IOntology#loadOntology()
	 */
	@Override
	public void loadOntology(final String owlPath)
			throws OWLOntologyCreationException {

		File file = new File(owlPath);
		// load the local copy
		owlOntology = manager.loadOntologyFromOntologyDocument(file);
		System.out.println("Loaded ontology: " + owlOntology
				+ "\n Ontology ID =" + owlOntology.getOntologyID());

		// We can always obtain the location where an ontology was loaded from
		documentIRI = manager.getOntologyDocumentIRI(owlOntology);
		System.out.println("    from: " + documentIRI); //$NON-NLS-1$

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mapper.utility.IOntology#readOntology()
	 */
	@Override
	public void readOntology() {
		// We can get a reference to a data factory from an OWLOntologyManager.
		OWLDataFactory factory = manager.getOWLDataFactory();
		// Now we create the class
		OWLClass clsAMethodA = factory.getOWLClass(documentIRI);

		OWLAnnotationProperty label = factory
				.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		for (OWLClass cls : owlOntology.getClassesInSignature()) {
			// Get the annotations on the class that use the label property
			/*
			 * for (OWLAnnotation annotation : cls.getAnnotations(owlOntology,
			 * label)) { if (annotation.getValue() instanceof OWLLiteral) {
			 * OWLLiteral val = (OWLLiteral) annotation.getValue(); if
			 * (val.hasLang("pt")) { System.out.println(cls + " -> " +
			 * val.getLiteral()); } } }
			 */
			for (OWLSubClassOfAxiom ax : owlOntology
					.getSubClassAxiomsForSubClass(cls)) {
				OWLClassExpression superCls = ax.getSuperClass();
				logger.info(cls.toString() + " \t " + superCls.toString());
			}
		}
	}
}

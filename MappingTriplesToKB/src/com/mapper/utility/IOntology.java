/**
 * 
 */
package com.mapper.utility;


import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.mapper.client.Main;

/**
 * @author arnab
 * 
 */
interface IOntology {

	public Logger logger = Logger.getLogger(Main.class.getName());

	/**
	 * read an ontology file * @throws Exception
	 */
	public void readOntology();

	/**
	 * load an ontology file
	 * 
	 * @throws OWLOntologyCreationException
	 * 
	 */
	public void loadOntology(final String owlPath)
			throws OWLOntologyCreationException;

}

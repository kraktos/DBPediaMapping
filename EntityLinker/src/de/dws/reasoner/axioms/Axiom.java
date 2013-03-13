/**
 * 
 */
package de.dws.reasoner.axioms;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Arnab Dutta
 */
public class Axiom
{

    private OWLAxiom axiom;

    private double confidence;

    /**
     * @param axiom
     * @param confidence
     */
    public Axiom(OWLAxiom axiom, double confidence)
    {
        this.axiom = axiom;
        this.confidence = confidence;
    }

    /**
     * @return the axiom
     */
    public OWLAxiom getAxiom()
    {
        return axiom;
    }

    /**
     * @return the confidence
     */
    public double getConfidence()
    {
        return confidence;
    }

}

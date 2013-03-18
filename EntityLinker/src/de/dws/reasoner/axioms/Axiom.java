/**
 * 
 */

package de.dws.reasoner.axioms;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Custom Axioms built using the {@link OWLAxiom}
 * 
 * @author Arnab Dutta
 */
public class Axiom
{

    /**
     * {@link OWLAxiom} instance
     */
    private OWLAxiom axiom;

    /**
     * associated confidence of statements
     */
    private double confidence;

    /**
     * @param axiom {@link OWLAxiom} instance
     * @param confidence weight of the triples
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Axiom [axiom=" + axiom.toString() + ", confidence=" + confidence + "]";
    }

}

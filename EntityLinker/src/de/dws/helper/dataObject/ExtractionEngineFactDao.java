/**
 * 
 */
package de.dws.helper.dataObject;

/**
 * data access object for storing the fact coming as input from extraction engine
 * 
 * @author Arnab Dutta
 */
public class ExtractionEngineFactDao
{
    // subject variable
    private String subject;

    // object variable
    private String predicate;

    // predicate variable
    private String object;

    // truth value of the fact
    private Double confidence;

    
    /**
     * @param subject
     * @param predicate
     * @param object
     * @param confidence
     */
    public ExtractionEngineFactDao(String subject, String predicate, String object, Double confidence)
    {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.confidence = confidence;
    }

    /**
     * @return the subject
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * @return the predicate
     */
    public String getPredicate()
    {
        return predicate;
    }

    /**
     * @return the object
     */
    public String getObject()
    {
        return object;
    }

    /**
     * @return the confidence
     */
    public Double getConfidence()
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
        return "ExtractionEngineFactDao [subject=" + subject + ", predicate=" + predicate + ", object=" + object
            + ", confidence=" + confidence + "]";
    }

}

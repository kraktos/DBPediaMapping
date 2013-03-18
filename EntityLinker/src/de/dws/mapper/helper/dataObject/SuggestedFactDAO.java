/**
 * 
 */
package de.dws.mapper.helper.dataObject;

/**
 * data access object for storing the suggested fact in the form of triples
 * 
 * @author Arnab Dutta
 */
public class SuggestedFactDAO
{
    // subject variable
    private String subject;

    // object variable
    private String predicate;

    // predicate variable
    private String object;

    private Double confidence;

    // flag to determine true or false flag
    private boolean truthFlag;

    /**
     * @param subject
     * @param predicate
     * @param object
     * @param confidence
     * @param truthFlag
     */
    public SuggestedFactDAO(String subject, String predicate, String object, Double confidence, boolean truthFlag)
    {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.confidence = confidence;
        this.truthFlag = truthFlag;
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
     * @return the truthFlag
     */
    public boolean isTruthFlag()
    {
        return truthFlag;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate)
    {
        this.predicate = predicate;
    }

    /**
     * @param object the object to set
     */
    public void setObject(String object)
    {
        this.object = object;
    }

    /**
     * @param truthFlag the truthFlag to set
     */
    public void setTruthFlag(boolean truthFlag)
    {
        this.truthFlag = truthFlag;
    }

    /**
     * @return the confidence
     */
    public Double getConfidence()
    {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     */
    public void setConfidence(double confidence)
    {
        this.confidence = confidence;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((confidence == null) ? 0 : confidence.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + (truthFlag ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SuggestedFactDAO))
            return false;
        SuggestedFactDAO other = (SuggestedFactDAO) obj;
        if (confidence == null) {
            if (other.confidence != null)
                return false;
        } else if (!confidence.equals(other.confidence))
            return false;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        if (truthFlag != other.truthFlag)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SuggestedFactDAO [subject=" + subject + ", predicate=" + predicate + ", object=" + object
            + ", confidence=" + confidence + ", truthFlag=" + truthFlag + "]";
    }

}

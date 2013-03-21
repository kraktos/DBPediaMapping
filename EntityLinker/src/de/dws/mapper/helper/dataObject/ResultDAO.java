/**
 * 
 */

package de.dws.mapper.helper.dataObject;

import java.util.Comparator;

/**
 * Class for holding the result elements
 * 
 * @author Arnab Dutta
 */
public class ResultDAO
{

    /**
     * holds the URI of the DBPedia entities
     */
    private String fieldURI;

    /**
     * stores the score
     */
    private double score;

    /**
     * stores the label
     */
    private String label;

    /**
     * stores the high frequency
     */
    private String isHighFreq;

    /**
     * type of this entity
     */
    private String type;

    /**
     * @param fieldURI
     * @param score
     * @param label
     * @param isHighFreq
     */
    public ResultDAO(String fieldURI, String label, String isHighFreq, double score)
    {
        this.fieldURI = fieldURI;
        this.score = score;
        this.label = label;
        this.isHighFreq = isHighFreq;
    }

    /**
     * @param fieldURI
     * @param score
     * @param label
     */
    public ResultDAO(String fieldURI, String label, double score)
    {
        this.fieldURI = fieldURI;
        this.score = score;
        this.label = label;
    }

    /**
     * @param fieldURI
     * @param score
     */
    public ResultDAO(String fieldURI, double score)
    {
        this.fieldURI = fieldURI;
        this.score = score;
    }

    /**
     * @return the fieldURI
     */
    public String getFieldURI()
    {
        return fieldURI;
    }

    /**
     * @return the score
     */
    public double getScore()
    {
        return score;
    }

    /**
     * @return the label
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return the isHighFreq
     */
    public String getIsHighFreq()
    {
        return isHighFreq;
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResultDAO [fieldURI=" + fieldURI + ", score=" + score + "]";
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldURI == null) ? 0 : fieldURI.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ResultDAO))
            return false;
        ResultDAO other = (ResultDAO) obj;
        if (fieldURI == null) {
            if (other.fieldURI != null)
                return false;
        } else if (!fieldURI.equals(other.fieldURI))
            return false;
        return true;
    }

}

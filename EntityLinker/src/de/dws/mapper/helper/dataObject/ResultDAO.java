/**
 * 
 */
package de.dws.mapper.helper.dataObject;

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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ResultDAO [fieldURI=" + fieldURI + "]";
    }

}

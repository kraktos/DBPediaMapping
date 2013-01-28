/**
 * 
 */
package com.mapper.dataObjects;

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

}

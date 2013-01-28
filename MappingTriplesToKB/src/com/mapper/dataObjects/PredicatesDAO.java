/**
 * 
 */
package com.mapper.dataObjects;

/**
 * Class to hold the possible set of predicates, determined by the system
 * 
 * @author Arnab Dutta
 */
public class PredicatesDAO implements Comparable<PredicatesDAO>
{
    // possible predicate
    private String possiblePredicate;

    // jaccard score for this possible predicate with the given predicate from IE
    private double jaccardScore;

    /**
     * @param possiblePredicate
     * @param jaccardScore
     */
    public PredicatesDAO(String possiblePredicate, double jaccardScore)
    {
        this.possiblePredicate = possiblePredicate;
        this.jaccardScore = jaccardScore;
    }

    @Override
    public int compareTo(PredicatesDAO obj)
    {
        // sort by descending order based on scores
        return (this.jaccardScore >= obj.jaccardScore) ? -1 : 1;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return possiblePredicate + ":" + jaccardScore;
    }

}

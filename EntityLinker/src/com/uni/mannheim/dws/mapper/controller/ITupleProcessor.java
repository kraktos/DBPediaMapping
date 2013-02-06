/**
 * 
 */
package com.uni.mannheim.dws.mapper.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Essentially differnt Information extraction engines create data in different formats which requires different parsing
 * and handling methodologies. This is the Interface providing the basic functionality of preocessing the tuples. See
 * {@link ReVerbTupleProcessor} and {@link NellTupleProcessor}
 * 
 * @author Arnab Dutta
 */
public interface ITupleProcessor
{

    /**
     * Takes an input file and process them by each tuple
     * 
     * @param dataFilePath location of the input File. Can be {@code null}
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void processTuples(final String dataFilePath) throws IOException, InterruptedException, ExecutionException;

    /**
     * Map to store the possible predicates. This looks something like this <code>
     * born -> [dbpedia.org/property/birthPlace -> 10 ; dbpedia.org/ontology/birthPlace -> 22] 
     * 
     * </code> Broadly it is a Map a String vs Map, where each of the value maps are a collection of all the DBPedia
     * possible property matches with the count of their occurrences as value
     */
    public Map<String, HashMap<String, Integer>> predicateSurfaceFormsMap =
        new HashMap<String, HashMap<String, Integer>>();

    // keeps track of all the dbpedia predicates encountered with their counts
    public Map<String, Integer> dbPediaPredicatesCountMap = new HashMap<String, Integer>();

    // keeps track of all the predicates encountered from the IE engine with their counts
    public Map<String, Integer> iePredicatesCountMap = new HashMap<String, Integer>();

}

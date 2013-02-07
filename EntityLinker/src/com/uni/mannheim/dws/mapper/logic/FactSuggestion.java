/**
 * 
 */
package com.uni.mannheim.dws.mapper.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.uni.mannheim.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;

/**
 * @author Arnab Dutta
 */
public class FactSuggestion
{

    // define Logger
    static Logger logger = Logger.getLogger(FactSuggestion.class.getName());

    /**
     * @param retListSubj list of possible subjects, cannot be null
     * @param retListPredLookUp list of possible predicates from file lookup, can be null
     * @param retListPredSearch list of possible predicates from index lookup, cannot be null
     * @param retListObj list of possible objects, cannot be null
     * @return
     */
    public static List<SuggestedFactDAO> suggestFact(List<ResultDAO> retListSubj, List<ResultDAO> retListPredLookUp,
        List<ResultDAO> retListPredSearch, List<ResultDAO> retListObj)
    {

        List<SuggestedFactDAO> retList = new ArrayList<SuggestedFactDAO>();
        logger.info(retListObj.get(0).toString() + "  " + retListPredLookUp.get(0) + "  " + retListPredSearch.get(0)
            + "  " + retListSubj.get(0));

        // take all the top candidates
        List<String> subs = new ArrayList<String>();
        List<String> preds = new ArrayList<String>();
        List<String> objs = new ArrayList<String>();

        // iterate the results to fetch the top scoring matches to generate a possible set of facts
        for (ResultDAO dao : retListSubj) {
            if (dao.getScore() == 100.00) {
                subs.add(dao.getFieldURI());
            } else {
                break;
            }
        }

        for (ResultDAO dao : retListObj) {
            if (dao.getScore() == 100.00) {
                objs.add(dao.getFieldURI());
            } else {
                break;
            }
        }

        for (ResultDAO dao : retListPredSearch) {
            if (dao.getScore() == 100.00) {
                preds.add(dao.getFieldURI());
            } else {
                break;
            }
        }

        for (ResultDAO dao : retListPredLookUp) {
            if (dao.getScore() == 100.00) {
                preds.add(dao.getFieldURI());
            } else {
                break;
            }
        }

        //logger.info(preds.toString());

        logger.info("before= " + subs.toString());
        
        subs = createPossibleSubs(preds, objs);

        logger.info("after = " + subs.toString());
        
        /*retList.add(new SuggestedFactDAO(retListSubj.get(0).getFieldURI(), retListPredLookUp.get(0).getFieldURI(),
            retListObj.get(0).getFieldURI(), false));

        retList.add(new SuggestedFactDAO(retListSubj.get(0).getFieldURI(), retListPredSearch.get(0).getFieldURI(),
            retListObj.get(0).getFieldURI(), false));*/

        return retList;

    }

    private static List<String> createPossibleSubs(List<String> arg1, List<String> arg2)
    {
        String sparqlQuery = null;
        ResultSet results = null;

        List<String> possibleSubs = new ArrayList<String>();

        for (String pred : arg1) {
            for (String obj : arg2) {
                sparqlQuery = "select ?arg where {?arg <" + pred + "> <" + obj + ">}";
                logger.info(sparqlQuery);

                results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);

                while (results.hasNext()) {
                    possibleSubs.add(results.nextSolution().get("arg").toString());
                }
            }
        }

        return possibleSubs;
    }

}

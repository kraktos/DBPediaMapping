/**
 * 
 */
package com.uni.mannheim.dws.mapper.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.uni.mannheim.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.preProcess.estimator.PredicateLikelihoodEstimate;

/**
 * @author Arnab Dutta
 */
public class FactSuggestion
{

    // define Logger
    static Logger logger = Logger.getLogger(FactSuggestion.class.getName());

    /**
     * @param retListSubj list of possible subjects, cannot be null
     * @param qSub
     * @param retListPredLookUp list of possible predicates from file lookup, can be null
     * @param retListPredSearch list of possible predicates from index lookup, cannot be null
     * @param qPred
     * @param retListObj list of possible objects, cannot be null
     * @param qObj
     * @return {@code List of SuggestedFactDAO}
     */
    public static List<SuggestedFactDAO> suggestFact(List<ResultDAO> retListSubj, String qSub,
        List<ResultDAO> retListPredLookUp, List<ResultDAO> retListPredSearch, String qPred, List<ResultDAO> retListObj,
        String qObj, double minsim)
    {

        // take all the top candidates
        List<String> subs = new ArrayList<String>();
        List<String> preds = new ArrayList<String>();
        List<String> objs = new ArrayList<String>();

        // check if minimum similarity is 0
        minsim = (minsim != 0) ? minsim : Constants.SIMILARITY;

        // iterate the results to fetch the top scoring matches to generate a possible set of facts
        for (ResultDAO dao : retListSubj) {
            if (dao.getScore() >= minsim) {
                subs.add(dao.getFieldURI());
            } else
                break;
        }

        for (ResultDAO dao : retListObj) {
            if (dao.getScore() >= minsim) {
                objs.add(dao.getFieldURI());
            } else
                break;
        }

        for (ResultDAO dao : retListPredSearch) {
            if (dao.getScore() >= minsim) {
                preds.add(dao.getFieldURI());
            } else
                break;
        }

        for (ResultDAO dao : retListPredLookUp) {
            if (dao.getScore() >= minsim) {
                preds.add(dao.getFieldURI());
            }
        }

        return frameFacts(subs, preds, objs);
    }

    private static List<SuggestedFactDAO> frameFacts(List<String> tSubs, List<String> preds, List<String> tObjs)
    {
        List<SuggestedFactDAO> retList = new ArrayList<SuggestedFactDAO>();

        for (String pred : preds) {
            for (String sub : tSubs) {
                for (String obj : tObjs) {
                    retList.add(new SuggestedFactDAO(sub, pred, obj, null, false));
                }
            }
        }

        // Call engine to make an intelligent choice based on density estimation
        Map<Double, Set<SuggestedFactDAO>> mapReturn = PredicateLikelihoodEstimate.rankFacts(retList);

        retList.clear();

        for (Map.Entry<Double, Set<SuggestedFactDAO>> entry : mapReturn.entrySet()) {
            Set<SuggestedFactDAO> value = entry.getValue();

            for (SuggestedFactDAO dao : value) {
                retList.add(dao);
                logger.info(entry.getKey() + "  " + dao.toString());
            }
        }

        return retList;
    }

}

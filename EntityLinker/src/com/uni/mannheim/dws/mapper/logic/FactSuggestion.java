/**
 * 
 */
package com.uni.mannheim.dws.mapper.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.uni.mannheim.dws.mapper.engine.query.SPARQLEndPointQueryAPI;
import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;
import com.uni.mannheim.dws.mapper.helper.util.Constants;

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
     * @return
     */
    public static List<SuggestedFactDAO> suggestFact(List<ResultDAO> retListSubj, String qSub,
        List<ResultDAO> retListPredLookUp, List<ResultDAO> retListPredSearch, String qPred, List<ResultDAO> retListObj,
        String qObj, double minsim)
    {

        List<SuggestedFactDAO> retList = new ArrayList<SuggestedFactDAO>();

        // take all the top candidates
        List<String> subs = new ArrayList<String>();
        List<String> preds = new ArrayList<String>();
        List<String> objs = new ArrayList<String>();

        List<String> tSubs = new ArrayList<String>();
        List<String> tPreds = new ArrayList<String>();
        List<String> tObjs = new ArrayList<String>();

        // check if minimum similarity is 0
        minsim = (minsim != 0) ? minsim : Constants.SIMILARITY;

        // iterate the results to fetch the top scoring matches to generate a possible set of facts
        for (ResultDAO dao : retListSubj) {
            if (dao.getScore() >= minsim) {
                subs.add(dao.getFieldURI());
            } else {
                // break;
            }
        }

        for (ResultDAO dao : retListObj) {
            if (dao.getScore() >= minsim) {
                objs.add(dao.getFieldURI());
            } else {
                // break;
            }
        }

        for (ResultDAO dao : retListPredSearch) {
            if (dao.getScore() >= minsim) {
                preds.add(dao.getFieldURI());
            } else {
                // break;
            }
        }

        for (ResultDAO dao : retListPredLookUp) {
            if (dao.getScore() == 100.00) {
                preds.add(dao.getFieldURI());
            } else {
                break;
            }
        }

        /*
         * tSubs = createPossibleSubs(preds, objs, qSub); tObjs = createPossibleObs(subs, preds, qObj);
         */
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
        return retList;
    }

    private static List<String> createPossibleObs(List<String> listArg1, List<String> listArg2, String qObj)
    {
        String sparqlQuery = null;
        ResultSet results = null;

        List<String> possibleObs = new ArrayList<String>();
        int globalScore = Integer.MAX_VALUE;

        for (String firstArg : listArg1) {
            for (String secondArg : listArg2) {
                sparqlQuery = "select ?arg where {<" + firstArg + "> <" + secondArg + "> ?arg}";
                logger.debug(sparqlQuery);

                results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);

                while (results.hasNext()) {
                    String obj = results.nextSolution().get("arg").toString();
                    String tempObj = obj.substring(obj.lastIndexOf("/") + 1, obj.length());

                    int levenScore = StringUtils.getLevenshteinDistance(tempObj.toLowerCase(), qObj.toLowerCase());

                    if (levenScore < globalScore) {
                        globalScore = levenScore;
                        logger.info("adding obj = " + obj);
                        if (possibleObs.size() > 0)
                            possibleObs.clear();

                        possibleObs.add(obj);
                    }
                }
            }
        }

        return possibleObs;
    }

    private static List<String> createPossibleSubs(List<String> arg1, List<String> arg2, String qSub)
    {
        String sparqlQuery = null;
        ResultSet results = null;

        List<String> possibleSubs = new ArrayList<String>();
        int globalScore = Integer.MAX_VALUE;

        for (String pred : arg1) {
            for (String obj : arg2) {
                sparqlQuery = "select ?arg where {?arg <" + pred + "> <" + obj + ">}";
                logger.debug(sparqlQuery);

                results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);

                while (results.hasNext()) {
                    String sub = results.nextSolution().get("arg").toString();
                    String tempSub = sub.substring(sub.lastIndexOf("/") + 1, sub.length());

                    // int localScore = tempSub.toLowerCase().indexOf(qSub.toLowerCase());
                    int levenScore = StringUtils.getLevenshteinDistance(tempSub.toLowerCase(), qSub.toLowerCase());

                    if (levenScore < globalScore) {
                        globalScore = levenScore;
                        logger.info("adding sub = " + sub);
                        if (possibleSubs.size() > 0)
                            possibleSubs.clear();

                        possibleSubs.add(sub);
                    }

                }
            }
        }

        return possibleSubs;
    }

}

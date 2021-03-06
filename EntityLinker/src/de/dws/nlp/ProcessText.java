/**
 * 
 */

package de.dws.nlp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import de.dws.helper.util.Constants;
import de.dws.nlp.dao.FreeFormFactDao;
import de.dws.nlp.dao.SentenceDao;
import de.dws.nlp.dao.WikiDao;
import de.unimannheim.informatik.dws.pipeline.en.EnglishSentenceSplitter;

/**
 * This class performs all the language processing on pieces of text.
 * 
 * @author Arnab Dutta
 */
public class ProcessText {

    // define Logger
    static Logger logger = Logger.getLogger(ProcessText.class.getName());

    // the text chunk to process
    private String textChunk;

    // Splitter instance
    private EnglishSentenceSplitter splitter;

    /**
     * @param textChunk
     */
    public ProcessText(String textChunk) {
        this.textChunk = textChunk;

        // alter text [3] to text
        this.textChunk = this.textChunk.replaceAll(" *\\[\\d+\\] *", " ");

        splitter = EnglishSentenceSplitter.getInstance();
    }

    public String[] getSentences() {
        return splitter.splitSentences(this.textChunk);
    }

    /**
     * find in the text piece (splitted by sentences) if there is some
     * information involving the possible surface forms of subject and object
     * pair
     * 
     * @param source
     * @param rel
     * @param sentencesInText
     * @param listSubjSurfaceForms
     * @param listObjectSurfaceForms
     * @return List of {@link SentenceDao}
     */
    public List<SentenceDao> fetchMatchingSentences(WikiDao wikiDao, String[] sentencesInText
            ) {

        List<SentenceDao> listSentenceDao = new ArrayList<SentenceDao>();

        List<FreeFormFactDao> listExtractedFactsDao = new ArrayList<FreeFormFactDao>();
        // iterate the possible surface forms and over the sentences.
        // complexity !! :(
        for (String sentence : sentencesInText) {
            for (String possibleSubject : wikiDao.getListSubjSurfaceForms()) {
                for (String possibleObject : wikiDao.getListObjectSurfaceForms()) {
                    // if the sentence contains the subject and object

                    /*
                     * if (sentence.indexOf("Cruise was born in Syracuse, Ne")
                     * != -1 && possibleSubject.indexOf("Cruise") != -1 &&
                     * possibleObject.indexOf("Syracuse") != -1) {
                     * System.out.println(); }
                     */

                    listExtractedFactsDao = filterSentence(sentence, wikiDao.getPredicate(),
                            possibleSubject,
                            possibleObject);

                    if (listExtractedFactsDao.size() > 0) {

                        logger.debug(sentence + "  ===>> " + possibleSubject + ", "
                                + possibleObject);
                        listSentenceDao.add(new SentenceDao(possibleSubject, possibleObject, null,
                                sentence, wikiDao.getPageTitle()));
                    }
                }
            }
        }
        return listSentenceDao;
    }

    /**
     * critical function which determines if a sentence goes into a possible
     * relationship bearing candidate sentence.
     * 
     * @param sentence original sentence
     * @param possibleSubject surface form of subject
     * @param possibleObject surface form of object
     * @param possibleObject2
     * @return a boolean answer
     */
    private List<FreeFormFactDao> filterSentence(String sentence, String relation,
            String possibleSubject,
            String possibleObject) {

        List<FreeFormFactDao> listExtractedFactsDao = new ArrayList<FreeFormFactDao>();

        // holds the bunch of words defining the relationship
        StringBuffer relDefination = new StringBuffer();

        int wordCount = 0;
        int loopCtr = 0;

        sentence = sentence.toLowerCase();
        possibleObject = possibleObject.toLowerCase();
        possibleSubject = possibleSubject.toLowerCase();

        // first check if sentence contains the values
        boolean contains = sentence.indexOf(possibleSubject) != -1
                && sentence.indexOf(possibleObject) != -1;

        // omit anything more than certain distance apart..
        boolean wordGap = false;
        boolean predMatch = false;

        if (contains) {
            int subPos = sentence.indexOf(possibleSubject);
            int objPos = sentence.indexOf(possibleObject);

            int k;
            // sometimes the subject can follow the occurrence of the object
            if (subPos > objPos)
            {
                loopCtr = objPos + possibleObject.length();
                k = subPos - 1;
            } else {
                loopCtr = subPos + possibleSubject.length();
                k = objPos - 1;
            }
            for (; loopCtr < k; loopCtr++)
            {
                relDefination.append(sentence.charAt(loopCtr));

                if (sentence.charAt(loopCtr) == ' ')
                    wordCount++;
            }
            // check if they are valid words distance apart
            wordGap = (wordCount <= Constants.WORD_GAP) ? true : false;

            // ************* property matching ****************************
            // check if they are in a relationship as defined by the DBPedia
            // fact
            // very crude..need something better
            predMatch = validPredicate(relDefination, relation);

        }

        // iff all are satisfied, this is a valid uncertain fact.
        boolean flag = contains && wordGap && predMatch;

        if (flag)
            listExtractedFactsDao.add(new FreeFormFactDao(possibleSubject, possibleObject,
                    relDefination.toString()));

        return listExtractedFactsDao;
    }

    /**
     * checks if the bag of words represents a valid predicate as given
     * 
     * @param relationBagOfWords
     * @param dbPediaRelation
     * @return
     */
    private boolean validPredicate(StringBuffer relationBagOfWords, String dbPediaRelation) {
        if (relationBagOfWords.indexOf(dbPediaRelation) != -1)
            return true;

        String aftrStopWords = new StopWords().removeStopWords(relationBagOfWords.toString());

        String stemmedSentnc = runStemmer(aftrStopWords);
        String rel = runStemmer(breakWords(dbPediaRelation));

        // logger.info(stemmedSentnc + "  ----  " + aftrStopWords + " ----  " +
        // relationBagOfWords);
        // logger.info("Comparing " + stemmedSentnc + "  ****  " + rel );

        AbstractStringMetric metric = new CosineSimilarity();

        if (stemmedSentnc != null && rel != null && stemmedSentnc.length() > 0 && rel.length() > 0) {
            float result = metric.getSimilarity(stemmedSentnc.toLowerCase(),
                    rel.toLowerCase());

            if (result > 0.1) {
                // logger.info(relationBagOfWords + " ---- " + dbPediaRelation +
                // " -> " + result);
                return true;
            }
        }

        return false;
    }

    private String breakWords(String string) {

        String[] r = string.split("(?=\\p{Upper})");
        StringBuffer buf = new StringBuffer();

        for (String s : r) {
            buf.append(s + " ");
        }
        return buf.toString();
    }

    /**
     * @param stemmer
     * @param input
     * @param stemmer
     * @return
     */
    public String runStemmer(String input) {
        String val = null;
        PorterStemmer stemmer = new PorterStemmer();
        /*
         * stemmer.setCurrent(input); stemmer.stem(); return
         * stemmer.getCurrent();
         */
        for (int i = 0; i < input.length(); i++) {

            char ch = input.charAt(i);

            if (Character.isLetter((char) ch)) {
                stemmer.add(Character.toLowerCase((char) ch));
            }
            else {
                stemmer.stem();
                val = stemmer.toString();
                stemmer.reset();
                if (ch < 0)
                    break;

            }
        }
        return val;

    }

}

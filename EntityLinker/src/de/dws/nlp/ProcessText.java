/**
 * 
 */

package de.dws.nlp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.dws.nlp.dao.SentenceDao;
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
     * @param sentencesInText
     * @param listSubjSurfaceForms
     * @param listObjectSurfaceForms
     * @return List of {@link SentenceDao}
     */
    public List<SentenceDao> fetchMatchingSentences(String source, String[] sentencesInText,
            List<String> listSubjSurfaceForms,
            List<String> listObjectSurfaceForms) {

        List<SentenceDao> listSentenceDao = new ArrayList<SentenceDao>();

        // iterate the possible surface forms and over the sentences.
        // complexity !! :(
        for (String sentence : sentencesInText) {
            for (String possibleSubject : listSubjSurfaceForms) {
                for (String possibleObject : listObjectSurfaceForms) {
                    // if the sentence contains the subject and object
                    if ((sentence.indexOf(possibleSubject) != -1 && sentence
                            .indexOf(possibleObject) != -1)) {
                        logger.debug(sentence + "  ===>> " + possibleSubject + ", "
                                + possibleObject);
                        listSentenceDao.add(new SentenceDao(possibleSubject, possibleObject, null,
                                sentence, source));
                    }
                }
            }
        }
        return listSentenceDao;
    }
}

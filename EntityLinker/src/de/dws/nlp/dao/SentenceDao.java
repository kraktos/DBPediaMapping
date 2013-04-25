/**
 * 
 */

package de.dws.nlp.dao;

/**
 * this consists the information for a particular sentence, its content,
 * matching subject and object
 * 
 * @author Arnab Dutta
 */
public class SentenceDao {
    // surface form of the subject
    private String subj;

    // surface form of the object
    private String obj;

    private String rel;

    // matched sentence
    private String sentence;

    // source page where this came from
    private String sourcePage;

    /**
     * @param subj
     * @param obj
     * @param rel
     * @param sentence
     * @param sourcePage
     */
    public SentenceDao(String subj, String obj, String rel, String sentence, String sourcePage) {
        this.subj = subj;
        this.obj = obj;
        this.rel = rel;
        this.sentence = sentence;
        this.sourcePage = sourcePage;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SentenceDao [" + (subj != null ? "subj=" + subj + ", " : "")
                + (obj != null ? "obj=" + obj + ", " : "")
                + (rel != null ? "rel=" + rel + ", " : "")
                + (sentence != null ? "sentence=" + sentence + ", " : "")
                + (sourcePage != null ? "sourcePage=" + sourcePage : "") + "]";
    }

}

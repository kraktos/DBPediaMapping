/**
 * 
 */

package de.dws.nlp.dao;

import java.util.List;

/**
 * Class representing the data unit for a particular wikipedia page
 * 
 * @author Arnab Dutta
 */
public class WikiDao {

    /**
     * title of the the wiki page
     */
    private String pageTitle;

    /**
     * surface forms of the subject in concern
     */
    private List<String> listSubjSurfaceForms;

    /**
     */
    private List<String> listObjectSurfaceForms;

    /**
     * Text content of this page
     */
    private String content;

    /**
     * original Subject
     */
    private String subject;

    /**
     * original predicate
     */
    private String predicate;

    /**
     * original object
     */
    private String object;

    /**
     * @param pageTitle Title of Wikipedia article. 
     * @param listSubjSurfaceForms Surface forms of the subject in concern
     * @param listObjectSurfaceForms Surface forms of the object in concern
     * @param content text content in the particular wiki page. Can be null
     * @param subject subject in concern
     * @param predicate relation in concern
     * @param object object in concern
     */
    public WikiDao(String pageTitle, List<String> listSubjSurfaceForms,
            List<String> listObjectSurfaceForms, String content, String subject, String predicate,
            String object) {
        this.pageTitle = pageTitle;
        this.listSubjSurfaceForms = listSubjSurfaceForms;
        this.listObjectSurfaceForms = listObjectSurfaceForms;
        this.content = content;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * @return the pageTitle
     */
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * @return the object
     */
    public String getObject() {
        return object;
    }

    /**
     * @param pageTitle the pageTitle to set
     */
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * @param object the object to set
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * @return the listSubjSurfaceForms
     */
    public List<String> getListSubjSurfaceForms() {
        return listSubjSurfaceForms;
    }

    /**
     * @return the listObjectSurfaceForms
     */
    public List<String> getListObjectSurfaceForms() {
        return listObjectSurfaceForms;
    }

    /**
     * @param listSubjSurfaceForms the listSubjSurfaceForms to set
     */
    public void setListSubjSurfaceForms(List<String> listSubjSurfaceForms) {
        this.listSubjSurfaceForms = listSubjSurfaceForms;
    }

    /**
     * @param listObjectSurfaceForms the listObjectSurfaceForms to set
     */
    public void setListObjectSurfaceForms(List<String> listObjectSurfaceForms) {
        this.listObjectSurfaceForms = listObjectSurfaceForms;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WikiDao [pageTitle=" + pageTitle + ", listSubjSurfaceForms=" + listSubjSurfaceForms
                + ", listObjectSurfaceForms=" + listObjectSurfaceForms + ", content=" + content
                + ", subject=" + subject + ", predicate=" + predicate + ", object=" + object + "]";
    }

}

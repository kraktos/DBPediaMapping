/**
 * 
 */
package de.dws.nlp.dao;

/**
 * @author Arnab Dutta
 *
 */
public class SurfaceFormDao {

    private String form;
    private String uri;
    private int count;
    /**
     * @param form
     * @param uri
     * @param count
     */
    public SurfaceFormDao(String form, String uri, int count) {
        this.form = form;
        this.uri = uri;
        this.count = count;
    }
    /**
     * @return the form
     */
    public String getForm() {
        return form;
    }
    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
    /**
     * @param form the form to set
     */
    public void setForm(String form) {
        this.form = form;
    }
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }
    
    
}

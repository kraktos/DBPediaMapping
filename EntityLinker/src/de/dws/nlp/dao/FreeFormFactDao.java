/**
 * 
 */

package de.dws.nlp.dao;

/**
 * Represents a dao which is a natural language representation of a fact.
 * Something like, "[Einstein] [fell in love with] [mileva]"
 * 
 * @author Arnab Dutta
 */
public class FreeFormFactDao {

    /**
     * surface form of subject
     */
    private String surfaceSubj;

    /**
     * surface form of object
     */
    private String surfaceObj;

    /**
     * a sentence representing the relation
     */
    private String relationship;

    /**
     * @param surfaceSubj
     * @param surfaceObj
     * @param relationship
     */
    public FreeFormFactDao(String surfaceSubj, String relationship, String surfaceObj ) {
        this.surfaceSubj = surfaceSubj;
        this.surfaceObj = surfaceObj;
        this.relationship = relationship;
    }

    /**
     * @return the surfaceSubj
     */
    public String getSurfaceSubj() {
        return surfaceSubj;
    }

    /**
     * @return the surfaceObj
     */
    public String getSurfaceObj() {
        return surfaceObj;
    }

    /**
     * @return the relationship
     */
    public String getRelationship() {
        return relationship;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((relationship == null) ? 0 : relationship.hashCode());
        result = prime * result + ((surfaceObj == null) ? 0 : surfaceObj.hashCode());
        result = prime * result + ((surfaceSubj == null) ? 0 : surfaceSubj.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FreeFormFactDao))
            return false;
        FreeFormFactDao other = (FreeFormFactDao) obj;
        if (relationship == null) {
            if (other.relationship != null)
                return false;
        } else if (!relationship.equals(other.relationship))
            return false;
        if (surfaceObj == null) {
            if (other.surfaceObj != null)
                return false;
        } else if (!surfaceObj.equals(other.surfaceObj))
            return false;
        if (surfaceSubj == null) {
            if (other.surfaceSubj != null)
                return false;
        } else if (!surfaceSubj.equals(other.surfaceSubj))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (surfaceSubj != null) {
            builder.append(surfaceSubj);
            builder.append(", ");
        }
        if (surfaceObj != null) {

            builder.append(surfaceObj);
            builder.append(", ");
        }
        if (relationship != null) {

            builder.append(relationship);
        }
        builder.append("]");
        return builder.toString();
    }

}

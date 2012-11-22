/**
 * 
 */
package com.mapper.pojo;

/**
 * @author Arnab Dutta
 * 
 */
public class Fact {

	private String entity;
	private String relation;
	private String value;
	private String aPrioriProbability;

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getaPrioriProbability() {
		return aPrioriProbability;
	}

	public void setaPrioriProbability(String aPrioriProbability) {
		this.aPrioriProbability = aPrioriProbability;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((aPrioriProbability == null) ? 0 : aPrioriProbability
						.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result
				+ ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Fact))
			return false;
		Fact other = (Fact) obj;
		if (aPrioriProbability == null) {
			if (other.aPrioriProbability != null)
				return false;
		} else if (!aPrioriProbability.equals(other.aPrioriProbability))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Fact [entity=" + entity + ", relation=" + relation + ", value="
				+ value + ", aPrioriProbability=" + aPrioriProbability + "]";
	}

	
	
	
}

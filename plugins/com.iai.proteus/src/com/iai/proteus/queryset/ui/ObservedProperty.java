package com.iai.proteus.queryset.ui;

public class ObservedProperty {

	private String observedProperty;

	public ObservedProperty(String observedProperty) {
		this.observedProperty = observedProperty;
	}

	public String getObservedProperty() {
		return observedProperty;
	}

	@Override
	public String toString() {
		return observedProperty;
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
				+ ((observedProperty == null) ? 0 : observedProperty.hashCode());
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
		if (getClass() != obj.getClass())
			return false;
		ObservedProperty other = (ObservedProperty) obj;
		if (observedProperty == null) {
			if (other.observedProperty != null)
				return false;
		} else if (!observedProperty.equals(other.observedProperty))
			return false;
		return true;
	}

}

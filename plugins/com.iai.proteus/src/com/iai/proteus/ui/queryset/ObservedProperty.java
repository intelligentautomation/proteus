/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.queryset;

/**
 * Model object for observed properties 
 * 
 * @author Jakob Henriksson
 *
 */
public class ObservedProperty {

	private String observedProperty;
	private boolean checked; 

	/**
	 * Constructor 
	 * 
	 * @param observedProperty
	 */
	public ObservedProperty(String observedProperty) {
		this.observedProperty = observedProperty;
		checked = false;
	}
	
	public String getObservedProperty() {
		return observedProperty;
	}
	
	/**
	 * @param observedProperty the observedProperty to set
	 */
	public void setObservedProperty(String observedProperty) {
		this.observedProperty = observedProperty;
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
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

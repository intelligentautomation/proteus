/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.queryset;

import java.util.ArrayList;
import java.util.List;

/**
 * Model object for categorizes that are used for organizing 
 * observed properties 
 * 
 * @author Jakob Henriksson
 *
 */
public class Category {

	private String name;
	private List<ObservedProperty> observedProperties;

	/**
	 * Constructor 
	 * 
	 * @param name
	 */
	public Category(String name) {
		this.name = name;
		observedProperties = new ArrayList<ObservedProperty>();
	}

	public String getName() {
		return name;
	}

	public void setObservedProperties(List<ObservedProperty> observedProperties) {
		this.observedProperties = observedProperties;
	}

	public List<ObservedProperty> getObservedProperties() {
		return observedProperties;
	}

	@Override
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Category other = (Category) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}

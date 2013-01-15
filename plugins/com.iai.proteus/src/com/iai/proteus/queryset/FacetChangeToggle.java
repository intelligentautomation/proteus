/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;


/**
 * Specifies an facet change 
 * 
 * @author Jakob Henriksson 
 *
 */
public class FacetChangeToggle {
	
	private Facet facet;
	private boolean status;
	private String value; 
	
	/**
	 * Constructor 
	 * 
	 * @param facet
	 * @param status
	 * @param value
	 */
	public FacetChangeToggle(Facet facet, boolean status, String value) {
		this.facet = facet;
		this.status = status;
		this.value = value; 
	}

	/**
	 * 
	 * @return the facet
	 */
	public Facet getFacet() {
		return facet;
	}

	/**
	 * 
	 * @return the status
	 */
	public boolean getStatus() {
		return status;
	}

	/**
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facet == null) ? 0 : facet.hashCode());
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
		if (getClass() != obj.getClass())
			return false;
		FacetChangeToggle other = (FacetChangeToggle) obj;
		if (facet != other.facet)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}

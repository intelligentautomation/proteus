/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import java.util.Map;

/**
 * Holding information on available facet options
 *
 * @author Jakob Henriksson
 *
 */
public class FacetData {

	private Map<String, Integer> observationProperties;

	/**
	 * Constructor
	 *
	 * @param observationProperties
	 */
	public FacetData(Map<String, Integer> observationProperties) {
		this.observationProperties = observationProperties;
	}

	/**
	 *
	 * @return the observationProperties
	 */
	public Map<String, Integer> getObservationProperties() {
		return observationProperties;
	}

	/**
	 * Returns the number of observed properties
	 *
	 * @return
	 */
	public int getNoProperties() {
		if (observationProperties != null)
			return observationProperties.keySet().size();
		return 0;
	}

	/**
	 * Returns the number of matched sensor offerings
	 *
	 * @return
	 */
	public int getNoMatches() {
		if (observationProperties != null) {
			return observationProperties.keySet().size();
		}
		return 0;
	}

}

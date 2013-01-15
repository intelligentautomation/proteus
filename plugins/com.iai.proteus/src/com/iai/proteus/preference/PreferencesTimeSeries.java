/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.preference;

import java.io.Serializable;

/**
 * Preferences Bean object for Time Series 
 * 
 * @author Jakob Henriksson
 *
 */
public class PreferencesTimeSeries implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Time series 
	 */
	private int maxDefaultRangeVariables; 
	

	/**
	 * Constructor 
	 * 
	 */
	public PreferencesTimeSeries() {
		maxDefaultRangeVariables = 5; 
	}
	
	public void setMaxDefaultRangeVariables(int max) {
		this.maxDefaultRangeVariables = max;
	}
	
	public int getMaxDefaultRangeVariables() {
		return maxDefaultRangeVariables;
	}	
}

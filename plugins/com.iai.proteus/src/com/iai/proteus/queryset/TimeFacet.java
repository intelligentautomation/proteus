/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

public enum TimeFacet {

	ALL("No restriction"),
	ONEDAY("24 hours"),
	ONEWEEK("1 week"),
	CUSTOM("Custom");

	private String time;

	private TimeFacet(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return time;
	}

	public TimeFacet parse(String value) {
		for (TimeFacet facet : TimeFacet.values()) {
			if (facet.toString().equals(value))
				return facet;
		}
		// default
		return null;
	}

}

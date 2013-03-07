/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.services;

import java.io.Serializable;

/**
 * Enumerates valid service types 
 * 
 * @author Jakob Henriksson
 *
 */
public enum ServiceType implements Serializable {

	SOS("SOS"),
	WMS("WMS"); 
	
	private String name;
	
	/**
	 * Constructor 
	 * 
	 * @param name
	 */
	private ServiceType(String name) {
		this.name = name;
	}
	
	/**
	 * Parse string into a enumeration value 
	 * 
	 * @param type
	 * @return
	 */
	public static ServiceType parse(String type) {
		for (ServiceType st : ServiceType.values()) {
			if (type.equalsIgnoreCase(st.toString())) {
				return st;
			}
		}
		throw new IllegalArgumentException("No constant with text " + 
				type + " found");
	}		
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Returns a longer name for the service 
	 * 
	 * @return
	 */
	public String toStringLong() {
		String str = "";
		switch (this) {
		case SOS: 
			str = "Sensor Observation Service (" + toString() + ")";
			break;
		case WMS: 
			str = "Web Map Service (" + toString() + ")";
			break;
		}
		// default
		return str;
	}
}

package com.iai.proteus.model.services;

import java.io.Serializable;

/**
 * Enumerates valid service types 
 * 
 * @author Jakob Henriksson
 *
 */
public enum ServiceType implements Serializable {

	CSW("CSW"), 
	SOS("SOS"),
	SAS("SAS"), 
	WMS("WMS"), 
	WFS("WFS"), 
	WCS("WCS");
	
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
}

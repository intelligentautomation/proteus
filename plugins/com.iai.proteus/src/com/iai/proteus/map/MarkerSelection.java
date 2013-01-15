/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a World Wind marker selection 
 * 
 * @author Jakob Henriksson
 *
 */
public class MarkerSelection {

	List<SensorOfferingMarker> markers;
	
	public MarkerSelection() {
		markers = new ArrayList<SensorOfferingMarker>(); 
	}
	
	public void add(SensorOfferingMarker marker) {
		markers.add(marker);
	}
	
	public List<SensorOfferingMarker> getSelection() {
		return markers; 
	}
}

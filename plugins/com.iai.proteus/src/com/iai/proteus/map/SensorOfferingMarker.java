/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.render.Renderable;

import java.awt.Color;

import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.model.services.Service;

/**
 * A renderable marker representing a sensor offering 
 * 
 * @author Jakob Henriksson
 *
 */
public interface SensorOfferingMarker extends Renderable {
	
	/**
	 * Returns the observation data 
	 * 
	 * @return
	 */
	public SensorOffering getSensorOffering();
	
	/**
	 * Sets the color of this marker 
	 * 
	 * @param color
	 */
	public void setColor(Color color);
	
	/**
	 * Returns the color of this marker 
	 * 
	 * @return
	 */
	public Color getColor();
	
	/**
	 * Sets the service for the marker
	 *  
	 * @param service
	 */
	public void setService(Service service);
	
	/**
	 * Returns the service for the marker 
	 * 
	 * @return
	 */
	public Service getService();

}

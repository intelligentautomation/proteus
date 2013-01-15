/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceSector;

import java.awt.Color;

import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.model.services.Service;

/**
 * A renderable region representing a sensor offering 
 * 
 * @author Jakob Henriksson
 *
 */
public class SensorOfferingRegion extends SurfaceSector
	implements SensorOfferingMarker
{
	
	private Service service; 
	private SensorOffering data;
	private Color color; 
	
	/**
	 * Constructor 
	 * 
	 * @param offering
	 * @param sector
	 * @param attrs
	 */
	public SensorOfferingRegion(SensorOffering offering, Sector sector, 
			ShapeAttributes attrs) 
	{
		super(sector);
		
		setAttributes(attrs);
		
		this.data = offering; 
	}
	
	/**
	 * Returns the observation data 
	 * 
	 * @return
	 */
	public SensorOffering getSensorOffering() {
		return data;
	}
	
	/**
	 * Sets the color of this marker 
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * Returns the color of this marker 
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Sets the service for the marker
	 *  
	 * @param service
	 */
	public void setService(Service service) {
		this.service = service; 
	}
	
	/**
	 * Returns the service for the marker 
	 * 
	 * @return
	 */
	public Service getService() {
		return service;
	}	

}

package com.iai.proteus.map;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;

import java.awt.Color;

import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.model.services.Service;

/**
 * A specialized WorldWind marker 
 * 
 */
public class SensorOfferingPlacemark extends PointPlacemark 
	implements SensorOfferingMarker 
{

	private Service service; 
	private SensorOffering data;
	private Color color; 

	/**
	 * Constructor 
	 * 
	 * @param offering
	 * @param position
	 * @param attrs
	 */
	public SensorOfferingPlacemark(SensorOffering offering, 
			Position position, PointPlacemarkAttributes attrs) 
	{
		super(position);
		
		setAttributes(attrs);
		setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		
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

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;

import java.awt.Color;

import com.iai.proteus.communityhub.apiv1.Alert;

/**
 * A specialized WorldWind marker 
 * 
 */
public class AlertItemPlacemark extends PointPlacemark { 

	private Alert data; 
	private Color color; 

	/**
	 * Constructor 
	 * 
	 * @param position
	 * @param attrs
	 */
	public AlertItemPlacemark(Alert feedItem, Position position, PointPlacemarkAttributes attrs) 
	{
		super(position);
		
		setAttributes(attrs);
		setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		
		this.data = feedItem;
	}

	/**
	 * Returns the feed item
	 * 
	 * @return
	 */
	public Alert getAlert() {
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
	
}

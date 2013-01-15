/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views.layers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;


public class TimedPointPlacemark extends PointPlacemark
{
	protected long time;

	public TimedPointPlacemark(Position position, PointPlacemarkAttributes attributes, long time)
	{
		super(position);
		setAttributes(attributes);
		this.time = time;
	}
	
	public long getTime() {
		return time; 
	}
}


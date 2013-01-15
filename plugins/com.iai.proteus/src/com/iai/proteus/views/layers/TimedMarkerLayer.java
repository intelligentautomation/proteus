/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views.layers;

import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A lot of the code here was taken from the World Wind MarkersOrder example
 * 
 * 
 * @author Jakob Henriksson
 *
 */
public class TimedMarkerLayer extends MarkerLayer
{
	protected long latestTime = 0;
	protected long timeScale = (long) 60e3 * 120;   // 10 minutes between attributes ramp steps
	
	protected static final int RAMP_VALUES = 32;
	// ramp 
	protected static MarkerAttributes[] attrsRampMono = new MarkerAttributes[RAMP_VALUES];
	
	// 24h color set
	protected static MarkerAttributes[] attrsHours = new MarkerAttributes[24];

	static
	{
		for (int i = 0; i < RAMP_VALUES; i++)
		{
			float opacity = Math.max(1f - (float) i / RAMP_VALUES, .2f);
			attrsRampMono[i] = new BasicMarkerAttributes(new Material(Color.RED),
					BasicMarkerShape.SPHERE, opacity, 10, 5);
		}
	}
	
	
	static
	{
		for (int i = 0; i < 24; i++)        // 0...23
		{
			attrsHours[i] = new BasicMarkerAttributes(
					new Material(computeColorForHour(i)),
					BasicMarkerShape.SPHERE, 1, 10, 5);
		}
	}
	
	protected Marker lastHighlit;	
	protected MarkerAttributes[] attrs = attrsRampMono;

	
	/**
	 * Constructor 
	 * 
	 */
	public TimedMarkerLayer() {
		super();
		setMarkers(new ArrayList<Marker>()); 
	}
	
	/**
	 * Constructor 
	 * 
	 * @param markers
	 */
	public TimedMarkerLayer(Iterable<Marker> markers) {
		super();
		setMarkers(markers);
	}

	public void draw(DrawContext dc, java.awt.Point pickPoint)
	{
		if (!dc.isPickingMode())
		{
			Calendar cal = Calendar.getInstance();
			for (Marker marker1 : getMarkers())
			{
				TimedMarker marker = (TimedMarker) marker1;
				int i = 0;
//				switch (colorMode)
//				{
//				case COLOR_MODE_RAMP:
					i = Math.min((int) ((latestTime - marker.time) / timeScale), attrs.length - 1);
//					break;
//				case COLOR_MODE_DOW:
//					cal.setTimeInMillis(marker.time);
//					i = cal.get(Calendar.DAY_OF_WEEK) - 1;
//					break;
//				case COLOR_MODE_HOURS:
//					cal.setTimeInMillis(marker.time);
//					i = cal.get(Calendar.HOUR_OF_DAY) % 24;
//					break;
//				}
				if (marker != lastHighlit)
					marker.setAttributes(attrs[i]);
			}
		}
		super.draw(dc, pickPoint);
	}

	public void setLatestTime(long time)
	{
		this.latestTime = time;
	}

	public long getLatestTime()
	{
		return this.latestTime;
	}

	public void setTimeScale(long time)
	{
		this.timeScale = time;
	}

	public long getTimeScale()
	{
		return this.timeScale;
	}

	public static Color computeColorForHour(int hour)
	{
		// Hour from 0 to 23
		// Bias ratio to avoid looping back to red for 23:00
		return Color.getHSBColor((float) hour / 26f, 1f, 1f);
	}
	
	
	@Override
	public void setMarkers(Iterable<Marker> markers) {
		long latestTime = 0; 
		for (Marker m : markers) {
			TimedMarker marker = (TimedMarker) m;
			long time = marker.getTime();
			latestTime = time > latestTime ? time : latestTime;
		}
		setLatestTime(latestTime);
		super.setMarkers(markers);
	}
	
}



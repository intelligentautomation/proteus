package com.iai.proteus.views.layers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;


public class TimedMarker extends BasicMarker
{
	protected long time;

	public TimedMarker(Position position, MarkerAttributes attributes, long time)
	{
		super(position, attributes);
		this.time = time;
	}
	
	public long getTime() {
		return time; 
	}
}


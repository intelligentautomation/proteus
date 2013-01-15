/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;


import com.iai.proteus.common.LatLon;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.model.event.WorkspaceEventType;


/**
 * Models a query to an SOS (Sensor Observation Service) 
 * 
 * @author Jakob Henriksson
 *
 */
public abstract class QuerySos extends Query {
	
	private static final long serialVersionUID = 1L;

	protected SensorOffering sensorOffering;
	
	/**
	 * Default constructor 
	 * 
	 */
	public QuerySos() {
		
	}
	
	/**
	 * Constructor 
	 * 
	 * @param sensorOffering
	 */
	protected QuerySos(SensorOffering sensorOffering) {
		this.sensorOffering = sensorOffering;
	}
	
	
	
	/**
	 * Sets the sensor offering 
	 * 
	 * @param sensorOffering the sensorOffering to set
	 */
	public void setSensorOffering(SensorOffering sensorOffering) {
		this.sensorOffering = sensorOffering;
	}

	/**
	 * Returns the sensor offering 
	 * 
	 * @return
	 */
	public SensorOffering getSensorOffering() {
		return sensorOffering;
	}
		
	/**
	 * Fire event to fly to location 
	 */
	public void fireFlyTo() {
		QueryLayer layer = (QueryLayer)getParent();
		// only fly to location if they layer is active 
		if (layer.isActive()) {
			if (this instanceof QuerySensorOffering) {
				QuerySensorOffering offering = (QuerySensorOffering)this;
				double lat = offering.getSensorOffering().getLowerCornerLat();
				double lon = offering.getSensorOffering().getLowerCornerLong();
				fireEvent(WorkspaceEventType.WORKSPACE_FLY_TO_OFFERING, 
						new LatLon(lat, lon));
			}
		}
	}	
}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

import java.util.ArrayList;
import java.util.Collection;

import com.iai.proteus.common.LatLon;
import com.iai.proteus.communityhub.apiv1.Alert;

/**
 * A layer for displaying alerts 
 * 
 * @author Jakob Henriksson
 *
 */
public class AlertLayer extends RenderableLayer {
	
//	private static final Logger log = Logger.getLogger(AlertLayer.class);
	
	/**
	 * Constructor 
	 * 
	 */
	public AlertLayer() {

	}
	
	/**
	 * Update the alerts in the layer 
	 * 
	 * @param alerts
	 */
	public void setAlerts(Collection<Alert> alerts) {
		// clear renderables
		removeAllRenderables();
		// add new renderables 
		addRenderables(createRenderables(alerts));
	}
	
	/**
	 * Create renderables from alerts 
	 * 
	 * @param alerts
	 * @return
	 */
	private Collection<Renderable> createRenderables(Collection<Alert> alerts) {
		
		Collection<Renderable> shapes = new ArrayList<Renderable>();
		
		for (Alert alert : alerts) {
			
			Double latU = alert.getLatUpper();
			Double lonU = alert.getLonUpper();
			Double latL = alert.getLatLower();
			Double lonL = alert.getLonLower();
			
			if (latU != null && lonU != null && latL != null && lonL != null) {
				LatLon latLon = 
						WorldWindUtils.getCentralPosition(latU, lonU, latL, lonL);
				Position position = WorldWindUtils.getPosition(latLon);
				
				AlertItemPlacemark placemark = 
						new AlertItemPlacemark(alert, 
								position, WorldWindUtils.placemarkAttributes);
	
				shapes.add(placemark);
			}

		}
		
		return shapes;
	}
	
}

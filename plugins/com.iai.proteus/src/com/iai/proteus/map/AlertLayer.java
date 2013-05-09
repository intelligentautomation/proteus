/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.Renderable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
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
	
	private WorldWindow world;
	
	/**
	 * Constructor 
	 * 
	 */
	public AlertLayer(WorldWindow ww) {
		this.world = ww;
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
	
	/**
	 * Create a blurred pattern bitmap
	 * 
	 * (From NASA World Wind AlarmIcons.java demo.) 
	 * 
	 * @param pattern
	 * @param color
	 * @return
	 */
	private BufferedImage createBitmap(String pattern, Color color)
	{
		// Create bitmap with pattern
		BufferedImage image = PatternFactory.createPattern(pattern, new Dimension(128, 128), 0.7f,
				color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
		// Blur a lot to get a fuzzy edge
		image = PatternFactory.blur(image, 13);
		image = PatternFactory.blur(image, 13);
		image = PatternFactory.blur(image, 13);
		image = PatternFactory.blur(image, 13);
		return image;
	}
	
}

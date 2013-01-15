/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javaxt.geospatial.geometry.Point;
import javaxt.rss.Feed;
import javaxt.rss.Item;
import javaxt.rss.Location;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iai.proteus.communityhub.FeedItem;

public class GeoRssLayer extends RenderableLayer {
	
	private static final Logger log = Logger.getLogger(GeoRssLayer.class);
	
	private WorldWindow worldWindow;
	
	/**
	 * Constructor 
	 * 
	 */
	public GeoRssLayer(WorldWindow worldWindow) {
		this.worldWindow = worldWindow;
	}
	
	
	public void useFeed(String feed) {
		this.removeAllRenderables();
		try {
			
			Collection<Renderable> result = parse(feed);
			
			if (result != null) {
				addRenderables(result);
			}
			
			worldWindow.redrawNow();
			
		} catch (WWRuntimeException e) {
			log.error("WW Excpetion: " + e.getMessage());
		}
	}
	
	/**
	 * Parse alert feed 
	 * 
	 * @param feed
	 * @return
	 */
	private Collection<Renderable> parse(String feed) {
		
		Collection<Renderable> shapes = new ArrayList<Renderable>();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document xml = dBuilder.parse(new InputSource(new ByteArrayInputStream(feed.getBytes("utf-8"))));
			
			for (Feed f : new javaxt.rss.Parser(xml).getFeeds()) {
				for (Item item : f.getItems()) {
					Location location = item.getLocation();
					if (location != null) {
						
						Object objGeo = location.getGeometry();
						if (objGeo instanceof Point) {
							Point point = (Point) objGeo;

							/*
							 * NOTE: Despite what it says here:
							 * http://georss.org/simple, it seems that the Point
							 * object treats the order as longitude-latitude
							 * rather than the other way around. Hence the
							 * switched order to get the position 
							 */
							
							Position position = 
									new Position(LatLon.fromDegrees(point.getLongitude(), 
											point.getLatitude()), 0.0);
							
							AlertItemPlacemark placemark = 
									new AlertItemPlacemark(new FeedItem(item), 
											position, WorldWindUtils.placemarkAttributes);
							
							shapes.add(placemark);
						}
						// TODO: add more shapes
					}
				}
			}
			
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException: " + e.getMessage());
		} catch (SAXException e) {
			log.error("SAXException: " + e.getMessage());
		}
		
		return shapes;
	}
	
}

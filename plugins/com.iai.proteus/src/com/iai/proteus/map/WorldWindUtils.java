/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.poi.BasicPointOfInterest;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.iai.proteus.Activator;
import com.iai.proteus.common.sos.SosService;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.ui.UIUtil;

/**
 * Utilities related to World Wind 
 * 
 * @author Jakob Henriksson 
 *
 */
public class WorldWindUtils {
	
	private static final Logger log = Logger.getLogger(WorldWindUtils.class);
	
	public static MarkerAttributes markerAttributes;
	
	public static PointPlacemarkAttributes placemarkAttributes;
	public static ShapeAttributes regionAttributes; 
	
	static {
		/* marker attributes to use */ 
		placemarkAttributes = new PointPlacemarkAttributes();
		placemarkAttributes.setUsePointAsDefaultImage(true);
		placemarkAttributes.setScale(10d);
		placemarkAttributes.setLabelColor("ffffffff");
		placemarkAttributes.setLineColor("ff0000ff");	
		
		/* region attributes */ 
		regionAttributes = new BasicShapeAttributes();
		regionAttributes.setDrawInterior(false);
		regionAttributes.setDrawOutline(true);
		
		markerAttributes = new BasicMarkerAttributes();
		markerAttributes.setMinMarkerSize(10);
	}

	/**
	 * Returns World Wind markers corresponding to the sensor offerings 
	 * of a SOS capabilities document 
	 * 
	 * @param capabilities
	 * @return
	 */
	public static List<Renderable> 
		getCapabilitiesMarkers(SosCapabilities capabilities, Color color) 
	{
		
		// handle error 
		if (capabilities == null) {
			String msg = "The capabilities object was null";
			UIUtil.log(Activator.PLUGIN_ID, msg);
			log.error(msg);
			return new ArrayList<Renderable>(); 
		}
		
		List<Renderable> renderables = new ArrayList<Renderable>();
		
		for (SensorOffering offering : capabilities.getOfferings()) {

			String url = 
					SosUtil.findGetServiceUrl(capabilities, 
							SosService.GET_CAPABILITIES); 
			
			Service service = null;
			
			if (url != null) {
				service = new Service(ServiceType.SOS);
				service.setServiceUrl(url);
				String name = 
						capabilities.getServiceIdentification().getTitle();
				if (name != null)
					service.setName(name);
			}			
			
			/*
			 * Create a sector  
			 */
			if (isRegion(offering)) {
				
				Sector sector = getBoundingBoxSector(offering); 
				
				SensorOfferingRegion region = 
						new SensorOfferingRegion(offering, sector, 
								regionAttributes);
				
				region.setValue(AVKey.DISPLAY_NAME, 
						offering.getGmlId());
				
				if (service != null)
					region.setService(service);
				
				// set color 
				regionAttributes.setOutlineMaterial(new Material(color));
				
				renderables.add(region);
			} 
			/*
			 * Create a place marker 
			 */
			else {
							
				SensorOfferingPlacemark marker = 
						new SensorOfferingPlacemark(offering, getCentralPosition(offering), 
								placemarkAttributes);

				//			marker.setLabelText(offering.getGmlId());
				marker.setValue(AVKey.DISPLAY_NAME, 
						offering.getGmlId());

				if (service != null)
					marker.setService(service);

				// update attributes appropriately
				// (must happen after setting the source!)
				setAttributesFromColor(color, marker);
				
				renderables.add(marker);
			}
		}
		
		return renderables; 
	}
	
	/**
	 * Returns true if the offering has a bounding box that is a region, 
	 * as compared to a point. 
	 * 
	 * @param offering
	 * @return 
	 */
	public static boolean isRegion(SensorOffering offering) {
		Position lower = 
				getPosition(SosUtil.getLowerCornerBoundingBox(offering));
		Position upper = 
				getPosition(SosUtil.getUpperCornerBoundingBox(offering));
		if (lower != null && upper != null) {
			return !lower.equals(upper);
		}
		return false; 
	}
	
	/**
	 * Returns the bounding box sector for the offering
	 * 
	 * @param offering
	 * @return
	 */
	public static Sector getBoundingBoxSector(SensorOffering offering) {
		Position lower = 
				getPosition(SosUtil.getLowerCornerBoundingBox(offering));
		Position upper = 
				getPosition(SosUtil.getUpperCornerBoundingBox(offering));
		if (lower != null && upper != null) {
			return Sector.boundingSector(lower, upper);
		}
		return null; 
	}
	
	/**
	 * Returns the upper position of the sensor offering bounding box 
	 * 
	 * @param offering
	 * @return
	 */
	public static Position getUpperPosition(SensorOffering offering) {
		double lat = offering.getUpperCornerLat();
		double lon = offering.getUpperCornerLong();
		
		return new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0);
	}
	
	/**
	 * Returns the central position of the sensor offering bounding box  
	 * 
	 * @param offering
	 * @return
	 */
	public static Position getCentralPosition(SensorOffering offering) {
		
		double latU = offering.getUpperCornerLat();
		double lonU = offering.getUpperCornerLong();
		double latL = offering.getLowerCornerLat();
		double lonL = offering.getLowerCornerLong();
		
		Sector sector = 
				new Sector(Angle.fromDegrees(latL), 
						Angle.fromDegrees(latU),
						Angle.fromDegrees(lonL),
						Angle.fromDegrees(lonU));

		Position position = new Position(sector.getCentroid(), 0);	
			
		return position; 
	}
	
	/**
	 * Returns the central position of the given bounding box coordinates 
	 * 
	 * @param latU
	 * @param lonU
	 * @param latL
	 * @param lonL
	 * @return
	 */
	public static com.iai.proteus.common.LatLon 
		getCentralPosition(double latU, double lonU, double latL, double lonL) {
		
		Sector sector = 
				new Sector(Angle.fromDegrees(latL), 
						Angle.fromDegrees(latU),
						Angle.fromDegrees(lonL),
						Angle.fromDegrees(lonU));

		Position position = new Position(sector.getCentroid(), 0);
		
		return getLatLon(position);
	}
	
	/**
	 * Returns a point of interest object given LAT and LONG values 
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static PointOfInterest getPointOfInterest(double lat, double lon) {
		Angle angleLat = Angle.fromDegreesLatitude(lat);
		Angle angleLon = Angle.fromDegreesLongitude(lon);
		LatLon latlon = new LatLon(angleLat, angleLon); 
		return new BasicPointOfInterest(latlon); 
	}
	
	/**
	 * Returns a Position given LAT and LONG values 
	 * 
	 * @param lat
	 * @param lon
	 * @param elevation
	 * @return
	 */
	public static Position getPosition(double lat, double lon, double elevation) {
		Angle angleLat = Angle.fromDegreesLatitude(lat);
		Angle angleLon = Angle.fromDegreesLongitude(lon);
		LatLon latlon = new LatLon(angleLat, angleLon); 
		return new Position(latlon, elevation); 
	}
	
	/**
	 * Returns a Position 
	 * 
	 * @param location
	 * @param elevation
	 * @return
	 */
	public static Position getPosition(com.iai.proteus.common.LatLon location, 
			double elevation) 
	{
		if (location != null) {
			Angle angleLat = Angle.fromDegreesLatitude(location.getLat());
			Angle angleLon = Angle.fromDegreesLongitude(location.getLon());		
			LatLon latlon = new LatLon(angleLat, angleLon); 
			return new Position(latlon, elevation);
		} 
		return null; 
	}	
	
	/**
	 * Returns a Position 
	 * 
	 * @param location
	 * @return
	 */
	public static Position getPosition(com.iai.proteus.common.LatLon location) 
	{
		if (location != null) {
			Angle angleLat = Angle.fromDegreesLatitude(location.getLat());
			Angle angleLon = Angle.fromDegreesLongitude(location.getLon());		
			LatLon latlon = new LatLon(angleLat, angleLon); 
			return new Position(latlon, 0.0);
		}
		return null;
	}
	
	/**
	 * Create a @{link com.iai.proteus.common.LatLon} object from a Position
	 * 
	 * @param position
	 * @return
	 */
	public static com.iai.proteus.common.LatLon getLatLon(Position position) {
		if (position != null) {
			double lat = position.getLatitude().getDegrees();
			double lon = position.getLongitude().getDegrees();
			com.iai.proteus.common.LatLon latLon = 
					new com.iai.proteus.common.LatLon(lat, lon);
			return latLon;
		}
		
		// default
		return null;
	}
	
	/**
	 * Sets the appropriate attributes for this marker 
	 * 
	 * @param mapLayer
	 * @param marker
	 */
	public static void setAttributesFromLayer(MapLayer mapLayer, 
			SensorOfferingMarker marker)
	{
		if (marker instanceof SensorOfferingPlacemark) {
			SensorOfferingPlacemark placemark = (SensorOfferingPlacemark) marker;
			PointPlacemarkAttributes attrs = getPlacemarkAttributes(mapLayer);
			attrs.setLineMaterial(new Material(mapLayer.getColor()));
			placemark.setAttributes(attrs);
			marker.setColor(mapLayer.getColor());
		} else {
			SensorOfferingRegion region = (SensorOfferingRegion) marker;
			BasicShapeAttributes attrs = getRegionAttributes(mapLayer);
			region.setAttributes(attrs);
			marker.setColor(mapLayer.getColor());			
		}
		
	}
	
	/**
	 * Returns the default attributes for the given layer  
	 *
	 * @param mapLayer
	 */
	private static PointPlacemarkAttributes getPlacemarkAttributes(MapLayer mapLayer) 
	{
		PointPlacemarkAttributes attr = new 
			PointPlacemarkAttributes(placemarkAttributes);
		// use the color of the source 
		Color color = mapLayer.getColor();
		attr.setImageColor(color);
		return attr; 
	}
	
	/**
	 * Returns the default attributes for the given layer  
	 *
	 * @param mapLayer
	 */
	private static BasicShapeAttributes getRegionAttributes(MapLayer mapLayer) 
	{
		BasicShapeAttributes attr = new BasicShapeAttributes(regionAttributes);
		// use the color of the source 
		Color color = mapLayer.getColor();
		attr.setOutlineMaterial(new Material(color));
		return attr; 
	}	
	
	/**
	 * Sets the appropriate attributes for this marker 
	 * 
	 * @param color
	 * @param marker
	 */
	public static void setAttributesFromColor(Color color, 
			SensorOfferingPlacemark marker) 
	{
		PointPlacemarkAttributes attrs = new 
			PointPlacemarkAttributes(placemarkAttributes);
		// set the color 
		Material material = new Material(color); 
		attrs.setLineMaterial(material);
		marker.setAttributes(attrs);
		marker.setColor(color); 
	}	
	
	/**
	 * Reset attributes for the given marker 
	 * 
	 * @param marker
	 */
	public static void resetAttributes(SensorOfferingPlacemark marker) 
	{
		PointPlacemarkAttributes attr = 
			new PointPlacemarkAttributes(placemarkAttributes);
		attr.setLineMaterial(new Material(marker.getColor()));
//		attr.setImageColor(marker.getColor());
		marker.setAttributes(attr);
	}
	
	/**
	 * Reset attributes for the given marker 
	 * 
	 * @param marker
	 */
	public static void resetAttributes(SensorOfferingRegion marker) 
	{
		BasicShapeAttributes attr = 
			new BasicShapeAttributes(regionAttributes);
		attr.setDrawInterior(false);
		marker.setAttributes(attr);
	}	
	
	/**
	 * Returns True if the marker is within the given sector, false otherwise 
	 * 
	 * @param marker
	 * @param sector
	 * @return
	 */
	public static boolean markerInSection(SensorOfferingPlacemark marker, 
			Sector sector) 
	{
		Position position = marker.getPosition();
		LatLon latlon = 
			new LatLon(position.getLatitude(), position.getLongitude());
		
		return sector.contains(latlon);
	}
	
	/**
	 * Returns True if the Offering is within the given sector, false otherwise 
	 * 
	 * @param offering
	 * @param sector
	 * @return
	 */
	public static boolean offeringInSection(SensorOffering offering, 
			Sector sector) 
	{
		if (isRegion(offering)) {
			
			Sector region = getBoundingBoxSector(offering);
			
			return sector.intersects(region);
			
		} else {
			
			Position position = getCentralPosition(offering);
			LatLon latlon = 
					new LatLon(position.getLatitude(), position.getLongitude());

			return sector.contains(latlon);
		}
	}
	
	/**
	 * Moves World Wind to given location 
	 * 
	 * @param location
	 */
    public static void moveToLocation(View view, PointOfInterest location)
    {
        if (location == null) {
        	log.warn("Position was null, cannot go there"); 
        	return;
        }

        double elevation = view.getEyePosition().getElevation();
        Position position = new Position(location.getLatlon(), elevation);
        
        long timeInMilliseconds = 1000L; 
        OrbitViewInputHandler ovih = 
        	(OrbitViewInputHandler) view.getViewInputHandler();
        ovih.addPanToAnimator(position, view.getHeading(), view.getPitch(), 
        		elevation, timeInMilliseconds, true); 
        ovih.apply();
    }	
	
}

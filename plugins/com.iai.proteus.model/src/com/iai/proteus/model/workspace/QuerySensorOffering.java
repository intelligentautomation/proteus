/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.model.properties.QuerySensorOfferingPropertySource;

/**
 * Represents a sensor offering query 
 * 
 * @author Jakob Henriksson
 *
 */
public class QuerySensorOffering extends QuerySos implements IAdaptable {

	private static final long serialVersionUID = 1L;
	
	private String gmlId;
	
	private transient QuerySensorOfferingPropertySource property; 	
	
	/**
	 * Default constructor 
	 * 
	 */
	public QuerySensorOffering() {
		
	}
	
	/**
	 * Constructor 
	 * 
	 * @param gmlId
	 */
	public QuerySensorOffering(String gmlId) {
		super(new SensorOffering(gmlId));
		this.gmlId = gmlId;
		setName("Offering: " + gmlId);
	}
	
	
			
	/**
	 * Returns the gml:ID
	 * 
	 * @return the gmlId
	 */
	public String getGmlId() {
		return gmlId;
	}

	/**
	 * Sets the gml:ID
	 * 
	 * @param gmlId the gmlId to set
	 */
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}
	
	@Override
	public String toString() {
		return "Offering: " + gmlId;
	}
	
	/**
	 * Serialized to:
	 * 
	 * <query type="SensorOffering" gml:id="[id]">
	 * 	<source>...</source>
	 * 	<gml:boundedBy>
	 * 	  <gml:Envelope>
     *     <gml:lowerCorner>16.03 -107</gml:lowerCorner>
     *     <gml:upperCorner>16.03 -107</gml:upperCorner>
     *    </gml:Envelope>
	 *  </gml:boundedBy>
	 * </query>
	 * 
	 */
	@Override 
	public Element serialize(Document document) {
		Element root = document.createElement("query");
		// attributes
		root.setAttribute("type", "SensorOffering");
		root.setAttribute("gml:id", gmlId);
		// source 
		root.appendChild(getProvenance().serialize(document));
		// gml:boundedBy
		Element boundedBy = 
			document.createElementNS("http://www.opengis.net/gml/3.2", 
					"gml:boundedBy");
		root.appendChild(boundedBy);
		
		Element envelope = 
			document.createElementNS("http://www.opengis.net/gml/3.2", 
					"gml:Envelope");
		boundedBy.appendChild(envelope);
		
		Element lowerCorner = 
			document.createElementNS("http://www.opengis.net/gml/3.2", 
					"gml:lowerCorner");
		String value = Double.toString(sensorOffering.getLowerCornerLat()) + 
			" " + Double.toString(sensorOffering.getLowerCornerLong()); 
		lowerCorner.setTextContent(value);
		envelope.appendChild(lowerCorner);
		
		Element upperCorner = 
			document.createElementNS("http://www.opengis.net/gml/3.2", 
					"gml:upperCorner");
		value = Double.toString(sensorOffering.getUpperCornerLat()) + 
			" " + Double.toString(sensorOffering.getUpperCornerLong()); 
		upperCorner.setTextContent(value);
		envelope.appendChild(upperCorner);
		
		return root; 	
	}
	
	/**
	 * 
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (property == null) {
				// cache the source 
				property = new QuerySensorOfferingPropertySource(this);
			}
			return property;
		}
		// default 
		return null;
	}		
}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a map layer from a WMS service  
 * 
 * @author Jakob Henriksson
 *
 */
public class WmsMapLayer extends MapLayer {

	private static final long serialVersionUID = 1L;
	
	/*
	 * The WMS end-point
	 */
	private String serviceEndpoint;
	
	/*
	 * The title of the WMS layer 
	 */
	private String wmsLayerTitle;
	
	/**
	 * Default constructor 
	 */
	public WmsMapLayer() {
		
	}
	
	/**
	 * @return the serviceEndpoint
	 */
	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	/**
	 * @param serviceEndpoint the serviceEndpoint to set
	 */
	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	/**
	 * Returns the WMS layer title 
	 * 
	 * @return the layerName
	 */
	public String getWmsLayerTitle() {
		return wmsLayerTitle;
	}


	/**
	 * Sets the WMS layer title 
	 * 
	 * @param wmsLayerTitle the layerName to set
	 */
	public void setWmsLayerTitle(String wmsLayerTitle) {
		this.wmsLayerTitle = wmsLayerTitle;
	}


	@Override
	public Element serialize(Document document) {
		return null;
	}

}

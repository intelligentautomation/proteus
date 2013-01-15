/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.services;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.workspace.MapLayer;

/**
 * Represents a map layer from a WMS service  
 * 
 * @author Jakob Henriksson
 *
 */
public class WmsMapLayer extends MapLayer {

	private static final long serialVersionUID = 1L;
	
	/*
	 * The name of the WMS layer 
	 */
	private String wmsLayerName;
	
	/**
	 * Default constructor 
	 */
	public WmsMapLayer() {
		
	}
	
	/**
	 * Returns the WMS layer name 
	 * 
	 * @return the layerName
	 */
	public String getWmsLayerName() {
		return wmsLayerName;
	}


	/**
	 * Sets the WMS layer name 
	 * 
	 * @param wmsLayerName the layerName to set
	 */
	public void setWmsLayerName(String wmsLayerName) {
		this.wmsLayerName = wmsLayerName;
	}


	@Override
	public Element serialize(Document document) {
		return null;
	}

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.map;



/**
 * Model object for WMS maps saved with a Query Set 
 * 
 * @author Jakob Henriksson
 *
 */
public class WmsSavedMap extends WmsMapLayer {
	
	private static final long serialVersionUID = 1L;
	
	private String notes;

	/**
	 * Default constructor
	 * 
	 * @param service
	 * @param mapName
	 */
	public WmsSavedMap() {
		// default 
		notes = "";
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	/**
	 * Create a {@link WmsSavedMap} object from a {@link WmsMapLayer} object
	 * 
	 * @param mapLayer
	 * @return
	 */
	public static WmsSavedMap copy(WmsMapLayer mapLayer) {
		WmsSavedMap map = new WmsSavedMap();
		// from {@link com.iai.proteus.model.Model}
		map.setName(mapLayer.getName());
		// from {@link com.iai.proteus..model.workspace.MapLayer}
		map.setMapId(mapLayer.getMapId());
		map.setActive(mapLayer.isActive());
		map.setColor(mapLayer.getColor());
		// from {@link WmsMapLayer}
		map.setServiceEndpoint(mapLayer.getServiceEndpoint());
		map.setWmsLayerTitle(mapLayer.getWmsLayerTitle());
		return map;
	}
}

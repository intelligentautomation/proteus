/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a query for a map against a Web Map Service (WMS) 
 * 
 * @author Jakob Henriksson
 *
 */
public class QueryWmsMap extends Query {
	
	private static final long serialVersionUID = 1L;
	
	/*
	 * The title of the WMS layer 
	 */
	private String wmsLayerTitle; 

	/*
	 * User's notes on the query/map 
	 */
	private String notes;
	
	/**
	 * Default constructor 
	 * 
	 */
	public QueryWmsMap() {
		wmsLayerTitle = null;
	}
	
	/**
	 * Returns the WMS layer title 
	 * 
	 * @return the wmsLayerTitle
	 */
	public String getWmsLayerTitle() {
		return wmsLayerTitle;
	}


	/**
	 * Sets the WMS layer title 
	 * 
	 * @param wmsLayerTitle the wmsLayerTitle to set
	 */
	public void setWmsLayerTitle(String wmsLayerTitle) {
		this.wmsLayerTitle = wmsLayerTitle;
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

	@Override
	public Element serialize(Document document) {
		return null;
	}

}

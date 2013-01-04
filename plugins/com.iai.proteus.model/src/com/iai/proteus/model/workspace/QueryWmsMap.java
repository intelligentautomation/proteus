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
	 * The name of the WMS layer 
	 */
	private String wmsLayerName; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public QueryWmsMap() {
		wmsLayerName = null;
	}
	
	/**
	 * Returns the WMS layer name 
	 * 
	 * @return the wmsLayerName
	 */
	public String getWmsLayerName() {
		return wmsLayerName;
	}


	/**
	 * Sets the WMS layer name 
	 * 
	 * @param wmsLayerName the wmsLayerName to set
	 */
	public void setWmsLayerName(String wmsLayerName) {
		this.wmsLayerName = wmsLayerName;
	}



	@Override
	public Element serialize(Document document) {
		return null;
	}

}

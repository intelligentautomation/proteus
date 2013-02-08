/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map.wms;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

public class WmsLayerInfo {
	
	public WMSCapabilities caps;
	public AVListImpl params = new AVListImpl();
	
	/**
	 * Constructor 
	 */
	public WmsLayerInfo() {

	}

	public String getTitle() {
		return params.getStringValue(AVKey.DISPLAY_NAME);
	}

	public String getName() {
		return params.getStringValue(AVKey.LAYER_NAMES);
	}

	public String getAbstract() {
		return params.getStringValue(AVKey.LAYER_ABSTRACT);
	}
	
	public WMSCapabilities getCapabilities() {
		return caps;
	}
	
	public AVList getParams() {
		return params;
	}
}


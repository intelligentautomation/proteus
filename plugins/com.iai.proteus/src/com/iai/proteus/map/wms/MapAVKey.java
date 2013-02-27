/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map.wms;

/**
 * Attribute-value keys 
 * 
 * @author Jakob Henriksson
 *
 */
public interface MapAVKey {
	
	// for encoding map IDs
	final String MAP_ID = "com.iai.proteus.avkey.map_id";
	
	// for encoding the WMS end-point URL 
	final String WMS_SERVICE_URL = "com.iai.proteus.avkey.wms_service_url";
	
	// for encoding that a layer is a preview from a WMS
	final String WMS_MAP_PREVIEW = "com.iai.proteus.avkey.wms_map_preview";
	
}

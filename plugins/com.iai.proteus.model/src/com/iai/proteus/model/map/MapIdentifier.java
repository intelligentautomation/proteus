/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.map;

import com.iai.proteus.model.MapId;

/**
 * Interface for map layers that are identified by an ID
 * 
 * @author jhenriksson
 *
 */
public interface MapIdentifier {

	public MapId getMapId();
	public void setMapId(MapId mapId); 

}

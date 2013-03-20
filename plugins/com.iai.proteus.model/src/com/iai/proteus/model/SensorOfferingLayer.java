/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model;

import com.iai.proteus.model.map.IMapLayer;

/**
 * A sensor offering layer 
 * 
 * @author Jakob Henriksson
 *
 */
public class SensorOfferingLayer implements IMapLayer {
	
	// the map ID 
	private MapId mapId;
	
	/*
	 * Determines if the layer is active (visible) on the Map
	 * 
	 * NOTE: Currently we do not save the active state of a map layer. 
	 *       It should be noted that XMLEncoder does not respect the
	 *       transient keyword, instead we have to set the transient 
	 *       status before serializing the object to disk. 
	 */
	private transient boolean active;	
	
	
	/**
	 * Constructor 
	 * 
	 */
	public SensorOfferingLayer() {
		mapId = MapId.generateNewMapId();
		active = false;
	}

	/* (non-Javadoc)
	 * @see com.iai.proteus.model.map.IMapLayer#isActive()
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/* (non-Javadoc)
	 * @see com.iai.proteus.model.map.IMapLayer#setActive(boolean)
	 */
	@Override
	public void setActive(boolean status) {
		this.active = status;
	}

	/* (non-Javadoc)
	 * @see com.iai.proteus.model.map.IMapLayer#getMapId()
	 */
	@Override
	public MapId getMapId() {
		return mapId;
	}

	/* (non-Javadoc)
	 * @see com.iai.proteus.model.map.IMapLayer#setMapId(com.iai.proteus.model.MapId)
	 */
	@Override
	public void setMapId(MapId mapId) {
		this.mapId = mapId; 
	}

}

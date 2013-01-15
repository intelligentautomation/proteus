/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Simply represents a identifier for maps. We use this to map between 
 * MapLayer model objects and actual overlays in the map visualization
 * component (e.g. World Wind)  
 * 
 * @author Jakob Henriksson 
 *
 */
public class MapId implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String mapId;
	
	/**
	 * Default constructor 
	 */
	public MapId() {
		
	}
	
	/**
	 * @return the mapId
	 */
	public String getMapId() {
		return mapId;
	}

	/**
	 * @param mapId the mapId to set
	 */
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}
	
	/**
	 * Generates and returns a new map ID 
	 * 
	 * @return
	 */
	public static MapId generateNewMapId() {
		MapId mapId = new MapId();
		mapId.setMapId(UUID.randomUUID().toString());
		return mapId;
	}
	
	@Override
	public String toString() {
		return mapId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapId == null) ? 0 : mapId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapId other = (MapId) obj;
		if (mapId == null) {
			if (other.mapId != null)
				return false;
		} else if (!mapId.equals(other.mapId))
			return false;
		return true;
	}
	
}

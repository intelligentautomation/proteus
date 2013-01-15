/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;

import java.awt.Color;
import java.util.ArrayList;

import com.iai.proteus.common.Util;
import com.iai.proteus.model.MapId;
import com.iai.proteus.model.Model;
import com.iai.proteus.model.event.WorkspaceEventType;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.WmsMapLayer;

/**
 * A workspace model object that corresponds to a map layer 
 * 
 * @author Jakob Henriksson
 *
 */
public abstract class MapLayer extends Model {
	
	private static final long serialVersionUID = 1L;
	
	/*
	 * This array holds the map IDs that are used to 
	 * synchronize between workspace views and the map views
	 */
	private ArrayList<MapId> mapIds;

	/*
	 * Determines if the layer is active (visible) on the Map
	 * 
	 * NOTE: Currently we do not save the active state of a map layer. 
	 *       It should be noted that XMLEncoder does not respect the
	 *       transient keyword, instead we have to set the transient 
	 *       status before serializing the object to disk. 
	 */
	private transient boolean active;
	
	private Color color; 
	
	/**
	 * Default constructor 
	 */
	public MapLayer() {
		mapIds = new ArrayList<MapId>();
		/*
		 * Generate and add the default map Id for this map layer 
		 */
		mapIds.add(MapId.generateNewMapId()); 
		active = false;
	}
	
	/**
	 * Returns the map IDs for this map layer 
	 * 
	 * @return the map IDs
	 */
	public ArrayList<MapId> getMapIds() {
		ArrayList<MapId> ids = new ArrayList<MapId>();
		for (MapId mapId : mapIds) {
			if (!ids.contains(mapId))
				ids.add(mapId);
		}
		return ids;
	}

	/**
	 * Sets the map IDs for this map layer 
	 * 
	 * @param mapIds the map IDs to set
	 */
	public void setMapIds(ArrayList<MapId> mapIds) {
		for (MapId mapId : mapIds) {
			if (!this.mapIds.contains(mapId))
				this.mapIds.add(mapId);
		}
	}
	
	/**
	 * Adds a map ID to this map layer 
	 * 
	 * @param mapId
	 */
	public void addMapId(MapId mapId) {
		if (!mapIds.contains(mapId)) {
			mapIds.add(mapId);
		}
	}
	
	/**
	 * Returns the 'default' map ID 
	 * 
	 * @return
	 */
	public MapId getDefaultMapId() {
		return mapIds.get(0);
	}
	
	/**
	 * Generates a new map ID 
	 * 
	 * @return
	 */
	public static MapId generateNewMapId() {
		return MapId.generateNewMapId();
	}

	/**
	 * Sets the activity state of this layer 
	 * 
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Returns true if the layer is active, false otherwise 
	 * 
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Marks the layer as active (visible)  
	 * 
	 */
	public void activate() {
		setActive(true);
	}
	
	/**
	 * Marks the layer as inactive (not visible) 
	 */
	public void deactivate() {
		setActive(false);
	}
	
	/**
	 * Sets the color for this map layer 
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color; 
	}
	
	/**
	 * Returns the color for this map layer 
	 * @return
	 */
	public Color getColor() {
		// default color if there is no one specified
		if (color == null)
			return Color.WHITE; 			
		return color;
	}
		

	/**
	 * Fire event stating that a layer was toggled 
	 * 
	 * (sending the relevant model object as an argument to the listener)
	 * 
	 */
	public void fireToggleLayer() {
		if (this instanceof QueryLayer) {
			/*
			 * Fire update to projects root
			 */
			QueryLayer queryLayer = (QueryLayer) this;
			queryLayer.fireEvent(WorkspaceEventType.WORKSPACE_LAYER_TOGGLE);
			
		} else if (this instanceof Service) {
			Service service = (Service) this;
			/*
			 * Fire update event
			 */
			service.fireEvent(WorkspaceEventType.WORKSPACE_LAYER_TOGGLE);
			
		} else if (this instanceof WmsMapLayer) {
			WmsMapLayer mapLayer = (WmsMapLayer) this;
			
			mapLayer.fireEvent(WorkspaceEventType.WORKSPACE_WMS_TOGGLE);
		}
	}
	
	/**
	 * Fire event stating that the layer should be deleted 
	 * 
	 * (sending the relevant model object as an argument to the listener)
	 * 
	 */
	public void fireDeleteLayer() {
		if (this instanceof Service) {
			Service service = (Service)this;
			/*
			 * Fire update to delete layer 
			 */
			service.fireEvent(WorkspaceEventType.WORKSPACE_LAYER_DELETE);
			
		} else if (this instanceof WmsMapLayer) {
			WmsMapLayer mapLayer = (WmsMapLayer) this;
			/*
			 * Request map layer to be deleted 
			 */
			mapLayer.fireEvent(WorkspaceEventType.WORKSPACE_LAYER_DELETE);
		}
	}
	
	public void fireChangeColor() {
		fireEvent(WorkspaceEventType.WORKSPACE_LAYER_COLOR_CHANGE);
	}
	
	@Override
	public String toString() {
		return "Map layer, IDs: " + Util.join(getMapIds(), ",");
	}
}

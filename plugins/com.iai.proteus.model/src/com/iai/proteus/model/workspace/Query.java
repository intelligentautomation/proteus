package com.iai.proteus.model.workspace;

import org.apache.log4j.Logger;

import com.iai.proteus.model.Deletable;
import com.iai.proteus.model.MapId;
import com.iai.proteus.model.Model;

/**
 * Models service queries (e.g. query to an SOS or WMS)
 * 
 * @author Jakob Henriksson
 *
 */
public abstract class Query extends Model implements Deletable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(Query.class);
	
	/*
	 * Specifies which underlying map layer this query should be 
	 * displayed on 
	 */
	private MapId mapId;	
	
	/*
	 * The source of this query (e.g. SOS) 
	 */
	protected Provenance provenance; 

	/** 
	 * Constructor 
	 */
	public Query() {
	}
	
	public void setProvenance(Provenance provenance) {
		this.provenance = provenance;
	}
	
	public Provenance getProvenance() {
		return provenance;
	}
	
	
	/**
	 * Returns the map ID this layer is intended to be displayed on 
	 * 
	 * @return the mapId
	 */
	public MapId getMapId() {
		return mapId;
	}

	/**
	 * Sets the map ID this layer is intended to be displayed on 
	 * 
	 * @param mapId the mapId to set
	 */
	public void setMapId(MapId mapId) {
		this.mapId = mapId;
	}

	/**
	 * Deletes a query 
	 */
	@Override
	public boolean delete() {
		Model parent = getParent();
		if (parent != null && parent instanceof QueryLayer) {
			QueryLayer queryLayer = (QueryLayer) parent;
			return queryLayer.removeQuery(this);
		} else {
			log.warn("Could not delete query, parent model object " + 
					"did not exist, or was not a Query Layer");
		}
		return false;
	}
}

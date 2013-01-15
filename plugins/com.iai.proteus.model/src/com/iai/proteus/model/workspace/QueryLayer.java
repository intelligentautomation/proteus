/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.common.sos.model.GetObservationRequest;
import com.iai.proteus.model.Deletable;
import com.iai.proteus.model.MapId;
import com.iai.proteus.model.Model;

/**
 * Represents a workspace layer consisting of queries to Web services 
 * 
 * @author Jakob Henriksson 
 *
 */
public class QueryLayer extends MapLayer 
	implements Iterable<Query>, Deletable 
{

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(QueryLayer.class);
	
	private ArrayList<Query> queries;

	/**
	 * Default constructor 
	 * 
	 */
	public QueryLayer() {
		queries = new ArrayList<Query>();
	}
	
	/**
	 * Constructor 
	 */
	public QueryLayer(Project project) {
		this();
		setName("Untitled");
		setParent(project);
	}
	
	/**
	 * Constructor 
	 * 
	 * @param name
	 */
	public QueryLayer(Project project, String name) {
		this(project);
		setName(name);
	}
	
	/**
	 * Returns the queries 
	 * 
	 * @return the queries
	 */
	public synchronized ArrayList<Query> getQueries() {
		return queries;
	}
	
	/**
	 * Sets the queries 
	 * 
	 * @param queries the queries to set
	 */
	public synchronized void setQueries(ArrayList<Query> queries) {
		this.queries = queries;
	}
	
	/**
	 * Returns the queries matching the given Map ID 
	 * 
	 * @param mapId
	 * @return
	 */
	public synchronized ArrayList<Query> getQueries(MapId mapId) {
		ArrayList<Query> queries  = new ArrayList<Query>();
		for (Query query : getQueries()) {
			if (query.getMapId().equals(mapId))
				queries.add(query);
		}
		return queries;
	}

	/**
	 * Returns the project this layer is part of 
	 * 
	 * @return
	 */
	public Project getProject() {
		Model parent = getParent();
		if (parent instanceof Project) {
			return (Project)parent;
		}
		log.error("The parent of the QueryLayer was not a project: " + parent);
		return null;
		
	}
	
	/**
	 * Adds a query to this layer 
	 * 
	 * @param query
	 */
	public synchronized boolean addQuery(Query query) {
		
		MapId mapId = null; 
		
		if (query instanceof QuerySos) {
			
			if (query instanceof QuerySensorOffering) {
				/*
				 * Don't re-add an existing query sensor offerings to 
				 * the same layer 
				 * 
				 */
				QuerySensorOffering qso = (QuerySensorOffering)query;
				String gmlId = qso.getSensorOffering().getGmlId();
				for (Query existing : queries) {
					if (existing instanceof QuerySensorOffering) {
						QuerySensorOffering qsoExisting = 
								(QuerySensorOffering)existing;
						String existingGmlId = 
								qsoExisting.getSensorOffering().getGmlId();
						if (existingGmlId.equals(gmlId)) {
							log.info("Sensor offering (" + gmlId + 
									") not added, already existed in layer " + 
									name); 
							return false;
						}
					}
				}
				
			} else if (query instanceof QueryGetObservation) {
				
				// TODO: don't re-add queries 
			}
			
			/*
			 * If there are existing queries we must figure out if we need
			 * to create a new map ID or if we can reuse an existing one
			 */
			if (getQueries().size() > 0) {
				/*
				 * Try and find an existing SOS sensor offering layer 
				 * map ID
				 */
				mapId = getExistingSosMapLayerId();
				if (mapId == null) {
					// if there is none, create a new map ID
					mapId = generateNewMapId();
				}
			} 
			
		} else if (query instanceof QueryWmsMap) {
			
			/*
			 * TODO: Do not re-add the same WMS map layer
			 */
			
			/*
			 * Generate a new Map ID every time 
			 */
			if (getQueries().size() > 0) {
				mapId = generateNewMapId();
			} 

		}
		
		/*
		 * If there were previous queries, we would expect that we have 
		 * (above) specified which map ID we should use (or a new one 
		 * should have been created). 
		 */			
		if (getQueries().size() > 0) {
			
			if (mapId != null) {
				// associate the query with this ID 
				query.setMapId(mapId);
				// also inform the map layer about this ID  
				addMapId(mapId);
			} else { 
				log.warn("Did not expect Map ID to be null");
			}

		} else { 
			/*
			 * If the query layer is empty, reuse the 'default' ID 
			 */
			query.setMapId(getDefaultMapId());
		}		
		
		// make this query layer the parent of the added query  
		query.setParent(this);
		
		return queries.add(query);
	}
	
	/**
	 * Removes the given query from the layer 
	 * 
	 * @param query
	 * 
	 * @return
	 */
	public boolean removeQuery(Query query) {
		int index = getIndex(query);
		if (index != -1) {
			queries.remove(index);
			return true;
		}
		log.warn("Could not find query in query layer; nothing removed");
		return false;
	}
	
	/**
	 * Returns an existing SOS query layer map ID if it exists, 
	 * null otherwise
	 * 
	 * @return
	 */
	private MapId getExistingSosMapLayerId() {
		for (Query query : getQueries()) {
			if (query instanceof QuerySos) {
				return ((QuerySos) query).getMapId();
			}
		}
		return null;
	}
	
	/**
	 * Returns the index of the query in the collection of queries, 
	 * -1 if it is not found 
	 * 
	 * @param query
	 * @return
	 */
	private int getIndex(Query query) {
		if (query instanceof QuerySensorOffering) {
			QuerySensorOffering qso = (QuerySensorOffering) query;
			String gmlId = qso.getSensorOffering().getGmlId();
			for (int i = 0; i < queries.size(); i++) {
				Query existing = queries.get(i);
				if (existing instanceof QuerySensorOffering) {
					QuerySensorOffering qsoExisting = 
						(QuerySensorOffering)existing;
					String existingGmlId = 
						qsoExisting.getSensorOffering().getGmlId();
					if (existingGmlId.equals(gmlId)) {
						return i; 
					}
				}
			}
		} else if (query instanceof QueryGetObservation) {
			QueryGetObservation qgo = (QueryGetObservation) query;
			GetObservationRequest request = qgo.getGetObservationRequest();
			int index = 0;
			for (Query existingQuery : getQueries()) {
				if (existingQuery instanceof QueryGetObservation) {
					QueryGetObservation existingQgo =
							(QueryGetObservation) existingQuery;
					GetObservationRequest existingRequest = 
							existingQgo.getGetObservationRequest();
					if (request.equals(existingRequest))
						return index;
				}
				index++;
			}
		}
		
		// TODO: handle other types of queries 
		
		// default
		return -1; 
	}
	
	@Override
	public Iterator<Query> iterator() {
		return queries.iterator();
	}
	
	/**
	 * Serialized to:
	 * 
	 * <layer>
	 * 	<name>...</name>
	 * 	<queries>...</queries>
	 * </layer>
	 * 
	 */
	@Override 
	public Element serialize(Document document) {
		Element root = document.createElement("layer");
		// name 
		Element name = document.createElement("name");
		name.setTextContent(getName());
		root.appendChild(name);
		// queries
		Element queries = document.createElement("queries");
		for (Query query : this) {
			// append queries
			Element elmt = query.serialize(document);
			if (elmt != null)
				queries.appendChild(elmt);
		}
		root.appendChild(queries);
		// color 
		Element color = document.createElement("color");
		String rgb = Integer.toHexString(getColor().getRGB());
		rgb = rgb.substring(2, rgb.length());
		color.setTextContent("#" + rgb);
		root.appendChild(color);
		
		return root; 	
	}

	@Override
	public boolean delete() {
		// remove queries 
		queries.clear();
		// remove query layer from project
		Project project = getProject();
		if (project != null)
			project.removeLayer(this);
		return true;
	}	
	
}

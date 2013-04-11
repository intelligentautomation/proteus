/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.iai.proteus.queryset.persist.v1.QuerySet;


/**
 * Singleton object to help manage loaded and saved query sets 
 * 
 * @author Jakob Henriksson
 *
 */
public class QuerySetManager {
	
	// maps query set UUIDs to QuerySet models 
	Map<String, QuerySet> storedQuerySets;
	
	// contains the query set UUIDs that are open
	Set<String> openQuerySets;
	
	/**
	 * Private constructor 
	 * 
	 */
	private QuerySetManager() {
		storedQuerySets = new HashMap<String, QuerySet>();
		openQuerySets = new HashSet<String>();
	}

	/**
	 * Add to the set of stored query sets 
	 * 
	 * @param uuid
	 * @param qs
	 * @return
	 */
	public boolean addStored(String uuid, QuerySet qs) {
		 storedQuerySets.put(uuid, qs);
		 return true;
	}
	
	/**
	 * Removes from the set of stored query sets 
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean removeStored(String uuid) {
		storedQuerySets.remove(uuid);
		return true;
	}
	
	/**
	 * Returns true if the query set is stored, false otherwise
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean isStored(String uuid) {
		return storedQuerySets.containsKey(uuid);
	}
	
	/**
	 * Returns the query set model object if it exists, null otherwise 
	 * 
	 * @param uuid
	 * @return
	 */
	public QuerySet getStored(String uuid) {
		return storedQuerySets.get(uuid);
	}
	
	/**
	 * Add to the set of opened query sets 
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean addOpen(String uuid) {
		return openQuerySets.add(uuid);
	}
	
	/**
	 * Removes from the set of opened query sets
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean removeOpen(String uuid) {
		return openQuerySets.remove(uuid);
	}
	
	/**
	 * Returns true if the query set is opened, false otherwise
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean isOpen(String uuid) {
		return openQuerySets.contains(uuid);
	}

	/**
	 * Singleton holder
	 *
	 */
	private static class SingletonHolder {
		public static final QuerySetManager INSTANCE =
			new QuerySetManager();
	}

	public static QuerySetManager getInstance() {
		return SingletonHolder.INSTANCE;
	}	

}

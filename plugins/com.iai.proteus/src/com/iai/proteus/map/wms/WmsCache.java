/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map.wms;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for WMSs (Capabilities and layers) 
 * 
 * @author Jakob Henriksson 
 *
 */
public class WmsCache {
	
	/*
	 * Maps: Service URL -> Capabilities
	 */
	private Map<String, WMSCapabilities> cache;
	
	/*
	 * Maps: Service URL -> Layers 
	 */
	private Map<String, Collection<WmsLayerInfo>> layers; 
	
	
	/**
	 * Constructor 
	 * 
	 */
	private WmsCache() {
		cache = new HashMap<String, WMSCapabilities>();
		layers = new HashMap<String, Collection<WmsLayerInfo>>();
	}
	
	/**
	 * Commit capabilities document to the cache
	 * 
	 * @param serviceUrl
	 * @param caps
	 */
	public void commit(String serviceUrl, WMSCapabilities caps) {
		this.cache.put(serviceUrl, caps);
	}
	
	/**
	 * Get a capabilities document from the cache, null if it does not exists
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public WMSCapabilities get(String serviceUrl) {
		return cache.get(serviceUrl);
	}
	
	/**
	 * Returns true if the cache contains an entry for the given service 
	 * end-point, false otherwise 
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public boolean contains(String serviceUrl) {
		return cache.containsKey(serviceUrl);
	}	

	/**
	 * Commits layers to the cache for a given service 
	 * 
	 * @param serviceUrl
	 * @param layers
	 */
	public void commitLayers(String serviceUrl, Collection<WmsLayerInfo> layers) {
		this.layers.put(serviceUrl, layers);
	}
	
	/**
	 * Returns the layers for a given service end-point, null if it does not
	 * exist
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public Collection<WmsLayerInfo> getLayers(String serviceUrl) {
		return layers.get(serviceUrl);
	}
	
	/**
	 * Returns true if the cache has layers for a given service end-point, 
	 * false otherwise 
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public boolean containsLayers(String serviceUrl) {
		return layers.containsKey(serviceUrl);
	}
	
	private static class SingletonHolder {
		public static final WmsCache instance = new WmsCache();
	}

	public static WmsCache getInstance() {
		return SingletonHolder.instance;
	}	

}

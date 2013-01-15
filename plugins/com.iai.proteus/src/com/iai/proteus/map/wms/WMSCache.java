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

public class WMSCache {
	
	private Map<String, WMSCapabilities> cache;
	private Map<String, Collection<LayerInfo>> layers; 
	
	private WMSCache() {
		cache = new HashMap<String, WMSCapabilities>();
		layers = new HashMap<String, Collection<LayerInfo>>();
	}
	
	public void commit(String serviceUrl, WMSCapabilities caps) {
		this.cache.put(serviceUrl, caps);
	}
	
	public WMSCapabilities get(String serviceUrl) {
		return cache.get(serviceUrl);
	}
	
	public boolean contains(String serviceUrl) {
		return cache.containsKey(serviceUrl);
	}	
	
	public void commitLayers(String serviceUrl, Collection<LayerInfo> layers) {
		this.layers.put(serviceUrl, layers);
	}
	
	public Collection<LayerInfo> getLayers(String serviceUrl) {
		return layers.get(serviceUrl);
	}
	
	public boolean containsLayers(String serviceUrl) {
		return layers.containsKey(serviceUrl);
	}
	
	private static class SingletonHolder {
		public static final WMSCache instance = new WMSCache();
	}

	public static WMSCache getInstance() {
		return SingletonHolder.instance;
	}	

}

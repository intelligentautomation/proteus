/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.iai.proteus.ui.queryset.SensorOfferingItem;

public class SosOfferingLayerStats {

	Map<String, Integer> propertyCount = new HashMap<String, Integer>();
	Collection<SensorOfferingItem> sensorOfferingItems = new ArrayList<SensorOfferingItem>();
	Collection<String> formats = new HashSet<String>();

	// all offerings
	int countAll = 0;
	// all offerings in sector
	int countInSector = 0;
	// all offerings in sector + matching time
	int countTime = 0;
	// all offerings in sector + matching properties
	int countProperties = 0;
	// all offerings in sector + matching formats
	int countFormats = 0;
	// all offerings in sector + matching facets
	int countFilter = 0;

	public SosOfferingLayerStats() {

	}

	/**
	 * @return the propertyCount
	 */
	public Map<String, Integer> getPropertyCount() {
		return propertyCount;
	}

	/**
	 * @param propertyCount the propertyCount to set
	 */
	public void setPropertyCount(Map<String, Integer> propertyCount) {
		this.propertyCount = propertyCount;
	}

	/**
	 * @return the sensorOfferingItems
	 */
	public Collection<SensorOfferingItem> getSensorOfferingItems() {
		return sensorOfferingItems;
	}

	/**
	 * @param sensorOfferingItems the sensorOfferingItems to set
	 */
	public void setSensorOfferingItems(
			Collection<SensorOfferingItem> sensorOfferingItems) {
		this.sensorOfferingItems = sensorOfferingItems;
	}

	/**
	 * @return the formats
	 */
	public Collection<String> getFormats() {
		return formats;
	}

	/**
	 * @param formats the formats to set
	 */
	public void setFormats(Collection<String> formats) {
		this.formats = formats;
	}

	/**
	 * @return the countAll
	 */
	public int getCountAll() {
		return countAll;
	}

	/**
	 * @param countAll the countAll to set
	 */
	public void setCountAll(int countAll) {
		this.countAll = countAll;
	}

	/**
	 * @return the countInSector
	 */
	public int getCountInSector() {
		return countInSector;
	}

	/**
	 * @param countInSector the countInSector to set
	 */
	public void setCountInSector(int countInSector) {
		this.countInSector = countInSector;
	}

	/**
	 * @return the countTime
	 */
	public int getCountTime() {
		return countTime;
	}

	/**
	 * @param countTime the countTime to set
	 */
	public void setCountTime(int countTime) {
		this.countTime = countTime;
	}

	/**
	 * @return the countProperties
	 */
	public int getCountProperties() {
		return countProperties;
	}

	/**
	 * @param countProperties the countProperties to set
	 */
	public void setCountProperties(int countProperties) {
		this.countProperties = countProperties;
	}

	/**
	 * @return the countFormats
	 */
	public int getCountFormats() {
		return countFormats;
	}

	/**
	 * @param countFormats the countFormats to set
	 */
	public void setCountFormats(int countFormats) {
		this.countFormats = countFormats;
	}

	/**
	 * @return the countFilter
	 */
	public int getCountFilter() {
		return countFilter;
	}

	/**
	 * @param countFilter the countFilter to set
	 */
	public void setCountFilter(int countFilter) {
		this.countFilter = countFilter;
	}


}

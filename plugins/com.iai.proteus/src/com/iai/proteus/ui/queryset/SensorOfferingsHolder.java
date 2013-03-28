/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.queryset;

import java.util.ArrayList;
import java.util.Collection;

public class SensorOfferingsHolder {

	/*
	 * Holds the sensor offerings
	 */
	private Collection<SensorOfferingItem> sensorOfferings;

	/**
	 * Constructor
	 *
	 */
	public SensorOfferingsHolder() {

		sensorOfferings = new ArrayList<SensorOfferingItem>();
	}

	/**
	 * Sets the sensor offerings
	 *
	 * @param sensorOfferings
	 */
	public void setSensorOfferings(Collection<SensorOfferingItem> sensorOfferings) {
		this.sensorOfferings = sensorOfferings;
	}

	/**
	 * Returns the sensor offerings
	 *
	 * @return
	 */
	public Collection<SensorOfferingItem> getSensorOfferings() {
		return sensorOfferings;
	}

}

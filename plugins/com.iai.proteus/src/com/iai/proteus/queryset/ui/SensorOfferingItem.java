/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset.ui;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.model.services.Service;

public class SensorOfferingItem implements IAdaptable {

	private transient SensorOfferingItemPropertySource property;

	private Service service;
	private SensorOffering sensorOffering;

	/**
	 * Constructor
	 *
	 * @param service
	 * @param sensorOffering
	 */
	public SensorOfferingItem(Service service, SensorOffering sensorOffering) {
		this.service = service;
		this.sensorOffering = sensorOffering;
	}

	/**
	 * @return the service
	 */
	public Service getService() {
		return service;
	}

	/**
	 * @return the sensorOffering
	 */
	public SensorOffering getSensorOffering() {
		return sensorOffering;
	}

	/**
	 *
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (property == null) {
				// cache the source
				property = new SensorOfferingItemPropertySource(this);
			}
			return property;
		}
		// default
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SensorOfferingItem) {
			SensorOfferingItem item = (SensorOfferingItem) obj;
			if (item.getSensorOffering().getGmlId().equals(getSensorOffering().getGmlId()))
					return true;
		}
		return false;
	}
}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Model;
import com.iai.proteus.model.properties.SosServicePropertySource;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;

/**
 * Represents a model object that indicates the provenance of a queries 
 * 
 * @author Jakob Henriksson 
 *
 */
public class Provenance extends Model implements IAdaptable {

	private static final long serialVersionUID = 1L;
	
	private Service service; 
	
	private transient SosServicePropertySource property;
	
	/**
	 * Default constructor
	 * 
	 */
	public Provenance() {
		
	}
	
	/**
	 * Constructor 
	 * 
	 * @param service
	 */
	public Provenance(Service service) {
		this.service = service; 
	}
	
	/**
	 * Returns the service 
	 * 
	 * @return the service
	 */
	public Service getService() {
		return service;
	}

	/**
	 * Sets the service 
	 * 
	 * @param service the service to set
	 */
	public void setService(Service service) {
		this.service = service;
	}
	
	/**
	 * Delegate name to service  
	 */
	@Override
	public String getName() {
		if (service != null)
			return service.getName();
		return null;
	}
	
	/**
	 * Delegate name to service
	 * 
	 * @param name
	 */
	@Override
	public void setName(String name) {
		if (service != null)
			service.setName(name);
	}
	
	@Override
	public String toString() {
		if (service != null)
			return service.toString();
		return "Provenance: <unknown>";
	}

	@Override
	public Element serialize(Document document) {
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Provenance) {
			Provenance provenance = (Provenance) object;
			Service ser = provenance.getService();
			if (ser != null && service != null) {
				return ser.equals(service);
			}
		}
		return false;
	}
	
	/**
	 * 
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (service.getServiceType() == ServiceType.SOS) {
			if (adapter == IPropertySource.class) {
				if (property == null) {
					// cache the source 
					property = new SosServicePropertySource(service);
				}
				return property;
			}
		}
		// default 
		return null;
	}	

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.services;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Model;

/**
 * The root of all services
 *
 * @author Jakob Henriksson
 *
 */
public class ServiceRoot extends Model implements Iterable<Service>, ServiceManager {

	private static final long serialVersionUID = 1L;

	private ArrayList<Service> services;

	/**
	 * Constructor
	 *
	 * NOTE: This constructor is public to be bean-compliant. However,
	 *       the usage of this class is intended to follow the Singleton
	 *       pattern: ServiceRoot root = ServiceRoot.getInstance();
	 */
	public ServiceRoot() {
		 services = new ArrayList<Service>();
	}

	/**
	 * Returns all services
	 *
	 * Implements @{link ServiceManager}
	 *
	 * @return the services
	 */
	@Override
	public synchronized ArrayList<Service> getServices() {
		return services;
	}
	
	/**
	 * Return services of the given type 
	 * 
	 * Implements @{link ServiceManager}
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public synchronized ArrayList<Service> getServices(ServiceType type) {
		ArrayList<Service> result = new ArrayList<Service>();
		for (Service service : getServices()) {
			if (service.getServiceType().equals(type))
				result.add(service);
		}
		return result;
	}

	/**
	 * Sets all services
	 *
	 * @param services the services to set
	 */
	public synchronized void setServices(ArrayList<Service> services) {
		this.services = services;
	}

	/**
	 * Adds a service
	 *
	 * Implements @{link ServiceManager} 
	 *
	 * @param service
	 * @return true if service was removed, false otherwise 
	 */
	@Override
	public synchronized boolean addService(Service service) {
		if (!services.contains(service)) {
			// when a service is added, make is in-active by default
			service.deactivate();
			return services.add(service);
		}
		return false;
	}

	/**
	 * Removes a service
	 * 
	 * Implements @{link ServiceManager}
	 * 
	 * @param service
	 * @returns true if service was removed, false otherwise
	 */
	@Override
	public synchronized boolean removeService(Service service) {
		return services.remove(service);
	}

	/**
	 * Returns the first source matching the URL, null if none found
	 *
	 * @param serviceUrl
	 * @return
	 */
	public synchronized Service getService(String serviceUrl) {
		for (Service service : getServices()) {
			if (service.getEndpoint().equals(serviceUrl))
				return service;
		}
		return null;
	}

	/**
	 * Returns true if the service url is already in the list of services,
	 * false otherwise
	 *
	 * @param serviceUrl
	 * @return
	 */
	public synchronized boolean containsServiceUrl(String serviceUrl) {
		for (Service service : getServices()) {
			if (service.getEndpoint().equals(serviceUrl))
				return true;
		}
		return false;
	}

	/**
	 * Returns the number of sources
	 *
	 * @return
	 */
	public int getSize() {
		return services.size();
	}

	private static class SingletonHolder {
		public static final ServiceRoot instance = new ServiceRoot();
	}

	public static ServiceRoot getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public String toString() {
		return "Services";
	}

	@Override
	public Iterator<Service> iterator() {
		return services.iterator();
	}

	/**
	 * Serialized to:
	 *
	 * <services>
	 *  ...
	 * </services>
	 *
	 */
	@Override
	public Element serialize(Document document) {
		Element root = document.createElement("services");
		for (Service service : this) {
			root.appendChild(service.serialize(document));
		}
		return root;
	}
}

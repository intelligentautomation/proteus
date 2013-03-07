/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.services;

import java.util.Collection;

/**
 * Interface for service managers 
 * 
 * @author Jakob Henriksson
 *
 */
public interface ServiceManager {
	
	// returns true if the service was added, false otherwise 
	public boolean addService(Service service);
	// returns true if the service was removed, false otherwise
	public boolean removeService(Service service);
	
	// returns all services
	public Collection<Service> getServices();
	// returns all services of the given type 
	public Collection<Service> getServices(ServiceType type);

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;

public class ServiceContentProvider implements IStructuredContentProvider {

	Object[] EMPTY_ARRAY = new Object[0];
	
	/**
	 * Constructor
	 * 
	 */
	public ServiceContentProvider() {

	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object element) {

		if (element instanceof ServiceRoot) {
			ServiceRoot root = (ServiceRoot) element;
			
			List<Service> services = root.getServices(); 
			
			// sort alphabetically
			Collections.sort(services, new Comparator<Service>() {
				@Override
				public int compare(Service o1, Service o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			List<Service> likedServices = new ArrayList<Service>();
			List<Service> otherServices = new ArrayList<Service>();
			
			for (Service service : services) {
				if (service.isVisible())
					likedServices.add(service);
				else
					otherServices.add(service);
			}
			
			List<Service> allServices = new ArrayList<Service>();
			allServices.addAll(likedServices);
			allServices.addAll(otherServices);
			
			return allServices.toArray();
		}

		return EMPTY_ARRAY;
	}

}

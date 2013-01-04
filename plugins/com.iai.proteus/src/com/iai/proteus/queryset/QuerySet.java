package com.iai.proteus.queryset;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.workspace.MapLayer;

/**
 * Represents all details for a query set
 *
 * @author Jakob Henriksson
 *
 */
public class QuerySet extends MapLayer {

	private static final long serialVersionUID = 1L;

	private List<Service> services;

	/**
	 * Constructor
	 *
	 */
	public QuerySet() {
		services = new ArrayList<Service>();
	}

	/**
	 * @return the services
	 */
	public List<Service> getServices() {
		return services;
	}

	/**
	 * @param services the services to set
	 */
	public void setServices(List<Service> services) {
		this.services = services;
	}

	/**
	 *
	 * @param service
	 */
	public void addService(Service service) {
		if (!services.contains(service))
			services.add(service);
	}

	/**
	 *
	 * @param service
	 * @return
	 */
	public boolean removeService(Service service) {
		return services.remove(service);
	}

	@Override
	public Element serialize(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

}

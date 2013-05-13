/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.services;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Deletable;
import com.iai.proteus.model.Likable;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.properties.SosServicePropertySource;

/**
 * Represents a layer visible on a map 
 * 
 * @author Jakob Henriksson
 *
 */
public class Service extends MapLayer 
	implements Cloneable, Deletable, Likable, IAdaptable
{
	
	private static final long serialVersionUID = 1L;
	
	private String endpoint;
	private ServiceType serviceType;
	
	// indicates a "favorite" service  
	private boolean liked; 
	// indicates that connection to service has been successful
	private boolean success; 

	private transient SosServicePropertySource property; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public Service() {
		// defaults
		property = null;
		liked = false; 
		success = false;
		// note: a service should be inactive by default 
		// see MayLayer.active
	}
	
	/**
	 * Constructor 
	 * 
	 * @param type
	 */
	public Service(ServiceType type) {
		this();
		this.serviceType = type;
		setName("Untitled");
	}
	
	/**
	 * Constructor 
	 * 
	 * @param name
	 * @param type
	 * @param url 
	 */
	public Service(String name, ServiceType type, String url) {
		this(type);
		this.endpoint = url;
		setName(name);
	}
	
	/**
	 * Returns true if we have a URL, false otherwise 
	 * 
	 * @return
	 */
	public boolean hasEndpoint() {
		return endpoint != null;
	}
	
	/*
	 * Getters and Setters
	 */
	
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String url) {
		this.endpoint = url;
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(ServiceType type) {
		this.serviceType = type;
	}
	
	@Override
	public void like() {
		liked = true;
	}

	@Override
	public void dislike() {
		liked = false;
		
	}
	
	/**
	 * @param liked the liked to set
	 */
	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	/**
	 * @return the liked
	 */
	@Override
	public boolean isLiked() {
		return liked;
	}
	
	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return (name == null ? "<untitled>" : name) + " (" + serviceType + ")";
	}
	
	public String prettyString() {
		return "<" + serviceType + "; URL: " + endpoint + ">";
	}
	
	/**
	 * Serialized to:
	 * 
	 * <service type="SOS">
	 * 	<name>...</name>
	 * 	<url>...</url>
	 * </service>
	 * 
	 * Note: the <cache> tag is only present if the service is not a "query 
	 * service"
	 * 
	 */
	@Override 
	public Element serialize(Document document) {
		Element root = document.createElement("service");
		root.setAttribute("type", serviceType.toString());
		// name 
		Element name = document.createElement("name");
		name.setTextContent(getName());
		root.appendChild(name);
		// URL
		Element url = document.createElement("url");
		url.setTextContent(getEndpoint());
		root.appendChild(url);
		// color
		Element color = document.createElement("color");
		String rgb = Integer.toHexString(getColor().getRGB());
		rgb = rgb.substring(2, rgb.length());
		color.setTextContent("#" + rgb);
		root.appendChild(color);
		
		return root; 	
	}	
	
	/**
	 * 
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		
		if (getServiceType() == ServiceType.SOS) {
			if (adapter == IPropertySource.class) {
				if (property == null) {
					// cache the source 
					property = new SosServicePropertySource(this);
				}
				return property;
			}
		}
		// default 
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serviceType == null) ? 0 : serviceType.hashCode());
		result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (serviceType != other.serviceType)
			return false;
		if (endpoint == null) {
			if (other.endpoint != null)
				return false;
		} else if (!endpoint.equals(other.endpoint))
			return false;
		return true;
	}
	
	/**
	 * Clones this service 
	 * 
	 * Mainly carries over source type, URL, name  
	 * 
	 */
	@Override
	public Object clone() {
		Service service = new Service(getServiceType());
		service.setName(getName());
		service.setEndpoint(getEndpoint());
		return service; 
	}

	@Override
	public boolean delete() {
		return ServiceRoot.getInstance().removeService(this);
	}
}


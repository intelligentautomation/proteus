package com.iai.proteus.model.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Deletable;
import com.iai.proteus.model.Likable;
import com.iai.proteus.model.properties.SosServicePropertySource;
import com.iai.proteus.model.workspace.MapLayer;

/**
 * Represents a layer visible on a map 
 * 
 * @author Jakob Henriksson
 *
 */
public class Service extends MapLayer 
	implements Cloneable, Deletable, Likable, IAdaptable 
{
	
//	private static final Logger log = Logger.getLogger(Source.class);
	
	private static final long serialVersionUID = 1L;
	
	private String serviceUrl;
	private ServiceType serviceType;
	
	private boolean liked; 

	private transient SosServicePropertySource property; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public Service() {
		// defaults
		property = null;
		liked = false; 
	}
	
	/**
	 * Constructor 
	 * 
	 * @param type
	 */
	public Service(ServiceType type) {
		this();
		this.serviceType = type;
		setName("untitled");
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
		this.serviceUrl = url;
		setName(name);
	}
	
	/**
	 * Returns a list of information about the source 
	 * 
	 * @return
	 */
	public List<String> getSourceDetails() {
		List<String> children = new ArrayList<String>(); 
    	children.add("Type: " + serviceType.toString());
    	children.add("URL: " + serviceUrl);
		return children;
	}
	
	/**
	 * Returns true if we have a URL, false otherwise 
	 * 
	 * @return
	 */
	public boolean hasUrl() {
		return serviceUrl != null;
	}
	
	/*
	 * Getters and Setters
	 */
	
	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String url) {
		this.serviceUrl = url;
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(ServiceType type) {
		this.serviceType = type;
	}
	
	@Override
	public String toString() {
		return (name == null ? "<untitled>" : name) + " (" + serviceType + ")";
	}
	
	public String prettyString() {
		return "<" + serviceType + "; URL: " + serviceUrl + ">";
	}
	
	/**
	 * Serialized to:
	 * 
	 * <service type="SOS">
	 * 	<name>...</name>
	 * 	<url>...</url>
	 * </service>
	 * 
	 * Note: the <cache> tag is only present if the source is not a "query 
	 * source"
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
		url.setTextContent(getServiceUrl());
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
		result = prime * result + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
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
		if (serviceUrl == null) {
			if (other.serviceUrl != null)
				return false;
		} else if (!serviceUrl.equals(other.serviceUrl))
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
		service.setServiceUrl(getServiceUrl());
		return service; 
	}

	@Override
	public boolean delete() {
		return ServiceRoot.getInstance().removeService(this);
	}

	@Override
	public void like() {
		liked = true;
	}

	@Override
	public void dislike() {
		liked = false;
		
	}

	@Override
	public boolean isLiked() {
		return liked;
	}
}


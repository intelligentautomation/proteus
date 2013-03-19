/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub.apiv1;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Represents an alert from the Community Hub
 * 
 * @author Jakob Henriksson
 *
 */
public class Alert implements IAdaptable {
	
	private int id;
	private String type;
	private String detail;
	private Date dateCreated;
	private Date lastUpdate;
	private Date validFrom;
	private Date validTo;
	private Double latLower;
	private Double latUpper;
	private Double lonLower;
	private Double lonUpper;
	private String serviceEndpoint; 
	private String sensorOfferingId;
	private String observedProperty;
	
	private AlertPropertySource property;
	
	/**
	 * Constructor 
	 * 
	 */
	public Alert() {
		
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @param detail the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the lastUpdate
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * @return the validFrom
	 */
	public Date getValidFrom() {
		return validFrom;
	}

	/**
	 * @param validFrom the validFrom to set
	 */
	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	/**
	 * @return the validTo
	 */
	public Date getValidTo() {
		return validTo;
	}

	/**
	 * @param validTo the validTo to set
	 */
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	/**
	 * @return the latLower
	 */
	public Double getLatLower() {
		return latLower;
	}

	/**
	 * @param latLower the latLower to set
	 */
	public void setLatLower(Double latLower) {
		this.latLower = latLower;
	}

	/**
	 * @return the latUpper
	 */
	public Double getLatUpper() {
		return latUpper;
	}

	/**
	 * @param latUpper the latUpper to set
	 */
	public void setLatUpper(Double latUpper) {
		this.latUpper = latUpper;
	}

	/**
	 * @return the lonLower
	 */
	public Double getLonLower() {
		return lonLower;
	}

	/**
	 * @param lonLower the lonLower to set
	 */
	public void setLonLower(Double lonLower) {
		this.lonLower = lonLower;
	}

	/**
	 * @return the lonUpper
	 */
	public Double getLonUpper() {
		return lonUpper;
	}

	/**
	 * @param lonUpper the lonUpper to set
	 */
	public void setLonUpper(Double lonUpper) {
		this.lonUpper = lonUpper;
	}

	/**
	 * @return the serviceEndpoint
	 */
	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	/**
	 * @param serviceEndpoint the serviceEndpoint to set
	 */
	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	/**
	 * @return the sensorOfferingId
	 */
	public String getSensorOfferingId() {
		return sensorOfferingId;
	}

	/**
	 * @param sensorOfferingId the sensorOfferingId to set
	 */
	public void setSensorOfferingId(String sensorOfferingId) {
		this.sensorOfferingId = sensorOfferingId;
	}

	/**
	 * @return the observedProperty
	 */
	public String getObservedProperty() {
		return observedProperty;
	}

	/**
	 * @param observedProperty the observedProperty to set
	 */
	public void setObservedProperty(String observedProperty) {
		this.observedProperty = observedProperty;
	}
	
	/**
	 * Adapter
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		
		if (adapter == IPropertySource.class) {
			if (property == null) {
				// cache the source 
				property = new AlertPropertySource(this);
			}
			return property;
		}
		// default 
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result
				+ ((serviceEndpoint == null) ? 0 : serviceEndpoint.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Alert other = (Alert) obj;
		if (id != other.id)
			return false;
		if (serviceEndpoint == null) {
			if (other.serviceEndpoint != null)
				return false;
		} else if (!serviceEndpoint.equals(other.serviceEndpoint))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}

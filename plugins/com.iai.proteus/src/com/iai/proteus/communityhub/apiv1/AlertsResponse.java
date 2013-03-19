/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub.apiv1;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Object structure for the response to the Community Hub API call
 * to endpoint /apiv1/alerts/
 * 
 * @author Jakob Henriksson
 *
 */
public class AlertsResponse {
	
	private String status;
	private String version;
	private Collection<Alert> alerts;
	
	/**
	 * Constructor
	 * 
	 */
	public AlertsResponse() {
		alerts = new ArrayList<Alert>();
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the message to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the alerts
	 */
	public Collection<Alert> getAlerts() {
		return alerts;
	}

	/**
	 * @param alerts the alerts to set
	 */
	public void setAlerts(Collection<Alert> alerts) {
		this.alerts = alerts;
	}

}

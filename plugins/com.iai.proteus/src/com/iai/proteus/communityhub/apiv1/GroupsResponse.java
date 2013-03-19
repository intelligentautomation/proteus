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
 * to endpoint /apiv1/groups/
 * 
 * @author Jakob Henriksson
 *
 */
public class GroupsResponse {
	
	private String status;
	private String version;
	private Collection<Group> groups;
	
	/**
	 * Constructor
	 * 
	 */
	public GroupsResponse() {
		groups = new ArrayList<Group>();
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
	 * @return the groups
	 */
	public Collection<Group> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(Collection<Group> groups) {
		this.groups = groups;
	}

}

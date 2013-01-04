package com.iai.proteus.communityhub;

import java.util.ArrayList;
import java.util.Collection;

public class CommunityGroupResponse {
	
	private String status;
	private String version;
	private Collection<Group> response;
	
	/**
	 * Constructor
	 * 
	 */
	public CommunityGroupResponse() {
		response = new ArrayList<Group>();
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
	 * @return the response
	 */
	public Collection<Group> getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(Collection<Group> response) {
		this.response = response;
	}

}

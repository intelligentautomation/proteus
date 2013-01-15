/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import com.iai.proteus.common.sos.util.SosDataRequest;

public class DownloadModel {
	
	private Status status; 
	private String statusMsg;
	private String label; 
	private SosDataRequest dataRequest;
	
	/**
	 * Constructor 
	 */
	public DownloadModel() {
		status = Status.NONE;
		statusMsg = "Pending...";
		label = "-";
	}
	
	
	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}



	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}



	/**
	 * @return the statusMsg
	 */
	public String getStatusMsg() {
		return statusMsg;
	}



	/**
	 * @param statusMsg the statusMsg to set
	 */
	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}


	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}


	/**
	 * @return the dataRequest
	 */
	public SosDataRequest getDataRequest() {
		return dataRequest;
	}



	/**
	 * @param dataRequest the dataRequest to set
	 */
	public void setDataRequest(SosDataRequest dataRequest) {
		this.dataRequest = dataRequest;
	}


	public enum Status {
		NONE, 
		SUCCESS, 
		ERROR, 
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		DownloadModel other = (DownloadModel) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
}

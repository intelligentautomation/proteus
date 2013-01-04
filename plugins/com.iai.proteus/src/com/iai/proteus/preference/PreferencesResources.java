package com.iai.proteus.preference;

import java.io.Serializable;

/** 
 * Preferences Bean object for model resources
 * 
 * @author Jakob Henriksson 
 */
public class PreferencesResources implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/* true if data query resource model objects should be put under
	 * the offering in the project/layer view
	 */  
	private boolean dataQueryUnderOffering;
	
	
	/**
	 * Constructor 
	 */
	public PreferencesResources() {
		dataQueryUnderOffering = true; 
	}
	
	public void setDataQueryUnderOffering(boolean status) {
		this.dataQueryUnderOffering = status;
	}
	
	public boolean getDataQueryUnderOffering() {
		return dataQueryUnderOffering;
	}
}

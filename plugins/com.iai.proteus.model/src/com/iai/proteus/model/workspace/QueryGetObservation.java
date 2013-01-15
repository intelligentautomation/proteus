/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.workspace;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.common.Labeling;
import com.iai.proteus.common.sos.model.GetObservationRequest;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.model.event.WorkspaceEventType;
import com.iai.proteus.model.properties.QueryGetObservationPropertySource;

/**
 * Represents an SOS GetObservation query 
 * 
 * @author Jakob Henriksson
 *
 */
public class QueryGetObservation extends QuerySos implements IAdaptable {

	private static final long serialVersionUID = 1L;
	
	private GetObservationRequest getObservationRequest;
	
	private transient QueryGetObservationPropertySource property;	

	/**
	 * Default constructor 
	 * 
	 */
	public QueryGetObservation() {
		
	}
	
	/**
	 * Constructor 
	 */
	public QueryGetObservation(GetObservationRequest request) {
		super(request.getSensorOffering());
		this.getObservationRequest = request; 
		setName(Labeling.labelProperty(request.getObservedProperty()));
	}
	
	/**
	 * Set the GetObservation request
	 * 
	 * @param request
	 */
	public void setGetObservationRequest(GetObservationRequest request) {
		this.getObservationRequest = request;
	}
	
	/**
	 * Returns the GetObservation request 
	 * 
	 * @return
	 */
	public GetObservationRequest getGetObservationRequest() {
		return getObservationRequest;
	}

	/**
	 * Fires and event that the query should be executed 
	 * 
	 */
	public void fireExecQuery() {
		GetObservationRequest request = 
				getGetObservationRequest();
		if (request != null) {
			// create a SosDataRequest and send as a value to event listeners
			SosDataRequest dataRequest = 
					new SosDataRequest("", 
							getProvenance().getService().getServiceUrl(), 
							request);	
			// fire event 
			fireEvent(WorkspaceEventType.WORKSPACE_EXEC_QUERY, dataRequest);
		} 		
	}
	
	/**
	 * Serializes this model element
	 *  
	 */
	@Override 
	public Element serialize(Document document) {
		Element root = document.createElement("query");
		// attributes
		root.setAttribute("type", "GetObservation");
		// source 
		root.appendChild(getProvenance().serialize(document));
		// query
		GetObservationRequest request = getGetObservationRequest();
		root.appendChild(request.createXmlRequest(document, false));
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
		if (adapter == IPropertySource.class) {
			if (property == null) {
				// cache the source 
				property = new QueryGetObservationPropertySource(this);
			}
			return property;
		}
		// default 
		return null;
	}		
}

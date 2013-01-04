package com.iai.proteus.model;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.event.WorkspaceEvent;
import com.iai.proteus.model.event.WorkspaceEventNotifier;
import com.iai.proteus.model.event.WorkspaceEventType;

/**
 * Represents and abstract model object 
 * 
 * @author Jakob Henriksson 
 * 
 */
public abstract class Model implements Namable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected String name;
	protected Model parent;
		
	/**
	 * 
	 */
	public Model() {
		// default 
		name = "untitled";
	}
	
	public Model getParent() {
		return parent;
	}
	
	public void setParent(Model node) {
		this.parent = node;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Notifies listeners that there has been an update
	 * 
	 * @param instance The exact instance that was modified  
	 */
	public void fireUpdated() {
		fireEvent(WorkspaceEventType.WORKSPACE_MODEL_UPDATED);
	}
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 */	
	public void fireEvent(WorkspaceEventType eventType) {
		fireEvent(this, eventType, null);
	}	
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 */	
	public void fireEvent(WorkspaceEventType eventType, Object value) {
		fireEvent(this, eventType, value);
	}		
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 * @param value 
	 */	
	private void fireEvent(Object eventObject, WorkspaceEventType eventType, Object value) {
		WorkspaceEvent event = new WorkspaceEvent(eventObject); 
		event.setEventType(eventType); 
		if (value != null)
			event.setValue(value); 
		
		/*
		 * The event is fired on listeners to the below object instead of 
		 * listeners to the underlying event, this is done for convenience   
		 */
		WorkspaceEventNotifier.getInstance().fireEvent(this, eventType, value);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * The reason this is deprecated is because we are now using 
	 * Java Bean serialization to persist models objects to disk 
	 * 
	 * @param document
	 * @return
	 */
	@Deprecated
	public abstract Element serialize(Document document);
	
}

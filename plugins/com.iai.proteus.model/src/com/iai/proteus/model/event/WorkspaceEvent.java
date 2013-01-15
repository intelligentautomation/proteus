/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.event;

/**
 * An event 
 * 
 * @author Jakob Henriksson
 *
 */
public class WorkspaceEvent {
	
	protected Object eventObject;
	protected WorkspaceEventType eventType; 
	/*
	 * Additional data object (not necessarily the same as the event object) 
	 */
	protected Object value; 
	
	public WorkspaceEvent(Object obj) {
		eventObject = obj;
	}
	
	public void setEventType(WorkspaceEventType type) {
		this.eventType = type;
	}
	
	public WorkspaceEventType getEventType() {
		return eventType; 
	}
	
	public Object getEventObject() {
		return eventObject;
	}
	
	public void setValue(Object value) {
		this.value = value; 
	}
	
	public Object getValue() {
		return value; 
	}

}

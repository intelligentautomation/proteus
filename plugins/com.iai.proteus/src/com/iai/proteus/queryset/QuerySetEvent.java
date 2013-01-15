/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

/**
 * An event 
 * 
 * @author Jakob Henriksson
 *
 */
public class QuerySetEvent {
	
	protected Object eventObject;
	protected QuerySetEventType eventType; 
	/*
	 * Additional data object (not necessarily the same as the event object) 
	 */
	protected Object value; 
	
	public QuerySetEvent(Object obj) {
		eventObject = obj;
	}
	
	public void setEventType(QuerySetEventType type) {
		this.eventType = type;
	}
	
	public QuerySetEventType getEventType() {
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

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.events;

public class Event {
	
	protected Object eventObject;
	protected EventType eventType; 
	/*
	 * Additional data object (not necessarily the same as the event object) 
	 */
	protected Object value; 
	
	public Event(Object obj) {
		eventObject = obj;
	}
	
	public void setEventType(EventType type) {
		this.eventType = type;
	}
	
	public EventType getEventType() {
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

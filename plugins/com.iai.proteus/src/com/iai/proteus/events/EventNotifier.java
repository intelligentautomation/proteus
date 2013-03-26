/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.events;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Notification object that listeners can register to in order to receive
 * World Wind selection updates 
 * 
 * @author Jakob Henriksson
 *
 */
public class EventNotifier {

	/*
	 * Collection of listeners on this model object  
	 */
	protected Collection<EventListener> listeners = 
		new ArrayList<EventListener>(); 
	
	/**
	 * Adding a listener 
	 * 
	 * @param listener
	 */
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removing a listener 
	 * 
	 * @param listener
	 */
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 */	
	public void fireEvent(Object eventObject, EventType eventType) {
		fireEvent(eventObject, eventType, null);
	}
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 * @param value 
	 */	
	public void fireEvent(Object eventObject, EventType eventType, Object value) {
		Event event = new Event(eventObject); 
		event.setEventType(eventType); 
		if (value != null)
			event.setValue(value); 
		for (EventListener listener : listeners) {
			listener.update(event);
		}		
	}
	
	
	/**
	 * Singleton holder 
	 * 
	 */
	private static class SingletonHolder {
		public static final EventNotifier INSTANCE = 
			new EventNotifier();
	}

	public static EventNotifier getInstance() {
		return SingletonHolder.INSTANCE;
	}
}

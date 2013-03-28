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
 * notification of query set events
 *
 * @author Jakob Henriksson
 *
 */
public class QuerySetEventNotifier {

	// Collection of listeners on this model object
	protected Collection<QuerySetEventListener> listeners =
		new ArrayList<QuerySetEventListener>();

	/**
	 * Private constructor
	 * 
	 */
	private QuerySetEventNotifier() {
		
	}
	
	/**
	 * Adding a listener
	 *
	 * @param listener
	 */
	public void addListener(QuerySetEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removing a listener
	 *
	 * @param listener
	 */
	public void removeListener(QuerySetEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Update listeners
	 *
	 * @param eventObject
	 * @param eventType
	 */
	public void fireEvent(Object eventObject, QuerySetEventType eventType) {
		fireEvent(eventObject, eventType, null);
	}

	/**
	 * Update listeners
	 *
	 * @param eventObject
	 * @param eventType
	 * @param value
	 */
	public void fireEvent(Object eventObject, QuerySetEventType eventType, Object value) {
		QuerySetEvent event = new QuerySetEvent(eventObject);
		event.setEventType(eventType);
		if (value != null)
			event.setValue(value);
		for (QuerySetEventListener listener : listeners) {
			listener.querySetEventHandler(event);
		}
	}


	/**
	 * Singleton holder
	 *
	 */
	private static class SingletonHolder {
		public static final QuerySetEventNotifier INSTANCE =
			new QuerySetEventNotifier();
	}

	public static QuerySetEventNotifier getInstance() {
		return SingletonHolder.INSTANCE;
	}
}

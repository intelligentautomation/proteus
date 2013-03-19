/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;

/**
 * Alert event notifier 
 * 
 * @author Jakob Henriksson
 *
 */
public class AlertEventNotifier {
	
	// Collection of listeners on this model object
	protected Collection<AlertEventListener> listeners =
		new ArrayList<AlertEventListener>();
	
	/**
	 * Private constructor
	 * 
	 */
	private AlertEventNotifier() {
		
	}

	/**
	 * Adding a listener
	 *
	 * @param listener
	 */
	public void addListener(AlertEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removing a listener
	 *
	 * @param listener
	 */
	public void removeListener(AlertEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Update listeners
	 *
	 * @param eventObject
	 * @param eventType
	 */
	public void fireEvent(EventObject eventObject) {
		for (AlertEventListener listener : listeners) {
			listener.alertEventHandler(eventObject);
		}
	}

	/**
	 * Singleton holder
	 *
	 */
	private static class SingletonHolder {
		public static final AlertEventNotifier INSTANCE =
			new AlertEventNotifier();
	}

	public static AlertEventNotifier getInstance() {
		return SingletonHolder.INSTANCE;
	}	

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.iai.proteus.common.sos.model.SensorOffering;


/**
 * Notification object that listeners can register to in order to receive
 * World Wind selection updates 
 * 
 * @author Jakob Henriksson
 *
 */
public class SelectionNotifier {

	ArrayList<IPropertyChangeListener> listeners;
	
	SensorOffering offerings; 

	/**
	 * Private constructor 
	 */
	private SelectionNotifier() {
		listeners = new ArrayList<IPropertyChangeListener>();
	}
	
	/**
	 * Allows listener registration
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Allows listener de-registration 
	 * 
	 * @param listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}	
	
	/**
	 * Updates all listeners that a new selection has been detected 
	 * 
	 * @param selection
	 */
	public void selectionChanged(MarkerSelection selection) {
		
		Iterator<IPropertyChangeListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			IPropertyChangeListener element = 
				(IPropertyChangeListener) iter.next();
			// only provide a new value object 
			element.propertyChange(new PropertyChangeEvent(this, 
					NotifyProperties.OFFERING, null, selection));
		}		
	}
	
	/**
	 * Singleton holder 
	 * 
	 */
	private static class SingletonHolder {
		public static final SelectionNotifier INSTANCE = 
			new SelectionNotifier();
	}

	public static SelectionNotifier getInstance() {
		return SingletonHolder.INSTANCE;
	}
}

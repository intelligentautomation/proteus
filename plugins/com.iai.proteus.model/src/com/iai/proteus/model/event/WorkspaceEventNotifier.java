package com.iai.proteus.model.event;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Notification object that listeners can register to in order to receive
 * notification of workspace events 
 * 
 * @author Jakob Henriksson
 *
 */
public class WorkspaceEventNotifier {

	/*
	 * Collection of listeners on this model object  
	 */
	protected Collection<WorkspaceEventListener> listeners = 
		new ArrayList<WorkspaceEventListener>(); 
	
	/**
	 * Adding a listener 
	 * 
	 * @param listener
	 */
	public void addListener(WorkspaceEventListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removing a listener 
	 * 
	 * @param listener
	 */
	public void removeListener(WorkspaceEventListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 */	
	public void fireEvent(Object eventObject, WorkspaceEventType eventType) {
		fireEvent(eventObject, eventType, null);
	}
	
	/**
	 * Update listeners 
	 * 
	 * @param eventObject
	 * @param eventType 
	 * @param value 
	 */	
	public void fireEvent(Object eventObject, WorkspaceEventType eventType, Object value) {
		WorkspaceEvent event = new WorkspaceEvent(eventObject); 
		event.setEventType(eventType); 
		if (value != null)
			event.setValue(value); 
		for (WorkspaceEventListener listener : listeners) {
			listener.workspaceModelUpdate(event);
		}		
	}
	
	
	/**
	 * Singleton holder 
	 * 
	 */
	private static class SingletonHolder {
		public static final WorkspaceEventNotifier INSTANCE = 
			new WorkspaceEventNotifier();
	}

	public static WorkspaceEventNotifier getInstance() {
		return SingletonHolder.INSTANCE;
	}
}

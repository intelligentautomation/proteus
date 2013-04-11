/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import com.iai.proteus.events.EventNotifier;
import com.iai.proteus.events.EventType;


/**
 * Handler for toggling map layers 
 * 
 * @author Jakob Henriksson 
 *
 */
public class ToggleMapLayerHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String layer = event.getParameter("com.iai.proteus.map.layer.name");
		
		if (layer != null) {
			// toggle layer with given name
			EventNotifier.getInstance().fireEvent(this,
					EventType.MAP_TOGGLE_LAYER, layer);
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}
}

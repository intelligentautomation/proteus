/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.command;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.iai.proteus.Activator;
import com.iai.proteus.dialogs.OpenQuerySetDialog;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.queryset.persist.v1.QuerySet;
import com.iai.proteus.ui.UIUtil;

/**
 * Handler for opening a query set
 * 
 * @author Jakob Henriksson
 *
 */
public class OpenQuerySetHandler implements IHandler {

	/**
	 * Execute the handler
	 * 
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// create and open the dialog to manage services 
		OpenQuerySetDialog dialog =
				new OpenQuerySetDialog(UIUtil.getShell());
		if (dialog.open() == IDialogConstants.OK_ID) {
			final QuerySet querySet = dialog.getSelectedQuerySet();
			// is should not be null, but check just in case
			if (querySet != null) {
				
				// get service 
				BundleContext ctx = Activator.getContext();
				ServiceReference<EventAdmin> ref = 
						ctx.getServiceReference(EventAdmin.class);
				EventAdmin eventAdmin = ctx.getService(ref);
				
				Map<String,Object> properties = new HashMap<String, Object>();
				properties.put("object", querySet);

				// send event 
				Event e = new Event(EventTopic.QS_LOAD.toString(), 
								properties);
				eventAdmin.sendEvent(e);
			}
		}
		
		dialog.close();
		
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

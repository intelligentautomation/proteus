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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.iai.proteus.ui.queryset.QuerySetTab;
import com.iai.proteus.views.DiscoverView;

/**
 * Handler for closing a query set
 * 
 * @author Jakob Henriksson
 *
 */
public class CloseQuerySetHandler implements IHandler {

	/**
	 * Execute the handler 
	 * 
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final IWorkbenchPage page = 
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();		
		final IViewReference viewReference = 
				page.findViewReference(DiscoverView.ID);
		if (viewReference != null) {
			final IViewPart view = viewReference.getView(true);
			if (view instanceof DiscoverView) {
				DiscoverView discoverView = (DiscoverView) view;
				QuerySetTab querySetTab = discoverView.getCurrentQuerySet();
				// close query set tab
				discoverView.closeQuerySetTab(querySetTab);
				// dispose
				querySetTab.dispose();
			}
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

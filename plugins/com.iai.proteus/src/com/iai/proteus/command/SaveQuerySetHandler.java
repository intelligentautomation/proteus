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

import com.iai.proteus.queryset.QuerySetManager;
import com.iai.proteus.queryset.persist.v1.QuerySet;
import com.iai.proteus.queryset.persist.v1.QuerySetPersist;
import com.iai.proteus.ui.queryset.QuerySetTab;
import com.iai.proteus.views.DiscoverView;

/**
 * Handler for saving a query set
 * 
 * @author Jakob Henriksson
 *
 */
public class SaveQuerySetHandler implements IHandler {
	
	public static final String ID = "com.iai.proteus.command.queryset.save";

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
				if (querySetTab != null) {
					
					// provide a name 
					if (!querySetTab.isNamed()) {
						querySetTab.nameQuerySet();
					}
					
					// persist the query set 
					QuerySet qs = QuerySetPersist.write(querySetTab);
					// update status
					querySetTab.setDirty(false);

					String uuid = querySetTab.getUuid();
					// add as a stored query set
					QuerySetManager.getInstance().addStored(uuid, qs);
					// also add as an open query set 
					QuerySetManager.getInstance().addOpen(uuid);
				}
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

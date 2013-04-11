/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.command;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.iai.proteus.Activator;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.queryset.QuerySetManager;
import com.iai.proteus.queryset.persist.v1.QuerySet;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.ui.queryset.QuerySetTab;
import com.iai.proteus.views.DiscoverView;

/**
 * Handler for deleting a query set
 * 
 * @author Jakob Henriksson
 *
 */
public class DeleteQuerySetHandler implements IHandler {

	/**
	 * Execute the handler 
	 * 
	 */
	@SuppressWarnings("serial")
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
				final QuerySetTab querySetTab = discoverView.getCurrentQuerySet();
				if (querySetTab != null) {

					// warning dialog 
					MessageDialog dialog = 
							UIUtil.getConfirmDialog(UIUtil.getShell(), 
									"Delete query set", 
									"Are you sure you want to delete the " +
									"query set?");

					if (dialog.open() == MessageDialog.OK) {
						
						// get the query set UUID
						String uuid = querySetTab.getUuid();
						
						// notify that all map layers from the query set should
						// be deleted
						BundleContext ctx = Activator.getContext();
						ServiceReference<EventAdmin> ref = 
								ctx.getServiceReference(EventAdmin.class);
						EventAdmin eventAdminService = ctx.getService(ref);							
						eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_DELETE.toString(), 
								new HashMap<String, Object>() { 
							{
								put("object", querySetTab);
								put("value", querySetTab.getMapLayers());
							}
						}));
						
//						QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
//								QuerySetEventType.QUERYSET_LAYERS_DELETE,
//								querySetTab.getMapLayers());
						
						QuerySetManager qm = QuerySetManager.getInstance();

						// get the stored query set model object that 
						// should contain the file where query set is stored 
						QuerySet qs = qm.getStored(uuid);
						
						// close the query set 
						querySetTab.dispose();
						
						// delete the query set from manager
						qm.removeOpen(uuid);
						qm.removeStored(uuid);
						
						// delete the actual query set (if it was stored) 
						if (qs != null) {
							File file = qs.getFile();
							if (file != null)
								FileUtils.deleteQuietly(file);
						}
					}
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

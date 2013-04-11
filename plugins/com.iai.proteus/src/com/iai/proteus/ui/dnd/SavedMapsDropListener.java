/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.dnd;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.iai.proteus.Activator;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.map.WmsSavedMap;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.queryset.RearrangeMapsEventValue;

/**
 * Drop listener for saved maps 
 * 
 * @author Jakob Henriksson 
 *
 */
public class SavedMapsDropListener extends ViewerDropAdapter {

	private List<MapLayer> allMaps;
	
	/**
	 * Constructor 
	 * 
	 * @param viewer
	 * @param maps
	 */
	public SavedMapsDropListener(Viewer viewer, List<MapLayer> maps) {
		super(viewer);
		this.allMaps = maps;
	}
 	
	/**
	 * Perform drop 
	 * 
	 * @param data 
	 */
	@SuppressWarnings("serial")
	@Override
	public boolean performDrop(Object data) {
		LocalSelectionTransfer transfer = 
				LocalSelectionTransfer.getTransfer();
		IStructuredSelection selection = 
				(IStructuredSelection) transfer.getSelection();
		// get selection 
		@SuppressWarnings("unchecked")
		final List<WmsSavedMap> maps = (List<WmsSavedMap>)selection.toList();
		// get target 
		final WmsSavedMap target = (WmsSavedMap) getCurrentTarget();
		// get the index of the target 
		int idx = allMaps.indexOf(target);
		// remove moved maps
		allMaps.removeAll(maps);
		// re-locate the moved maps
		allMaps.addAll(idx, maps);
		// update viewer
		getViewer().refresh();
		// notify listeners that order has changed
		BundleContext ctx = Activator.getContext();
		ServiceReference<EventAdmin> ref = 
				ctx.getServiceReference(EventAdmin.class);
		EventAdmin eventAdminService = ctx.getService(ref);							
		eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_REARRANGE.toString(), 
				new HashMap<String, Object>() { 
			{
				put("value", new RearrangeMapsEventValue(maps, target));
			}
		}));			
//		UIUtil.update(new Runnable() {
//			@Override
//			public void run() {
//				QuerySetEventNotifier.getInstance().fireEvent(null, 
//						QuerySetEventType.QUERYSET_LAYERS_REARRANGE, 
//						new RearrangeMapsEventValue(maps, target));
//			}
//		});
		return true;
	}

	/**
	 * Check if drop is valid 
	 * 
	 * @param target
	 * @param operation
	 * @param transferType
	 */
	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		if (target instanceof WmsSavedMap && 
				LocalSelectionTransfer.getTransfer().isSupportedType(
						transferType)) {
			return true;
		}
		return false;
	}

}

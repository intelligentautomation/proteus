/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * Drag listener for saved maps
 * 
 * @author Jakob Henriksson
 *
 */
public class SavedMapsDragListener extends DragSourceAdapter {

	private final ISelectionProvider selectionProvider;
 
	/**
	 * Constructor 
	 * 
	 * @param selectionProvider
	 */
	public SavedMapsDragListener(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}
 
	@Override
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = 
				(IStructuredSelection) selectionProvider.getSelection();
 
		LocalSelectionTransfer transfer = 
				LocalSelectionTransfer.getTransfer();
		if (transfer.isSupportedType(event.dataType)) {
			transfer.setSelection(selection);
			transfer.setSelectionSetTime(event.time & 0xFFFF);
		}
	}	
}

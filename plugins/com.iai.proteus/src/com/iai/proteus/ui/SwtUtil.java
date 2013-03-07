/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author Jakob Henriksson
 *
 */
public class SwtUtil {
	
	/**
	 * Returns list of the selection in the structured viewer 
	 * 
	 * @param viewer
	 * @return
	 */
	public static List<?> getSelection(StructuredViewer viewer) {
		if (viewer != null)
			return getStructuredSelection(viewer.getSelection());
		// default 
		return new ArrayList<Object>();
	}	
	
	/**
	 * Returns list of the selection if it is structured, 
	 * an empty list otherwise
	 * 
	 * @param selection
	 * @return
	 */
	public static List<?> getStructuredSelection(ISelection selection) {
		if (selection instanceof StructuredSelection)
			return ((StructuredSelection) selection).toList();
		// default 
		return new ArrayList<Object>();
	}

}

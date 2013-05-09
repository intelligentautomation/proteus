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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;

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
	
	/**
	 * Returns the first selected item from the given selection. The
	 * selection must be structured and must have at least one item
	 * selected, otherwise null is returned 
	 * 
	 * @param selection
	 * @return
	 */
	public static Object getFirstSelectedElement(ISelection selection) {
		List<?> list = getStructuredSelection(selection);
		if (list != null && list.size() > 0)
			return list.get(0);
		// default
		return null;
	}
	
	/**
	 * Creates a help control that can be controlled via a tool bar item 
	 * 
	 * @param parent Parent control in which the help control should be placed
	 * @param toolItem Tool item controlling the help 
	 * @param controlBelow Control the help control should be placed above
	 * @param helpText The text to be displayed (may include newline characters)
	 */
	public static void createHelpController(final Composite parent, 
			final ToolItem toolItem, Control controlBelow, String helpText) 
	{
		// create resources 
		final Color colorFg = new Color(Display.getCurrent(), 64, 133, 176);
		final Color colorBg = new Color(Display.getCurrent(), 218, 237, 248);
		// create control to hold help information 
		final Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		// set colors 
		composite.setForeground(colorFg);
		composite.setBackground(colorBg);
		// dispose listener 
		composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				if (colorFg != null)
					colorFg.dispose();
				if (colorBg != null)
					colorBg.dispose();
			}
		});
		// help/information text 
		StyledText text = new StyledText(composite, SWT.WRAP);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setText(helpText);
		text.setEnabled(false);
		// color and style 
		text.setForeground(colorFg);
		text.setBackground(colorBg);
		StyleRange styleRange = new StyleRange();
		styleRange.start = 0;
		styleRange.length = helpText.length();
		styleRange.fontStyle = SWT.BOLD;
		text.setStyleRange(styleRange);
		// add button and listener to hide help 
		Button btnClose = new Button(composite, SWT.NONE);
		btnClose.setText("Close");
		btnClose.setBackground(colorBg);
		btnClose.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// dispose control
				composite.dispose();
				// update layout 
				parent.layout();
				// re-enable tool item
				toolItem.setEnabled(true);
			}
		});
		// move composite to the right place 
		composite.moveAbove(controlBelow);
		// disable tool item
		toolItem.setEnabled(false);
		// update the layout 
		parent.layout();			
	}

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.google.gson.JsonSyntaxException;
import com.iai.proteus.Activator;
import com.iai.proteus.communityhub.Group;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.util.ProteusUtil;

public class CommunityGroupView extends ViewPart {
	
	private static final Logger log = Logger.getLogger(CommunityGroupView.class);

	public static final String ID = "com.iai.proteus.views.CommunityGroupView"; //$NON-NLS-1$
	
	private TableViewer tableViewer;
	
	private Collection<Group> groups; 
	
	/**
	 * Constructor 
	 * 
	 */
	public CommunityGroupView() {
		// default 
		groups = new ArrayList<Group>();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		
		Label lblCommunityGroups = new Label(container, SWT.NONE);
		lblCommunityGroups.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lblCommunityGroups.setText("Community groups");
		
		ToolBar toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ToolItem toolItemRefresh = new ToolItem(toolBar, SWT.NONE);
		toolItemRefresh.setText("Refresh");
		toolItemRefresh.setImage(UIUtil.getImage("icons/fugue/arrow-circle-double-135.png"));
		
		toolItemRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// update available community groups 
				updateCommunityGroups();
				tableViewer.refresh();
			}
		});
		
		tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setUseHashlookup(true);		
		// set input 
		tableViewer.setInput(groups);
		
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		// add this view as a selection provider 
		getSite().setSelectionProvider(tableViewer);
	}
	
	/**
	 * Fetches the Community Groups from the Community Hub 
	 * 
	 */
	private void updateCommunityGroups() {
		
		// clear old groups
		groups.clear();
		
		// get service address from preference store
		IPreferenceStore store =
				Activator.getDefault().getPreferenceStore();
		
		boolean success = false;

		try {
			
			// get groups from the Community Hub 
			// (may throw exceptions) 
			Collection<Group> newGroups = 
					ProteusUtil.getCommunityGroups(store);
			
			groups.addAll(newGroups);
			
			success = true;
			
		} catch (MalformedURLException e) {
			log.error("Malformed URL exception: " + e.getMessage());
		} catch (SocketTimeoutException e) {
			log.error("Socket timeout exception: " + e.getMessage());
		} catch (JsonSyntaxException e) {
			log.error("JSON syntax exception: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		} finally {
			
			if (!success) {
				String msg =
						"There was an error updating the groups " + 
								"from the Community Hub. \n\n" + 
								"Please check your preferences.";
				UIUtil.showErrorMessage(msg);
			}
		}
		
		
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
//		IToolBarManager toolbarManager = getViewSite().getActionBars()
//				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars()
//				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}

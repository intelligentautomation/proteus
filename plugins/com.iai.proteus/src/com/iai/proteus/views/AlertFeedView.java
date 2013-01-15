/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javaxt.rss.Feed;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iai.proteus.Activator;
import com.iai.proteus.communityhub.FeedItem;
import com.iai.proteus.communityhub.Group;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.util.ProteusUtil;

public class AlertFeedView extends ViewPart implements ISelectionListener {

	public static final String ID = "com.iai.proteus.views.AlertFeedView"; //$NON-NLS-1$
	
	private TableViewer tableViewer;
	
	private Collection<FeedItem> items;
	
	// remembers the currently selected community group id 
	private int currentCommunityGroupId;

	/**
	 * Constructor
	 * 
	 */
	public AlertFeedView() {
		// default 
		items = new ArrayList<FeedItem>();
		currentCommunityGroupId = -1;
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, true));
		
		Label lblAlerts = new Label(container, SWT.NONE);
		lblAlerts.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lblAlerts.setText("Alerts");
		
		ToolBar toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ToolItem toolItemRefresh = new ToolItem(toolBar, SWT.NONE);
		toolItemRefresh.setText("Refresh");
		toolItemRefresh.setImage(UIUtil.getImage("icons/fugue/arrow-circle-double-135.png"));
		
		toolItemRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// updates the feed
				if (currentCommunityGroupId > 0) {
					// update model by fetching feed
					fetchFeed(currentCommunityGroupId);
					// update viewer since model has changed 
					tableViewer.refresh(true);
				}
			}
		});		
		
		Composite tableContainer = new Composite(container, SWT.NONE);
		tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumnLayout layout = new TableColumnLayout();
		tableContainer.setLayout(layout);
		
		tableViewer = new TableViewer(tableContainer, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setUseHashlookup(true);
		// set input
		tableViewer.setInput(items);
		
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(false);
		table.setLinesVisible(true);
		
		TableViewerColumn colTitle = new TableViewerColumn(tableViewer, SWT.NONE);
		colTitle.getColumn().setText("Title");
		// layout 
		layout.setColumnData(colTitle.getColumn(),
				new ColumnWeightData(4, ColumnWeightData.MINIMUM_WIDTH,
						true));
		// label provider
		colTitle.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				FeedItem feedItem = (FeedItem) element;
				return feedItem.getItem().getTitle();
			}
		});

		createActions();
		initializeToolBar();
		initializeMenu();
		
		// add a listener 
		getSite().getPage().addSelectionListener(CommunityGroupView.ID, this);
		
		// add this view as a selection provider 
		getSite().setSelectionProvider(tableViewer);

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

	/**
	 * Selection listener 
	 * 
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection treeSelection =
						(StructuredSelection) selection;
				Object element = treeSelection.getFirstElement();
				if (element instanceof Group) {

					Group group = (Group) element;
					
					// remembers the community group id
					currentCommunityGroupId = group.getId();
					
					// update model by fetching feed
					fetchFeed(currentCommunityGroupId);
					// update viewer since model has changed 
					tableViewer.refresh(true);
				}
			}
		}
	}
	
	/**
	 * Fetches the feed for the given group ID from the Community Hub 
	 * 
	 * @param groupId
	 */
	private void fetchFeed(int groupId) {
		
		// get service address from preference store
		IPreferenceStore store =
				Activator.getDefault().getPreferenceStore();
		
		try {
			
			String feedStr = ProteusUtil.getAlertFeed(store, groupId);
			
			if (feedStr != null) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document xml = dBuilder.parse(new InputSource(new ByteArrayInputStream(feedStr.getBytes("utf-8"))));
		
				// clear items
				items.clear();
				
				// iterate through feeds, typically only one
				for (Feed feed : new javaxt.rss.Parser(xml).getFeeds()) {
					// iterate through individual items in the feed
					for (javaxt.rss.Item item : feed.getItems()) {
						items.add(new FeedItem(item));
					}
				}
			}
			
		} catch (IOException e) {
			
		} catch (ParserConfigurationException e) {

		} catch (SAXException e) {

		}

	}
}

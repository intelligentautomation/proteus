/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.google.gson.JsonSyntaxException;
import com.iai.proteus.Activator;
import com.iai.proteus.common.LatLon;
import com.iai.proteus.communityhub.AlertEventListener;
import com.iai.proteus.communityhub.AlertEventNotifier;
import com.iai.proteus.communityhub.apiv1.Alert;
import com.iai.proteus.communityhub.apiv1.Group;
import com.iai.proteus.events.QuerySetEventNotifier;
import com.iai.proteus.events.QuerySetEventType;
import com.iai.proteus.map.WorldWindUtils;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.util.ProteusUtil;

/**
 * View that displays the alerts from a specific feed
 * 
 * @author Jakob Henriksson 
 *
 */
public class CommunityHubAlertsView extends ViewPart 
	implements ISelectionListener, AlertEventListener {
	
	public static final String ID = "com.iai.proteus.views.communityhub.AlertsView"; 
	
	private static final Logger log = Logger.getLogger(CommunityHubAlertsView.class);

	
	private TableViewer tableViewer;

	private Image imgExternal;
	
	private Collection<Alert> alerts;
	
	// date formatter 
	private SimpleDateFormat dateFormatter = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm");	

	/**
	 * Constructor
	 * 
	 */
	public CommunityHubAlertsView() {
		
		// images 
		imgExternal = UIUtil.getImage("icons/fugue/external.png");
		
		// default 
		alerts = new ArrayList<Alert>();
		
		// register as listener to alert events
		AlertEventNotifier.getInstance().addListener(this);
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		// add dispose listener 
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				// dispose of resources 
				if (imgExternal != null)
					imgExternal.dispose();
			}
		});			
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, true));
		
		Label lblAlerts = new Label(container, SWT.NONE);
		lblAlerts.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lblAlerts.setText("Alerts");
		
		ToolBar toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
		
		final ToolItem tltmViewOnCommunity = new ToolItem(toolBar, SWT.NONE);
		tltmViewOnCommunity.setText("View on Community Hub");
		tltmViewOnCommunity.setImage(imgExternal);
		// default 
		tltmViewOnCommunity.setEnabled(false);
		// listener
		tltmViewOnCommunity.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Object elmt = 
						SwtUtil.getFirstSelectedElement(tableViewer.getSelection());
				if (elmt != null && elmt instanceof Alert) {
					Alert alert = (Alert) elmt;
					try {
						final IWebBrowser browser = 
								PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
						// get Community Hub service address from preference store
						IPreferenceStore store =
								Activator.getDefault().getPreferenceStore();
						String endpoint = ProteusUtil.getCommunityHubEndpoint(store);
						if (endpoint != null) {
							// TODO: avoid hard-coding here 
							URL url = new URL(endpoint + 
									"/alert/view/" + alert.getId());
							browser.openURL(url);
						}
					} catch (MalformedURLException e) {
						log.error("Malformed URL: " + e.getMessage());
					} catch (PartInitException e) {
						log.error("Error getting access to browser: " + 
								e.getMessage());
					}
				}
			}
		});
		
		tableViewer = new TableViewer(container, SWT.BORDER | SWT.SINGLE);
		
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
		
		// column views 
		TableViewerColumn tblViewerColumnType = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnType = tblViewerColumnType.getColumn();
		tblclmnType.setWidth(200);
		tblclmnType.setText("Type");
		// label provider
		tblViewerColumnType.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// ensure we are dealing with the right model type
				if (element instanceof Alert) {
					Alert alert = (Alert) element;
					return alert.getType();
				}
				return "";
			}
		});
		
		TableViewerColumn tblViewerColumnDateCreated = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDateCreated = tblViewerColumnDateCreated.getColumn();
		tblclmnDateCreated.setWidth(100);
		tblclmnDateCreated.setText("Created");
		// label provider
		tblViewerColumnDateCreated.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// ensure we are dealing with the right model type
				if (element instanceof Alert) {
					Alert alert = (Alert) element;
					return dateFormatter.format(alert.getDateCreated());
				}
				return "";
			}
		});		

		// set input after the table view columns are defined  
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setUseHashlookup(true);
		tableViewer.setInput(alerts);
		
		// add double click listener
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object elmt = 
						SwtUtil.getFirstSelectedElement(event.getSelection());
				if (elmt != null && elmt instanceof Alert) {
					Alert alert = (Alert) elmt;
					LatLon pos = getCentralPosition(alert);
					// ask us to move to this lat-lon
					QuerySetEventNotifier.getInstance().fireEvent(alert,
							QuerySetEventType.QUERYSET_FLY_TO_LATLON, pos);
				}
			}
		});
		
		// add listener to update tool bar items' status 
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				tltmViewOnCommunity.setEnabled(!event.getSelection().isEmpty());
			}
		});

		createActions();
		initializeToolBar();
		initializeMenu();
		
		// add as listener to the selection of feeds  
		getSite().getPage().addSelectionListener(CommunityHubGroupsView.ID, this);
		
		// add this view as a selection provider 
		getSite().setSelectionProvider(tableViewer);
	}
	
	/**
	 * Selection listener 
	 * 
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		// handles selection from @{link CommunityGroupView} views 
		if (part instanceof CommunityHubGroupsView) {
			
			// get the first selected item 
			Object element = SwtUtil.getFirstSelectedElement(selection);
			// make sure we have an element 
			if (element != null && element instanceof Group) {
				// update the group 
				updateGroup((Group) element);
			}
		}
	}
	
	@Override
	public void alertEventHandler(EventObject event) {
		Object source = event.getSource();
		if (source instanceof Group) {
			// update the group
			updateGroup((Group) source);
		}
	}
	
	/**
	 * Update the given group by fetching the alerts
	 * 
	 * @param group
	 */
	private void updateGroup(final Group group) {
		// perform API call in a separate thread 
		new Thread() {
			@Override
			public void run() {
				// update model by fetching alerts
				getAlerts(group);
				// refresh viewer as input might have changed
				UIUtil.update(new Runnable() {
					@Override
					public void run() {
						tableViewer.refresh(true);
					}
				});
			}
		}.start();
	}
	
	/**
	 * Retrieves the alerts for the given group 
	 * 
	 * @param group
	 */
	private void getAlerts(Group group) {
		
		// clear old groups
		alerts.clear();

		// get service address from preference store
		IPreferenceStore store =
				Activator.getDefault().getPreferenceStore();
		
		final boolean[] success = new boolean[1];
		success[0] = false;

		try {
					
			// get alerts from the Community Hub 
			// (may throw exceptions)
			Collection<Alert> newAlerts = 
					ProteusUtil.getAlerts(store, group);
			
			alerts.addAll(newAlerts);
			
			success[0] = true;
			
		} catch (MalformedURLException e) {
			log.error("Malformed URL exception: " + e.getMessage());
		} catch (SocketTimeoutException e) {
			log.error("Socket timeout exception: " + e.getMessage());
		} catch (JsonSyntaxException e) {
			log.error("JSON syntax exception: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		} finally {

		}
		
	}

	/**
	 * Returns the central position of the alert bounding box, if it exists, 
	 * null otherwise 
	 * 
	 * @param alert
	 * @return
	 */
	private LatLon getCentralPosition(Alert alert) {
	
		Double latU = alert.getLatUpper();
		Double lonU = alert.getLonUpper();
		Double latL = alert.getLatLower();
		Double lonL = alert.getLonLower();
		
		if (latL != null && lonL != null && latU != null && lonU != null) {
			return WorldWindUtils.getCentralPosition(latU, lonU, latL, lonL);
		}
		
		// default 
		return null;
	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {

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

	}

}

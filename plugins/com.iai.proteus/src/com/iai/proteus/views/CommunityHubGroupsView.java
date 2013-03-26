/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.google.gson.JsonSyntaxException;
import com.iai.proteus.Activator;
import com.iai.proteus.communityhub.AlertEvent;
import com.iai.proteus.communityhub.AlertEventNotifier;
import com.iai.proteus.communityhub.apiv1.Group;
import com.iai.proteus.events.EventNotifier;
import com.iai.proteus.events.EventType;
import com.iai.proteus.preference.PrefPageCommunityHub;
import com.iai.proteus.ui.AlertPerspective;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.util.ProteusUtil;

/**
 * View that lists and controls the available alert feeds 
 * 
 * @author Jakob Henriksson
 *
 */
public class CommunityHubGroupsView extends ViewPart implements IPerspectiveListener {
	
	public static final String ID = "com.iai.proteus.views.communityhub.GroupsView";

	private static final Logger log = Logger.getLogger(CommunityHubGroupsView.class);
	
	// executor service to update alert feeds
	private ScheduledExecutorService executorService;

	// UI elements
	private Composite compositeStack;
	private StackLayout layoutStack; 
	
	private Composite compositeError;
	
	private TableViewer tableViewer;
	private Table table;
	
	private Image imgQuestion;
	private Color colorErrorFg;
	private Color colorErrorBg;
	
	// the available community groups (i.e. alert feeds) 
	private Collection<Group> groups;
	// the currently selected group object 
	private Group selectedGroup;
	
	// true if we are set to regularly update alert feeds, false otherwise 
	private boolean autoUpdating; 
	// the currently set update interval 
	private UpdateInterval updateInterval;
	
	// date formatter 
	private SimpleDateFormat dateFormatter = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	/**
	 * Constructor 
	 * 
	 */
	public CommunityHubGroupsView() {
		
		// images 
		imgQuestion = UIUtil.getImage("icons/fugue/question-white.png");
		// colors
		colorErrorFg = new Color(Display.getCurrent(), 182, 75, 69);
		colorErrorBg = new Color(Display.getCurrent(), 241, 222, 222);
		
		
		// defaults 
		groups = new ArrayList<Group>();
		selectedGroup = null;
		autoUpdating = false;
		updateInterval = UpdateInterval.ONEMINUTE;
		
		// create executor service (one thread)
		executorService = Executors.newScheduledThreadPool(1);
		
		// create callable instance to schedule alert feed updates 
		Callable<Void> c = new Callable<Void>() {
			public Void call() {
				try { 
					// only update if that is what we are currently doing
					if (autoUpdating) {
						// do work
						// perform API call in a separate thread 
						new Thread() {
							@Override
							public void run() {
								// update the groups
								updateCommunityGroups();
								// refresh viewer as input might have changed
								UIUtil.update(new Runnable() {
									@Override
									public void run() {
										tableViewer.refresh();
									}
								});
								// update listeners of potential update
								if (selectedGroup != null) {
									EventObject event =	new AlertEvent(selectedGroup);
									AlertEventNotifier.getInstance().fireEvent(event);
								}
							}
						}.start();	
					} 
				} finally {
					// reschedule the next update if service is not shutdown
					if (!executorService.isShutdown()) {
						// determine rescheduling time
						long ms = UpdateInterval.toMillis(updateInterval);
						// reschedule based on the setting 
						executorService.schedule(this, ms, TimeUnit.MILLISECONDS);
					}
				}
				// default 
				return null;
			}
		};

		// schedule initial run
		executorService.schedule(c, 1L, TimeUnit.SECONDS);
		
		// add this object as a perspective changed listener 
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
	}

	/**
	 * Create contents of the view part
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		// add dispose listener 
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				// dispose of resources 
				if (imgQuestion != null)
					imgQuestion.dispose();
				if (colorErrorFg != null)
					colorErrorFg.dispose();
				if (colorErrorBg != null)
					colorErrorBg.dispose();
				
				// shutdown execution service
				executorService.shutdown();
			}
		});		
		
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		
		Label lblCommunityGroups = new Label(container, SWT.NONE);
		lblCommunityGroups.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lblCommunityGroups.setText("Community groups");
		
		final Composite compositeToolbar = new Composite(container, SWT.NONE);
		compositeToolbar.setLayout(new GridLayout(3, false));
		compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final ToolItem toolItemUpdate = new ToolItem(toolBar, SWT.CHECK);
		toolItemUpdate.setText("Update");
		toolItemUpdate.setImage(UIUtil.getImage("icons/fugue/arrow-circle-double-135.png"));
		
		ToolBar toolBarHelp = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		
		final ToolItem tltmHelp = new ToolItem(toolBarHelp, SWT.NONE);
		tltmHelp.setText("");
		tltmHelp.setImage(imgQuestion);
		new Label(compositeToolbar, SWT.NONE);
		tltmHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// create the help controller 
				SwtUtil.createHelpController(container, 
						tltmHelp, compositeToolbar, 
						"The available community groups and their alert feeds " + 
						"will be displayed in the table below.");
			}
		});
		
		toolItemUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// auto updating is toggled
				if (toolItemUpdate.getSelection()) {
					// create updating options control  
					createAutoUpdateOptionsControl(container, toolItemUpdate);
					// indicate that we are auto updating
					autoUpdating = true;
				} else {
					// indicate that we are no longer auto updating
					autoUpdating = false;
				}
			}
		});
		
		// stack 
		compositeStack = new Composite(container, SWT.NONE);
		layoutStack = new StackLayout();
		compositeStack.setLayout(layoutStack);
		compositeStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tableViewer = new TableViewer(compositeStack, SWT.BORDER | SWT.SINGLE);
		
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// make the table the top control by default 
		layoutStack.topControl = table;
		
		// table viewer columns
		TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");
		// label provider 
		tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// ensure we are dealing with the right model object
				if (element instanceof Group) {
					Group model = (Group) element;
					return model.getName();
				}
				return "";		
			}
		});	
		
		TableViewerColumn tableViewerColumnDesc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDesc = tableViewerColumnDesc.getColumn();
		tblclmnDesc.setWidth(150);
		tblclmnDesc.setText("Description");
		// label provider 
		tableViewerColumnDesc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// ensure we are dealing with the right model object
				if (element instanceof Group) {
					Group model = (Group) element;
					return model.getDescription();
				}
				return "";		
			}
		});	
		
		TableViewerColumn tableViewerColumnDateCreated = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDateCreated = tableViewerColumnDateCreated.getColumn();
		tblclmnDateCreated.setWidth(100);
		tblclmnDateCreated.setText("Created");
		// label provider 
		tableViewerColumnDateCreated.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				// ensure we are dealing with the right model object
				if (element instanceof Group) {
					Group model = (Group) element;
					return dateFormatter.format(model.getDateCreated());
				}
				return "";		
			}
		});			
		
		// data input must be set after the table columns are defined 
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setUseHashlookup(true);		
		tableViewer.setInput(groups);
		
		// listener to update the currently selected group 
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					selectedGroup = null;
				else {
					Object elmt = 
							SwtUtil.getFirstSelectedElement(event.getSelection());
					if (elmt != null && elmt instanceof Group) {
						selectedGroup = (Group) elmt;
					}
				}
			}
		});
		
		// control to show error message if we cannot connect to the Hub 
		compositeError = new Composite(compositeStack, SWT.BORDER);
		compositeError.setLayout(new GridLayout(1, false));
		compositeError.setBackground(colorErrorBg);
		
		// error message 
		StyledText textError = new StyledText(compositeError, SWT.CENTER | SWT.WRAP);
		String strFirst = "Something went wrong!";
		String strSecond = "Cannot connect to the Community Hub at this moment.";
		textError.setText(strFirst + "\n\n" + strSecond + "\n");
		StyleRange styleRange = new StyleRange();
		styleRange.start = 0;
		styleRange.length = strFirst.length();
		styleRange.fontStyle = SWT.BOLD;
		textError.setStyleRange(styleRange);
		textError.setForeground(colorErrorFg);
		textError.setBackground(colorErrorBg);
		textError.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		// button to open preferences 
		Button btnPrefs = new Button(compositeError, SWT.NONE);
		btnPrefs.setText("Open preferences");
		btnPrefs.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, false, false));
		// listener 
		btnPrefs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// open the preferences, showing the specified page 
				PreferenceDialog pd =
						PreferencesUtil.createPreferenceDialogOn(null, 
								PrefPageCommunityHub.ID, 
								null, null);
				pd.open();
			}
		});
		
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
		
		final boolean[] success = new boolean[1];
		success[0] = false;

		try {
			
			// get groups from the Community Hub 
			// (may throw exceptions) 
			Collection<Group> newGroups = 
					ProteusUtil.getCommunityGroups(store);
			
			groups.addAll(newGroups);
			
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
			
			// update the stack layout based on success (in UI thread) 
			UIUtil.update(new Runnable() {
				@Override 
				public void run() {
					// update the top stack control 
					if (success[0]) {
						layoutStack.topControl = table; 
					} else {
						layoutStack.topControl = compositeError;
					}

					// update layout to force the change 
					compositeStack.layout();
				}
			});
		}
	}
	
	/**
	 * Create update options control  
	 * 
	 * @param parent
	 * @param item
	 */
	private void createAutoUpdateOptionsControl(final Composite parent, final ToolItem item) {
		
		// create control to hold options 
		final Composite composite = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.center = true;
		rowLayout.justify = true;
		rowLayout.spacing = 0;
		rowLayout.marginBottom = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginRight = 0;
		composite.setLayout(rowLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		new Label(composite, SWT.NONE).setText("Every:");
		
		// options 
		Button btnOneMinute = new Button(composite, SWT.RADIO);
		btnOneMinute.setText("minute");
		// listener
		btnOneMinute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// update setting
				updateInterval = UpdateInterval.ONEMINUTE;
			}
		});
		
		Button btnFiveMinutes = new Button(composite, SWT.RADIO);
		btnFiveMinutes.setText("5 minutes");
		// listener
		btnFiveMinutes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// update setting
				updateInterval = UpdateInterval.FIVEMINUTES;
			}
		});

		Button btnTenMinutes = new Button(composite, SWT.RADIO);
		btnTenMinutes.setText("10 minutes");
		// listener
		btnTenMinutes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// update setting
				updateInterval = UpdateInterval.TENMINUTES;
			}
		});
		
		Button btnNow = new Button(composite, SWT.NONE);
		btnNow.setText("Now");
		// listener
		btnNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// perform API call in a separate thread 
				new Thread() {
					public void run() {
						// update the groups
						updateCommunityGroups();
						// refresh viewer as input might have changed
						UIUtil.update(new Runnable() {
							@Override
							public void run() {
								tableViewer.refresh();				
							}
						});
					}
				}.start();
			}
		});
		
		// set the current/default selection 
		switch (updateInterval) {
		case ONEMINUTE:
			btnOneMinute.setSelection(true);
			break;
		case FIVEMINUTES:
			btnFiveMinutes.setSelection(true);
			break;
		case TENMINUTES:
			btnTenMinutes.setSelection(true);
			break;
		}
		
		// move composite to the right place 
		composite.moveBelow(compositeStack);
		
		// add listener to dispose of the created control 
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// dispose component if the button is being de-toggled
				if (!item.getSelection()) {
					composite.dispose();
					// layout parent 
					parent.layout();
				}
			}
		});
		
		// update the parent controller 
		parent.layout();
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
	 * (Alert feed) Update intervals 
	 * 
	 * @author Jakob Henriksson
	 *
	 */
	private static enum UpdateInterval {
		
		ONEMINUTE,
		FIVEMINUTES,
		TENMINUTES;
		
		/**
		 * Returns the number of milliseconds the given update interval 
		 * corresponds to  
		 * 
		 * @param interval
		 * @return
		 */
		public static long toMillis(UpdateInterval interval) {
			switch (interval) {
			// TODO: set back to normal!
			case ONEMINUTE:
				return TimeUnit.SECONDS.toMillis(4);
//				return TimeUnit.MINUTES.toMillis(1);
			case FIVEMINUTES:
				return TimeUnit.SECONDS.toMillis(10);
//				return TimeUnit.MINUTES.toMillis(5);
			case TENMINUTES:
				return TimeUnit.SECONDS.toMillis(20);
//				return TimeUnit.MINUTES.toMillis(10);
			default:
				return 0L;
			}
			
		}
	}

	/** 
	 * Called when the perspective changes 
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, 
	 *  org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {

		EventNotifier notifier = EventNotifier.getInstance();
		
		// hide all contexts when we leave the discovery perspective 
		if (perspective.getId().equals(AlertPerspective.ID)) {
			
			// notify that we should toggle the alert layer on
			notifier.fireEvent(this, EventType.MAP_TOGGLE_ALERT_LAYER, true);
			
		} else {
			
			// notify that we should toggle the alert layer off
			notifier.fireEvent(this, EventType.MAP_TOGGLE_ALERT_LAYER, false);
		}
	}	

	/* (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, 
	 *  org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	@Override
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {
	}
}

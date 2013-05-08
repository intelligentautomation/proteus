/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import gov.nasa.worldwind.geom.Sector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.iai.proteus.Activator;
import com.iai.proteus.map.MarkerSelection;
import com.iai.proteus.map.NotifyProperties;
import com.iai.proteus.map.SelectionNotifier;
import com.iai.proteus.map.SensorOfferingMarker;
import com.iai.proteus.model.map.IMapLayer;
import com.iai.proteus.model.map.WmsSavedMap;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.queryset.FacetData;
import com.iai.proteus.queryset.QuerySetManager;
import com.iai.proteus.queryset.SosOfferingLayer;
import com.iai.proteus.queryset.SosOfferingLayerStats;
import com.iai.proteus.queryset.persist.v1.QuerySet;
import com.iai.proteus.queryset.persist.v1.QuerySetPersist;
import com.iai.proteus.ui.DiscoverPerspective;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.ui.queryset.QuerySetOpenState;
import com.iai.proteus.ui.queryset.QuerySetTab;
import com.iai.proteus.ui.queryset.SelectionProviderIntermediate;
import com.iai.proteus.ui.queryset.SensorOfferingItem;

/**
 * Sensor discovery view
 *
 * @author Jakob Henriksson
 *
 */
public class DiscoverView extends ViewPart 
	implements IPropertyChangeListener, IPerspectiveListener
{
	
	public static final String ID = "com.iai.proteus.views.DiscoverView";
	
	private static final Logger log = Logger.getLogger(DiscoverView.class);
	
	// EventAdmin service for communicating with other views/modules
	private EventAdmin eventAdminService;
	
//	private static final Logger log = Logger.getLogger(DiscoverView.class);

	// the tab folder object
	private CTabFolder tabFolder;

	// tool items 
	private ToolItem tbItemSensors;
	private ToolItem tbItemMaps;
	
	// selection provider intermediator
	private SelectionProviderIntermediate intermediator;
	
	static Menu chevronMenu = null;
	
	private Image imgChart;
	private Image imgMap; 
	private Image imgQuestion;
	
	private QuerySetManager qm = QuerySetManager.getInstance(); 

	/**
	 * Constructor
	 *
	 */
	public DiscoverView() {
		
		intermediator = new SelectionProviderIntermediate();
		
		// add this object as a perspective changed listener 
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
		
		// images
		imgChart = UIUtil.getImage("icons/fugue/chart.png");
		imgMap = UIUtil.getImage("icons/fugue/map.png");
		imgQuestion = UIUtil.getImage("icons/fugue/question-white.png");
		
		// get EventAdmin service object 
		BundleContext ctx = Activator.getContext();		
		ServiceReference<EventAdmin> ref = 
				ctx.getServiceReference(EventAdmin.class);
		eventAdminService = ctx.getService(ref);
	}

	/**
	 * Create part contents 
	 * 
	 */
	@Override
	public void createPartControl(final Composite parent) {
		
		// dispose listener 
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (imgChart != null)
					imgChart.dispose();
				if (imgMap != null)
					imgMap.dispose();
				if (imgQuestion != null)
					imgQuestion.dispose();
			}
		});

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// composite to hold two different toolbars 
		final Composite compositeToolbar = new Composite(composite, SWT.NONE);
		compositeToolbar.setLayout(new GridLayout(2, false));
		compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// add a toolbar 
		ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		int minWidth = 0; 
		
		tbItemSensors = new ToolItem(toolBar, SWT.RADIO);
		tbItemSensors.setText("Sensors");
		tbItemSensors.setImage(imgChart);
		tbItemSensors.setToolTipText("Discover and view sensor data");
		// find minimum width
		if (tbItemSensors.getWidth() > minWidth)
			minWidth = tbItemSensors.getWidth();
		// selected by default
		tbItemSensors.setSelection(true);

		tbItemMaps = new ToolItem(toolBar, SWT.RADIO);
		tbItemMaps.setText("Maps");
		tbItemMaps.setImage(imgMap);
		tbItemMaps.setToolTipText("Find and view maps");
		// default 
		tbItemMaps.setEnabled(true);
		// find minimum width		
		if (tbItemMaps.getWidth() > minWidth)
			minWidth = tbItemMaps.getWidth();

		
		// add help toolbar 
		ToolBar toolBarHelp = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBarHelp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		
		// tool bar item that shows some help information 
		final ToolItem tltmHelp = new ToolItem(toolBarHelp, SWT.NONE);
		tltmHelp.setText("");
		tltmHelp.setImage(imgQuestion);
		tltmHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// create the help controller 
				SwtUtil.createHelpController(composite, 
						tltmHelp, compositeToolbar, 
						"Use the toolbar items 'Sensors' and 'Maps' to switch " + 
						"between collecting sensor offerings and maps for the Query Set.");
			}
		});
		
//		// create drop down cool item to the cool bar
//		CoolItem coolItem = new CoolItem(coolBar, SWT.DROP_DOWN);
//		coolItem.setControl(toolBar);
//		
//		Point toolBarSize = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		Point coolItemSize = coolItem.computeSize(toolBarSize.x, toolBarSize.y);
//		coolItem.setMinimumSize(minWidth, coolItemSize.y);
//		coolItem.setPreferredSize(coolItemSize);
//		coolItem.setSize(coolItemSize);
//		coolItem.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent event) {
//				/*
//				 * NOTE (jhenriksson): Taken from Snippet140.java 
//				 * 
//				 * org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet140.java
//				 */
//				if (event.detail == SWT.ARROW) {
//					CoolItem item = (CoolItem) event.widget;
//					Rectangle itemBounds = item.getBounds ();
//					Point pt = coolBar.toDisplay(new Point(itemBounds.x, itemBounds.y));
//					itemBounds.x = pt.x;
//					itemBounds.y = pt.y;
//					ToolBar bar = (ToolBar) item.getControl ();
//					ToolItem[] tools = bar.getItems ();
//					
//					int i = 0;
//					while (i < tools.length) {
//						Rectangle toolBounds = tools[i].getBounds ();
//						pt = bar.toDisplay(new Point(toolBounds.x, toolBounds.y));
//						toolBounds.x = pt.x;
//						toolBounds.y = pt.y;
//						
//						/* Figure out the visible portion of the tool by looking at the
//						 * intersection of the tool bounds with the cool item bounds. */
//				  		Rectangle intersection = itemBounds.intersection (toolBounds);
//				  		
//						/* If the tool is not completely within the cool item bounds, then it
//						 * is partially hidden, and all remaining tools are completely hidden. */
//				  		if (!intersection.equals (toolBounds)) break;
//				  		i++;
//					}
//					
//					/* Create a menu with items for each of the completely hidden buttons. */
//					if (chevronMenu != null) chevronMenu.dispose();
//					chevronMenu = new Menu (coolBar);
//					for (int j = i; j < tools.length; j++) {
//						MenuItem menuItem = new MenuItem (chevronMenu, SWT.PUSH);
//						menuItem.setText (tools[j].getText());
//						// jhenriksson: using the image as well, if it exists
//						Image image = tools[j].getImage();
//						if (image != null) {
//							menuItem.setImage(image);
//						}
//						// jhenriksson: set status
//						menuItem.setEnabled(tools[j].getEnabled());
//					}
//					
//					/* Drop down the menu below the chevron, with the left edges aligned. */
//					pt = coolBar.toDisplay(new Point(event.x, event.y));
//					chevronMenu.setLocation (pt.x, pt.y);
//					chevronMenu.setVisible (true);					
//				}
//			}
//		});
		
		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setSimple(false);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// create a new default query set 
		QuerySetTab querySet = createNewQuerySet();
		
		// add to list of opened query sets 
		qm.addOpen(querySet.getUuid());

		/*
		 * Add listeners
		 *
		 */
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			
			// listen to when tabs are closed and take appropriate action
			public void close(CTabFolderEvent event) {
				if (event.item instanceof QuerySetTab) {
					QuerySetTab querySetTab = (QuerySetTab) event.item;

					// close query set tab 
					closeQuerySetTab(querySetTab);
				}
			}
		});

		// listen to when we switch query sets 
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				Widget widget = e.item;
				if (widget instanceof QuerySetTab) {
					final QuerySetTab querySetTab = (QuerySetTab) widget;

					// clear the selection of services and maps for this 
					// query set 
					querySetTab.mapDiscoveryClearSelection();
					
					// refresh viewers, if necessary 
					querySetTab.refreshViewers(); 
					
					// notify that we should switch tab 'context' - send event 
					eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_ACTIVATE.toString(), 
							new HashMap<String, Object>() { 
						{
							put("object", querySetTab);
							put("value", querySetTab.getMapLayers());
						}
					}));
					
					// switch the selection provider
					intermediator.setSelectionProviderDelegate(
							querySetTab.getActiveSelectionProvider());
					
					// update tool items' status
					boolean sensors = querySetTab.isShowingSensorStack();
					updateToolBarItems(sensors, !sensors);
				}

			}
		});


		// Handle double click events: rename query set
		tabFolder.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if (event.widget instanceof CTabFolder) {
					CTabItem tab = tabFolder.getSelection();
					if (tab instanceof QuerySetTab) {
						// rename tab 
						((QuerySetTab) tab).nameQuerySet();
					}
				}
			}

		});
		
		// listener to show sensor data 
		tbItemSensors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem tab = tabFolder.getSelection(); 
				if (tab instanceof QuerySetTab) {
					QuerySetTab queryTab = (QuerySetTab) tab;
					queryTab.showSensorStack();
				}
			}
		});

		// listener to show map data 
		tbItemMaps.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem tab = tabFolder.getSelection(); 
				if (tab instanceof QuerySetTab) {
					QuerySetTab queryTab = (QuerySetTab) tab;
					queryTab.showMapStack();
				}
			}
		});

		/*
		 * Register the selection provider intermediator as the
		 * selection provider
		 */
		getSite().setSelectionProvider(intermediator);

		SelectionNotifier.getInstance().addPropertyChangeListener(this);
		
		// get service 
		BundleContext ctx = Activator.getContext();
		// create handler 
		EventHandler handler = new EventHandler() {
			public void handleEvent(final Event event) {
				
				Object obj = event.getProperty("object");
				
				// load the given query set 
				if (event.getTopic().equals(EventTopic.QS_LOAD.toString())) {
					
					if (obj instanceof QuerySet) {
						final QuerySet querySet = (QuerySet) obj;

						// load query set 
						if (parent.getDisplay().getThread() == Thread.currentThread()) {
							loadQuerySet(querySet);
						} else {
							parent.getDisplay().syncExec(new Runnable() {
								public void run() {
									loadQuerySet(querySet);
								}
							});
						}
					}
				} 
				// contribution change 
				else if (event.getTopic().equals(EventTopic.QS_OFFERINGS_CHANGED.toString())) {
					
					if (obj instanceof SosOfferingLayer) {
						SosOfferingLayer offeringLayer = (SosOfferingLayer) obj;

						CTabItem item = tabFolder.getSelection();
						
						if (item instanceof QuerySetTab) {
							QuerySetTab querySetTab = (QuerySetTab) item;

							// collect statistics
							SosOfferingLayerStats stats = offeringLayer.collectStats();

							Sector sector = offeringLayer.getSector();

							// update geographic area
							querySetTab.updateGeographicArea(sector,
									stats.getCountInSector());

							// update time
							querySetTab.updateTime(stats.getCountTime());

							// update properties
							FacetData facetData = new FacetData(stats.getPropertyCount());
							querySetTab.updateObservedProperties(facetData,
									stats.getCountProperties());

							// update formats
							querySetTab.updateFormats(stats.getFormats(),
									stats.getCountFormats());

							// set offerings
							querySetTab.updateSensorOfferings(stats.getSensorOfferingItems());
						}
					}					
				}
			}
		};

		// register service for listening to events 
		Dictionary<String,String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, 
				EventTopic.TOPIC_QUERYSET.toString());
		ctx.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	/**
	 * Loads and populates a query set tab from a query set model object 
	 * read from disk  
	 * 
	 * @param querySet
	 */
	private void loadQuerySet(QuerySet querySet) {
		
		// create the query set 
		QuerySetTab querySetTab = createNewQuerySet();

		// populate the tab 
		populateQuerySetTab(querySetTab, querySet);

		// add to list of opened query sets 
		qm.addOpen(querySet.getUuid());

		// refresh viewers 
		querySetTab.refreshViewers();		
	}

	
	/**
	 * Populate the given query set tab from the query set model 
	 * 
	 * @param querySetTab
	 * @param querySet
	 */
	private void populateQuerySetTab(QuerySetTab querySetTab, QuerySet querySet) {
		
		// update UUID
		querySetTab.setUuid(querySet.getUuid());
		// not dirty yet 
		querySetTab.setDirty(false);
		// has been saved before
		querySetTab.setSaved(true);
		// update title 
		querySetTab.setText(querySet.getTitle());

		// SOS
		
		// services 
		for (QuerySet.SosService sosService : 
			querySet.getSectionSos().getSosServices()) {

			Service service = new Service(ServiceType.SOS);
			service.setEndpoint(sosService.getEndpoint());
			service.setName(sosService.getTitle());
			// remember the active setting 
			service.setActive(sosService.isActive());
			String color = sosService.getColor();
			if (color != null) {
				try {
					service.setColor(Color.decode(color));
				} catch (NumberFormatException e) {
					log.error("Number format exception: " + e.getMessage());
				}
			}
			
			querySetTab.addService(service);
		}
		
		// programmatically check the active services in the UI 
		querySetTab.checkActiveServices();
		// ensure that checked services as displayed on the map  
		querySetTab.updateSelectedServices();
		
		// bounding box  
		double[] bbox = querySet.getSectionSos().getBoundingBox().getAsArray();
		if (bbox != null)
			querySetTab.setSensorOfferingBoundingBox(bbox);
		
		// observed properties
		Collection<String> ops = new ArrayList<String>();
		for (QuerySet.SosObservedProperty op : 
			querySet.getSectionSos().getObservedProperties()) {
			// add the observed property URI 
			ops.add(op.getObservedProperty());
		}
		// set the active observed properties 
		querySetTab.setActiveObservedProperties(ops);
		
		
		// WMS 
		for (QuerySet.WmsSavedMap map : querySet.getSectionWms().getMaps()) {
			
			WmsSavedMap savedMap = new WmsSavedMap();
			savedMap.setServiceEndpoint(map.getEndpoint());
			savedMap.setWmsLayerTitle(map.getTitle());
			savedMap.setName(map.getName());
			savedMap.setNotes(map.getNotes());
			// remember the active setting
			savedMap.setActive(map.isActive());
			
			querySetTab.addSavedMap(savedMap);
		}

		// programmatically check the active maps in the UI
		querySetTab.checkActiveSavedMaps();
		// ensure that checked maps are displayed on the map
		querySetTab.updateSavedMaps();
	}

	/**
	 * Receives updates from map selection
	 *
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		if (event.getProperty().equals(NotifyProperties.OFFERING)) {
			MarkerSelection selection = (MarkerSelection)event.getNewValue();

			CTabItem item = tabFolder.getSelection();
			if (item instanceof QuerySetTab) {
				QuerySetTab querySetTab = (QuerySetTab) item;
				List<SensorOfferingMarker> markers =
						selection.getSelection();
				// NOTE: for now we only select one, the first one
				if (markers.size() > 0) {
					SensorOfferingMarker marker = markers.get(0);
					SensorOfferingItem offeringItem =
							new SensorOfferingItem(marker.getService(),
									marker.getSensorOffering());
					// invoke selection
					querySetTab.selectSensorOffering(offeringItem);
				}
			}
		}
	}

	/**
	 * Returns the current (active) Query Set (may be null)
	 * 
	 * @return
	 */
	public QuerySetTab getCurrentQuerySet() {
		CTabItem tab = tabFolder.getSelection(); 
		if (tab != null && tab instanceof QuerySetTab) {
			return (QuerySetTab) tab;
		}
		return null;
	}

	@Override
	public void setFocus() {
	}

	/** 
	 * Called when the perspective changes 
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, 
	 *  org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@SuppressWarnings("serial")
	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		
		CTabItem tabItem = tabFolder.getSelection();
		if (!(tabItem != null && tabItem instanceof QuerySetTab))
			return;
		
		final QuerySetTab querySetTab = (QuerySetTab) tabItem;
		
		// hide all contexts when we leave the discovery perspective 
		if (perspective.getId().equals(DiscoverPerspective.ID)) {
			
			// notify that we should show layers from the active 
			// query set, if it exists
			if (querySetTab != null) {
				eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_ACTIVATE.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", querySetTab);
						put("value", querySetTab.getMapLayers());
					}
				}));
			}
			
		} else {
			
			// notify that we should show no layers (empty array list)
			// NOTE: the event object (currentQuerySet) may be null
			eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_ACTIVATE.toString(), 
					new HashMap<String, Object>() { 
				{
					put("object", querySetTab);
					put("value", new ArrayList<IMapLayer>());
				}
			}));
		}
		
		// notify that we should switch tab 'context' - send event 
	}
	
	/**
	 * Creates a new query set 
	 * 
	 */
	@SuppressWarnings("serial")
	public QuerySetTab createNewQuerySet() {

		// create a new tab 
		final QuerySetTab querySetTab =
				new QuerySetTab(getSite(), intermediator, tabFolder, SWT.NONE);
		// make the new tab the active one
		tabFolder.setSelection(querySetTab);

		// enable tool bar items 
		setToolBarEnabled(true);
		// update the tool bar item status: sensors enabled by default 
		updateToolBarItems(true, false);
		
		// notify that we should switch tab 'context'
		eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_ACTIVATE.toString(), 
				new HashMap<String, Object>() { 
			{
				put("object", querySetTab);
				put("value", querySetTab.getMapLayers());
			}
		}));			
		
		// get source provider service 
		ISourceProviderService sourceProviderService =
				(ISourceProviderService) getSite().
				getWorkbenchWindow().
				getService(ISourceProviderService.class);

		// get our service
		QuerySetOpenState stateService = 
				(QuerySetOpenState) sourceProviderService
				.getSourceProvider(QuerySetOpenState.STATE);

		// activate change 
		stateService.setQuerySetOpen();
		
		return querySetTab;
	}
	
	/**
	 * Closes a query set tab 
	 * 
	 * @param querySetTab
	 */
	@SuppressWarnings("serial")
	public void closeQuerySetTab(final QuerySetTab querySetTab) {
		
		String uuid = querySetTab.getUuid();

		// should we save the changes before closing? 
		if (querySetTab.isDirty()) {

			MessageDialog dialog = 
					UIUtil.getConfirmDialog(getSite().getShell(), 
							"Save query set", 
							"Do you want to save the changes?");
			int result = dialog.open();
			// return and do nothing if the user cancels the action
			if (result == MessageDialog.OK) {

				// provide a name 
				if (!querySetTab.isSaved()) {
					querySetTab.nameQuerySet();
				}

				// persist the query set 
				QuerySet qs = QuerySetPersist.write(querySetTab);

				// add as a stored query set
				qm.addStored(uuid, qs);
			}
		}
		
		// update toolbar items 
		if (tabFolder.getItemCount() <= 1) {
			updateToolBarItems(false, false);
		}

		// notify that all map layers from the query set should
		// be deleted - send event 
		eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_DELETE.toString(), 
				new HashMap<String, Object>() { 
				{
					put("object", querySetTab);
					put("value", querySetTab.getMapLayers());
				}
		}));

		// remove from list of opened query sets 
		qm.removeOpen(uuid);
	}

	/**
	 * Update the status of tool bar items 
	 * 
	 * @param sensors
	 * @param maps
	 */
	private void updateToolBarItems(boolean sensors, boolean maps) {
		// update 
		boolean enabled = sensors || maps;
		setToolBarEnabled(enabled);
		
		// set selection
		if (enabled) {
			tbItemSensors.setSelection(sensors);
			tbItemMaps.setSelection(maps);
		} else {
			tbItemSensors.setSelection(false);
			tbItemMaps.setSelection(false);
		}
	}
	
	/**
	 * Updates the enabled flag on the tool bar items 
	 * 
	 * @param status
	 */
	private void setToolBarEnabled(boolean status) {
		tbItemSensors.setEnabled(status);
		tbItemMaps.setEnabled(status);
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

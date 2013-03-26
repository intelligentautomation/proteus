/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import gov.nasa.worldwind.geom.Sector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.iai.proteus.map.MarkerSelection;
import com.iai.proteus.map.NotifyProperties;
import com.iai.proteus.map.SelectionNotifier;
import com.iai.proteus.map.SensorOfferingMarker;
import com.iai.proteus.model.map.IMapLayer;
import com.iai.proteus.queryset.FacetData;
import com.iai.proteus.queryset.QuerySetEvent;
import com.iai.proteus.queryset.QuerySetEventListener;
import com.iai.proteus.queryset.QuerySetEventNotifier;
import com.iai.proteus.queryset.QuerySetEventType;
import com.iai.proteus.queryset.SosOfferingLayer;
import com.iai.proteus.queryset.SosOfferingLayerStats;
import com.iai.proteus.queryset.ui.QuerySetTab;
import com.iai.proteus.queryset.ui.SelectionProviderIntermediate;
import com.iai.proteus.queryset.ui.SensorOfferingItem;
import com.iai.proteus.ui.DiscoverPerspective;
import com.iai.proteus.ui.UIUtil;

/**
 * Sensor discovery view
 *
 * @author Jakob Henriksson
 *
 */
public class DiscoverView extends ViewPart implements QuerySetEventListener,
	IPropertyChangeListener, IPerspectiveListener
{

	public static final String ID = "com.iai.proteus.views.DiscoverView";

	// the tab folder object
	private CTabFolder tabFolder;

	// the currently active tab, null if none
	private QuerySetTab currentQuerySet;

	// selection provider intermediator
	private SelectionProviderIntermediate intermediator;
	
	static Menu chevronMenu = null;


	/**
	 * Constructor
	 *
	 */
	public DiscoverView() {

		// add this view as a listener
		QuerySetEventNotifier.getInstance().addListener(this);

		intermediator = new SelectionProviderIntermediate();
		
		// add this object as a perspective changed listener 
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
	}

	/**
	 * Create part contents 
	 * 
	 */
	@Override
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// The "CoolBar" 
		final CoolBar coolBar = new CoolBar(composite, SWT.FLAT | SWT.RIGHT);
		coolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// add a toolbar to the cool bar
		ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		int minWidth = 0; 
		
		// add tool items to the toolbar 
		ToolItem toolItemNew = new ToolItem(toolBar, SWT.NONE);
		toolItemNew.setText("New Query Set");
		toolItemNew.setImage(UIUtil.getImage("icons/fugue/document--plus.png"));
		// find minimum width
		if (toolItemNew.getWidth() > minWidth)
			minWidth = toolItemNew.getWidth();

		final ToolItem toolItemRename = new ToolItem(toolBar, SWT.NONE);
		toolItemRename.setText("Rename");
		toolItemRename.setImage(UIUtil.getImage("icons/fugue/document-rename.png"));
		// find minimum width
		if (toolItemRename.getWidth() > minWidth)
			minWidth = toolItemRename.getWidth();

		// separator 
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		final ToolItem toolItemSensors = new ToolItem(toolBar, SWT.RADIO);
		toolItemSensors.setText("Sensors");
		toolItemSensors.setImage(UIUtil.getImage("icons/fugue/chart.png"));
		toolItemSensors.setToolTipText("Discover and view sensor data");
		// find minimum width
		if (toolItemSensors.getWidth() > minWidth)
			minWidth = toolItemSensors.getWidth();
		// selected by default
		toolItemSensors.setSelection(true);

		final ToolItem toolItemMaps = new ToolItem(toolBar, SWT.RADIO);
		toolItemMaps.setText("Maps");
		toolItemMaps.setImage(UIUtil.getImage("icons/fugue/map.png"));
		toolItemMaps.setToolTipText("Find and view maps");
		// default 
		toolItemMaps.setEnabled(true);
		// find minimum width		
		if (toolItemMaps.getWidth() > minWidth)
			minWidth = toolItemMaps.getWidth();

		// create drop down cool item to the cool bar
		CoolItem coolItem = new CoolItem(coolBar, SWT.DROP_DOWN);
		coolItem.setControl(toolBar);
		
		Point toolBarSize = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point coolItemSize = coolItem.computeSize(toolBarSize.x, toolBarSize.y);
		coolItem.setMinimumSize(minWidth, coolItemSize.y);
		coolItem.setPreferredSize(coolItemSize);
		coolItem.setSize(coolItemSize);
		coolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				/*
				 * NOTE (jhenriksson): Taken from Snippet140.java 
				 * 
				 * org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet140.java
				 */
				if (event.detail == SWT.ARROW) {
					CoolItem item = (CoolItem) event.widget;
					Rectangle itemBounds = item.getBounds ();
					Point pt = coolBar.toDisplay(new Point(itemBounds.x, itemBounds.y));
					itemBounds.x = pt.x;
					itemBounds.y = pt.y;
					ToolBar bar = (ToolBar) item.getControl ();
					ToolItem[] tools = bar.getItems ();
					
					int i = 0;
					while (i < tools.length) {
						Rectangle toolBounds = tools[i].getBounds ();
						pt = bar.toDisplay(new Point(toolBounds.x, toolBounds.y));
						toolBounds.x = pt.x;
						toolBounds.y = pt.y;
						
						/* Figure out the visible portion of the tool by looking at the
						 * intersection of the tool bounds with the cool item bounds. */
				  		Rectangle intersection = itemBounds.intersection (toolBounds);
				  		
						/* If the tool is not completely within the cool item bounds, then it
						 * is partially hidden, and all remaining tools are completely hidden. */
				  		if (!intersection.equals (toolBounds)) break;
				  		i++;
					}
					
					/* Create a menu with items for each of the completely hidden buttons. */
					if (chevronMenu != null) chevronMenu.dispose();
					chevronMenu = new Menu (coolBar);
					for (int j = i; j < tools.length; j++) {
						MenuItem menuItem = new MenuItem (chevronMenu, SWT.PUSH);
						menuItem.setText (tools[j].getText());
						// jhenriksson: using the image as well, if it exists
						Image image = tools[j].getImage();
						if (image != null) {
							menuItem.setImage(image);
						}
						// jhenriksson: set status
						menuItem.setEnabled(tools[j].getEnabled());
					}
					
					/* Drop down the menu below the chevron, with the left edges aligned. */
					pt = coolBar.toDisplay(new Point(event.x, event.y));
					chevronMenu.setLocation (pt.x, pt.y);
					chevronMenu.setVisible (true);					
				}
			}
		});
		
		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setSimple(false);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// create default query set
		QuerySetTab querySetTab =
				new QuerySetTab(getSite(), intermediator, tabFolder, SWT.NONE);
		// mark as the current query set 
		currentQuerySet = querySetTab;
		// make the new tab the active one
		tabFolder.setSelection(querySetTab);
		
		/*
		 * Add listeners
		 *
		 */
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			
			// listen to when tabs are closed and take appropriate action
			public void close(CTabFolderEvent event) {
				if (event.item instanceof QuerySetTab) {
					QuerySetTab querySetTab = (QuerySetTab) event.item;
					if (querySetTab.isDirty()) {
						UIUtil.showInfoMessage("To implement: check dirty flag");
					}

					// notify that all map layers from the query set should
					// be deleted
					QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
							QuerySetEventType.QUERYSET_LAYERS_DELETE,
							querySetTab.getMapLayers());

					// disable actions if this was the last item
					boolean status = !(tabFolder.getItemCount() <= 1);
					// tabs cannot be renamed 
					toolItemRename.setEnabled(status);
					// we cannot switch between sensors and map stacks
					toolItemSensors.setSelection(status);
					toolItemSensors.setEnabled(status);
					toolItemMaps.setSelection(status);
					toolItemMaps.setEnabled(status);
					
					// update current query set
					if (!status)
						currentQuerySet = null;
				}
			}
		});

		// listen to when we switch query sets 
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Widget widget = e.item;
				if (widget instanceof QuerySetTab) {
					QuerySetTab querySetTab = (QuerySetTab) widget;

					// clear the selection of services and maps for this 
					// query set 
					querySetTab.mapDiscoveryClearSelection();
					
					// refresh viewers, if necessary 
					querySetTab.refreshViewers(); 
					
					// notify that we should switch tab 'context'
					QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
							QuerySetEventType.QUERYSET_LAYERS_ACTIVATE,
							querySetTab.getMapLayers());
					
					// switch the selection provider
					intermediator.setSelectionProviderDelegate(
							querySetTab.getActiveSelectionProvider());
					
					// update tool items' status
					if (querySetTab.isShowingSensorStack()) {
						toolItemSensors.setSelection(true);
						toolItemMaps.setSelection(false);
					} else {
						toolItemSensors.setSelection(false);
						toolItemMaps.setSelection(true);
					}
					
					// update current query set
					currentQuerySet = querySetTab;
				}

			}
		});


		// Handle double click events: rename query set
		tabFolder.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.widget instanceof CTabFolder) {
					CTabItem tab = tabFolder.getSelection();
					renameQuerySet(tab);
				}
			}

		});
		
		// listener to create a new tab
		toolItemNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				/*
				 * Create tab
				 */
				QuerySetTab querySetTab =
						new QuerySetTab(getSite(), intermediator, tabFolder, SWT.NONE);
				// make the new tab the active one
				tabFolder.setSelection(querySetTab);
				// update current query set
				currentQuerySet = querySetTab;

				// notify that we should switch tab 'context'
				QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
						QuerySetEventType.QUERYSET_LAYERS_ACTIVATE,
						querySetTab.getMapLayers());

				// set the default selection provider
				intermediator.setSelectionProviderDelegate(
						querySetTab.getActiveSelectionProvider());
				
				/*
				 * Enable actions
				 */
				// the tab can be renamed 
				toolItemRename.setEnabled(true);
				// we can switch between sensors and maps stacks
				toolItemSensors.setEnabled(true);
				toolItemMaps.setEnabled(true);
				// the sensors stack is shown by default  
				toolItemSensors.setSelection(true);
				toolItemMaps.setSelection(false);
			}
		});

		toolItemRename.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem tab = tabFolder.getSelection();
				renameQuerySet(tab);
			}
		});
		
		// listener to show sensor data 
		toolItemSensors.addSelectionListener(new SelectionAdapter() {
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
		toolItemMaps.addSelectionListener(new SelectionAdapter() {
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

	}

	private void renameQuerySet(CTabItem tab) {

		IInputValidator validator = new IInputValidator() {
			@Override
			public String isValid(String newText) {
				// names cannot start with the reserved character
				if (newText.trim().startsWith(QuerySetTab.dirtyPrefix))
					return "The name cannot start with the '" +
					QuerySetTab.dirtyPrefix + "' character";
				// input OK
				return null;
			}
		};

		InputDialog dialog =
				new InputDialog(Display.getCurrent().getActiveShell(),
						"Rename...", "Name:",
						tab.getText(), validator);

		if (dialog.open() == Window.OK) {
			String name = dialog.getValue().trim();
			// set new name
			tab.setText(name);
		}
	}

	/**
	 * Listener to QuerySet events
	 */
	@Override
	public void querySetEventHandler(QuerySetEvent event) {

		Object obj = event.getEventObject();

		switch (event.getEventType()) {

		/*
		 * Update observed properties
		 */
		case QUERYSET_OFFERING_LAYER_CONTRIBUTION_CHANGED:

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

			break;
		}
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
	 * Collects the available response formats
	 *
	 * @param offeringItems
	 * @return
	 */
//	private Collection<String> collectAvailableFormats(Collection<SensorOfferingItem> offeringItems) {
//		Collection<String> formats = new HashSet<String>();
//		for (SensorOfferingItem offeringItem : offeringItems) {
//			SensorOffering sensorOffering = offeringItem.getSensorOffering();
//			// add all
//			formats.addAll(sensorOffering.getResponseFormats());
//		}
//		return formats;
//	}

	@Override
	public void setFocus() {
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
		
		QuerySetEventNotifier notifier = QuerySetEventNotifier.getInstance();
		
		// hide all contexts when we leave the discovery perspective 
		if (perspective.getId().equals(DiscoverPerspective.ID)) {
			
			// notify that we should show layers from the active 
			// query set, if it exists
			if (currentQuerySet != null) {
				notifier.fireEvent(currentQuerySet,
						QuerySetEventType.QUERYSET_LAYERS_ACTIVATE,
						currentQuerySet.getMapLayers());
			}
			
		} else {
			
			// notify that we should show no layers (empty array list)
			// NOTE: the event object (currentQuerySet) may be null
			notifier.fireEvent(currentQuerySet,
					QuerySetEventType.QUERYSET_LAYERS_ACTIVATE,
					new ArrayList<IMapLayer>());
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

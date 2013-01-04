package com.iai.proteus.views;

import gov.nasa.worldwind.geom.Sector;

import java.util.Collection;
import java.util.HashSet;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;

import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.map.MarkerSelection;
import com.iai.proteus.map.NotifyProperties;
import com.iai.proteus.map.SelectionNotifier;
import com.iai.proteus.map.SensorOfferingMarker;
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
import com.iai.proteus.ui.UIUtil;

/**
 * Discovery view
 *
 * @author Jakob Henriksson
 *
 */
public class DiscoverView extends ViewPart implements QuerySetEventListener,
	IPropertyChangeListener
{

	public static final String ID = "com.iai.proteus.views.DiscoverView";

	private CTabFolder tabFolder;

	// selection provider intermediator
	private SelectionProviderIntermediate intermediator;


	/**
	 * Constructor
	 *
	 */
	public DiscoverView() {

		// add this view as a listener
		QuerySetEventNotifier.getInstance().addListener(this);

		intermediator = new SelectionProviderIntermediate();
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		ToolItem toolItemNew = new ToolItem(toolBar, SWT.NONE);
		toolItemNew.setText("New Query Set");
		toolItemNew.setImage(UIUtil.getImage("icons/fugue/document--plus.png"));

		final ToolItem toolItemRename = new ToolItem(toolBar, SWT.NONE);
		toolItemRename.setText("Rename");
		toolItemRename.setImage(UIUtil.getImage("icons/fugue/document-rename.png"));

		// separator 
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		final ToolItem toolItemSensors = new ToolItem(toolBar, SWT.RADIO);
		toolItemSensors.setText("Sensors");
		toolItemSensors.setImage(UIUtil.getImage("icons/fugue/chart.png"));
		// selected by default 
		toolItemSensors.setSelection(true);

		final ToolItem toolItemMaps = new ToolItem(toolBar, SWT.RADIO);
		toolItemMaps.setText("Maps");
		toolItemMaps.setImage(UIUtil.getImage("icons/fugue/map.png"));

		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setSimple(false);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));


		// create default query set
		QuerySetTab querySetTab =
				new QuerySetTab(getSite(), intermediator, tabFolder, SWT.NONE);
		// make the new tab the active one
		tabFolder.setSelection(querySetTab);


		/*
		 * Add listeners
		 *
		 */
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			/*
			 * Listen to when tabs are closed and take appropriate action
			 */
			public void close(CTabFolderEvent event) {
				if (event.item instanceof QuerySetTab) {
					QuerySetTab querySetTab = (QuerySetTab) event.item;
					if (querySetTab.isDirty()) {
						UIUtil.showInfoMessage("To implement: check dirty flag");
					}

					QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
							QuerySetEventType.QUERYSET_LAYERS_DELETE,
							querySetTab.getMapIds());

					/*
					 * Disable actions if this was the last item
					 */
					if (tabFolder.getItemCount() <= 1) {
						// tabs cannot be renamed 
						toolItemRename.setEnabled(false);
						// we cannot switch between sensors and map stacks
						toolItemSensors.setSelection(false);
						toolItemSensors.setEnabled(false);
						toolItemMaps.setSelection(false);
						toolItemMaps.setEnabled(false);
					}
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

					querySetTab.refreshViewers(); 
					
					// notify that we should switch tab 'context'
					QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
							QuerySetEventType.QUERYSET_LAYERS_ACTIVATE,
							querySetTab.getMapIds());

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
					
				}

			}
		});


		/*
		 * Handle double click events: rename query set
		 */
		tabFolder.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.widget instanceof CTabFolder) {
					CTabItem tab = tabFolder.getSelection();
					renameQuerySet(tab);
				}
			}

		});

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

				// notify that we should switch tab 'context'
				QuerySetEventNotifier.getInstance().fireEvent(querySetTab,
						QuerySetEventType.QUERYSET_LAYERS_ACTIVATE,
						querySetTab.getMapIds());

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
	private Collection<String> collectAvailableFormats(Collection<SensorOfferingItem> offeringItems) {
		Collection<String> formats = new HashSet<String>();
		for (SensorOfferingItem offeringItem : offeringItems) {
			SensorOffering sensorOffering = offeringItem.getSensorOffering();
			// add all
			formats.addAll(sensorOffering.getResponseFormats());
		}
		return formats;
	}

	@Override
	public void setFocus() {
	}

}

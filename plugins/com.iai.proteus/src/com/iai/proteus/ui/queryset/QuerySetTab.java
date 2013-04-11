/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.queryset;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.wb.swt.SWTResourceManager;
import org.joda.time.Period;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.iai.proteus.Activator;
import com.iai.proteus.common.Labeling;
import com.iai.proteus.common.LatLon;
import com.iai.proteus.common.sos.SupportedResponseFormats;
import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.common.sos.exception.ExceptionReportException;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.dialogs.DownloadModel;
import com.iai.proteus.dialogs.DownloadModelHelper;
import com.iai.proteus.dialogs.GetObservationProgressDialog;
import com.iai.proteus.dialogs.ManageAllServicesDialog;
import com.iai.proteus.dialogs.ManageQuerySetServicesDialog;
import com.iai.proteus.exceptions.ResponseFormatNotSupportedException;
import com.iai.proteus.map.WorldWindUtils;
import com.iai.proteus.map.wms.WmsLayerInfo;
import com.iai.proteus.map.wms.WmsUtil;
import com.iai.proteus.model.MapId;
import com.iai.proteus.model.SensorOfferingLayer;
import com.iai.proteus.model.map.IMapLayer;
import com.iai.proteus.model.map.MapIdentifier;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.map.WmsMapLayer;
import com.iai.proteus.model.map.WmsSavedMap;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceManager;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.plot.Variables;
import com.iai.proteus.plot.VariablesHolder;
import com.iai.proteus.queryset.DataFetcher;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.queryset.Facet;
import com.iai.proteus.queryset.FacetChangeToggle;
import com.iai.proteus.queryset.FacetData;
import com.iai.proteus.queryset.FormatFacet;
import com.iai.proteus.queryset.TimeFacet;
import com.iai.proteus.ui.SwtUtil;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.ui.dnd.SavedMapsDragListener;
import com.iai.proteus.ui.dnd.SavedMapsDropListener;
import com.iai.proteus.views.DataPreviewEvent;
import com.iai.proteus.views.DataPreviewView;
import com.iai.proteus.views.TimeSeriesUtil;

/** 
 * Represents an abstract query set tab used for discovery
 *
 * (This abstract class in particular contains all the UI related artifacts.)
 *
 * @author Jakob Henriksson
 *
 */
public class QuerySetTab extends CTabItem
	implements MapIdentifier, ServiceManager
{

	private static final Logger log = Logger.getLogger(QuerySetTab.class);

	// EventAdmin service for communicating with other views/modules
	private EventAdmin eventAdminService;
	
	// The unique ID of this query set 
	private String uuid; 
	
	// True if changes have not been saved, false otherwise
	private boolean dirty;
	// True if the query set has been saved before, false otherwise 
	private boolean saved;

	// Prefix added to dirty/modified query sets
	public static String dirtyPrefix = "*";
	
	private String querySetName;

	// The basic sensor offering layer part of this context (query set) 
	private SensorOfferingLayer offeringLayer;
	
	// Holds the geographic restrictions 
	private Sector sector;
	
	// Holds all the sensor offerings
	private SensorOfferingsHolder sensorOfferingsHolder;

	// Holds all the observed properties
	private ObservedPropertiesHolder observedPropertiesHolder;
	
	// Holds observed property URIs that should be set to true in the
	// @{link ObservedPropertiesHolder} model (only used when loading
	// a query set)
	private Collection<String> activeObservedPropertyURIs;

	// Holds all the available sensor data formats
	private AvailableFormatsHolder availableFormatsHolder;

	// active facets that constrain what is being displayed
	private Set<FacetChangeToggle> activeFacets;

	// Holds query set services
	private Collection<Service> services; 

	/*
	 * Stacks and UI components 
	 */
	private Composite stack;
	private StackLayout stackLayout;

	private Composite stackServices;
	private Composite stackGeographicArea;
	private Composite stackProperties;
	private Composite stackTime;
	private Composite stackFormats;
	private Composite stackPreview;
	private Composite stackExport;


	private Composite compositePreview;
	private StackLayout stackLayoutPreview;

	private Composite compositePreviewNeeded;
	private Composite compositePreviewAvailable;

	private Cursor cursor;


	/*
	 * UI elements
	 */

	private Composite compositeOuterStack; 
	private StackLayout compositeOuterLayout; 
	
	private boolean showingSensorStack;
	
	// outer composite for the sensor workflow 
	private SashForm sashSensors;
	// outer composite for maps
	private Composite compositeMaps;
	
	private Tile tileActive;

	private IWorkbenchPartSite site;

	private Composite tileServices;
	private StyledText liveServices;

	private Composite tileGeographic;
	private StyledText liveGeographic;

	private Composite tileProperties;
	private StyledText liveProperties;

	private Composite tileTime;
	private StyledText liveTime;

	private Composite tileFormats;
	private StyledText liveFormats;

	private Composite tilePreview;
	private StyledText livePreview;

	private Composite tileExport;
	private StyledText liveExport;


	// JFace viewers
	private CheckboxTableViewer tableViewerSosServices;
	private Table tableServices;

	private CheckboxTreeViewer treeViewerObservedProperties;
	private Tree treeObservedProperties;

	private TableViewer tableViewerSensorOfferings;
	private Table tableSensorOfferings;

	private TableViewer tableViewerUnsupportedFormats;

	// selection provider intermediator
	private SelectionProviderIntermediate intermediator;
	// the active structured viewer
	private StructuredViewer activeSelectionProvider;


	private Combo comboObservedProperties;
	private Combo comboPlotTimeSeriesDomain;
	private Table tableTimeSeriesVariables;

	private Button btnClearRegion;
	
	private Button btnFetchPreview;
	private Button btnExportData;

	private ToolItem itemClearProperties;
	
	/*
	 * Maps
	 */
	
	private List<MapLayer> savedMaps;
	
	// true if no selection of a service has been made in the 
	// find/discover map view, false otherwise  
	private boolean findMapsNoServiceSelection = true;
	
	/*
	 * UI for maps
	 */
	
	private StackLayout compositeStackMapsLayout;
	
	// stack composites
	private Composite compositeSavedMaps;
	private Composite compositeAvailableMaps;

	// checkbox viewers
	private CheckboxTreeViewer treeViewerSavedMaps; 
	private TreeViewer treeViewerWmsLayers;
	// list of WMS services 
	private TableViewer tableViewerWmsServices;
	
	private FilteredTree treeFiltered;
	private Tree treeWmsLayers;
	
	private ToolItem tltmSaveSelected;
	
	// to keep track of selected items
	private int countSelectedWmsLayers = 0;


	// UI elements status
	private Map<Tile, Status> tileStatus;


	/*
	 * Images
	 */
	private Image imgDocument;
	private Image imgSectorSelection;
	private Image imgSectorClear;
	private Image imgLike; 
	private Image imgDislike;
	private Image imgChart;
	private Image imgMap;
	private Image imgDatabase;
	private Image imgDelete;
	private Image imgClear;
	private Image imgSave;
	private Image imgRefresh;
	private Image imgDotRed;
	private Image imgDotGreen;
	private Image imgAddMap;
	private Image imgBackControl;
	private Image imgQuestion;
	private Image imgAdd;
	private Image imgArrowUp;
	private Image imgArrowDown;
	
	private Font fontActive;
	private Font fontInactive; 
	
	private Color colorBg;
	private Color colorWidgetShadow;


	/*
	 * UI labels and values
	 */

	private Label lblObservedProperties;
	private String strNoObservedProperties = "Select a sensor offering";

	/*
	 * Miscellaneous
	 */
	private boolean previewTileActive = false;

	private TimeFacet activeTimeFacet;
	private FormatFacet activeFormatFacet;


	/**
	 * Constructor
	 *
	 * @param site
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("serial")
	public QuerySetTab(IWorkbenchPartSite site,
			SelectionProviderIntermediate intermediator,
			CTabFolder parent, int style)
	{
		super(parent, style);
		
		this.uuid = UUID.randomUUID().toString();

		this.site = site;
		this.intermediator = intermediator;

		tileStatus = new HashMap<Tile, Status>();

		activeFacets = new HashSet<FacetChangeToggle>();
		
		services = new ArrayList<Service>();
		savedMaps = new CopyOnWriteArrayList<MapLayer>();
		
		/*
		 * Initialize statuses
		 */
		tileStatus.put(Tile.SERVICES, Status.ACTIVE_WARNING);
		tileStatus.put(Tile.GEOGRAPHIC, Status.INACTIVE_OK);
		tileStatus.put(Tile.PROPERTIES, Status.INACTIVE_OK);
		tileStatus.put(Tile.TIME, Status.INACTIVE_WARNING);
		tileStatus.put(Tile.FORMATS, Status.INACTIVE_WARNING);
		tileStatus.put(Tile.PREVIEW, Status.INACTIVE_WARNING);
		tileStatus.put(Tile.EXPORT, Status.INACTIVE_WARNING);

		sensorOfferingsHolder = new SensorOfferingsHolder();
		observedPropertiesHolder = new ObservedPropertiesHolder();
		availableFormatsHolder = new AvailableFormatsHolder();
		
		activeObservedPropertyURIs = new ArrayList<String>();

		/*
		 * Resources
		 */
		imgDocument = UIUtil.getImage("icons/fugue/document.png");
		imgSectorSelection = UIUtil.getImage("icons/fugue/zone--plus.png");
		imgSectorClear = UIUtil.getImage("icons/fugue/zone--minus.png");
		imgLike = UIUtil.getImage("icons/fugue/star.png");
		imgDislike = UIUtil.getImage("icons/fugue/star-empty.png");
		imgChart = UIUtil.getImage("icons/fugue/chart.png");
		imgMap = UIUtil.getImage("icons/fugue/map.png");
		imgDatabase = UIUtil.getImage("icons/fugue/database.png");
		imgDelete = UIUtil.getImage("icons/fugue/minus-button.png");
		imgClear = UIUtil.getImage("icons/fugue/cross-button.png");
		imgSave = UIUtil.getImage("icons/fugue/disk-black.png");
		imgRefresh = UIUtil.getImage("icons/fugue/arrow-circle-double-135.png");
		imgDotRed = UIUtil.getImage("icons/dot-red.png");
		imgDotGreen = UIUtil.getImage("icons/dot-green.png");
		imgAddMap = UIUtil.getImage("icons/fugue/map--plus.png");
		imgBackControl = UIUtil.getImage("icons/fugue/navigation-180-button.png");
		imgQuestion = UIUtil.getImage("icons/fugue/question-white.png");
		imgAdd = UIUtil.getImage("icons/fugue/plus-button.png");
		imgArrowUp = UIUtil.getImage("icons/fugue/arrow-090.png");
		imgArrowDown = UIUtil.getImage("icons/fugue/arrow-270.png");
		
		fontActive = SWTResourceManager.getFont("Lucida Grande", 10, SWT.BOLD | SWT.ITALIC);
		fontInactive = SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL);
		
		colorBg = new Color(Display.getCurrent(), 64, 133, 176);
		colorWidgetShadow = SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		
		cursor = getDisplay().getSystemCursor(SWT.CURSOR_HAND);

		/*
		 * Defaults
		 */

		this.tileActive = Tile.SERVICES;
		this.activeTimeFacet = TimeFacet.ALL;
		this.activeFormatFacet = FormatFacet.ALL;

		// unsaved
		dirty = true;
		saved = false;

		this.querySetName = "Untitled";
		
		this.setText(querySetName);
		setImage(imgDocument);

		offeringLayer = new SensorOfferingLayer();
		
		// Create tab interface
		createTab(parent);

		// highlight the Services section by default
		updateTiles();

		// bring the property sheet to the front by default
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().
				getActivePage().showView(IPageLayout.ID_PROP_SHEET);
		} catch (PartInitException exception) {
			log.error("Part init exception: " + exception.getMessage());
		}

		// activate the default tile, and set the default selection provider
		// in the process
		activateTile(Tile.SERVICES);

		// get EventAdmin service 
		BundleContext ctx = Activator.getContext();
		ServiceReference<EventAdmin> ref = 
				ctx.getServiceReference(EventAdmin.class);
		eventAdminService = ctx.getService(ref);
		
		// send event to initialize the layer 
		eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_INIT.toString(), 
				new HashMap<String, Object>() { 
				{
					put("object", this);
					put("value", offeringLayer);
				}
		}));
		
		// create handler 
		EventHandler handler = new EventHandler() {
			@Override
			public void handleEvent(final Event event) {
				
//				Object obj = event.getProperty("object");
				Object value = event.getProperty("value");
				
				// clear facets 
				if (match(event, EventTopic.QS_FACET_CLEARED)) {
					
					if (value instanceof Facet) {
						Facet facet = (Facet) value;
						if (facet.equals(Facet.OBSERVED_PROPERTY)) {
							// remove all remembered observed properties facets
							activeFacets.clear();
						}
					}
					
					// TODO: refresh observed property viewer? 
				}
			}
		};
		
		// register service 
		Dictionary<String,String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, 
				EventTopic.TOPIC_QUERYSET.toString());
		// listen to query set topics 
		ctx.registerService(EventHandler.class.getName(), handler, properties);
	}

	/**
	 * Returns true if the event matches the event topic, false otherwise 
	 * 
	 * @param event
	 * @param topic
	 * @return
	 */
	private boolean match(Event event, EventTopic topic) {
		return event.getTopic().equals(topic.toString());
	}

	/**
	 * Returns the unique ID 
	 * 
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the unique ID 
	 * 
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Create a new tab
	 *
	 * @param parent
	 */
	private void createTab(CTabFolder tabFolder) {

		setShowClose(true);
		
		/*
		 * Main stack (for sensors and maps) 
		 */
		
		compositeOuterStack = new Composite(tabFolder, SWT.NONE);
		compositeOuterStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compositeOuterLayout = new StackLayout();
		compositeOuterStack.setLayout(compositeOuterLayout);
		
		this.setControl(compositeOuterStack);
		
		/*
		 * Sensors  
		 */

		sashSensors = new SashForm(compositeOuterStack, SWT.SMOOTH);
		sashSensors.setSashWidth(10);
		GridData gd_sash = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		gd_sash.widthHint = 338;
		sashSensors.setLayoutData(gd_sash);

		final ScrolledComposite scrolledTiles = new ScrolledComposite(sashSensors, SWT.V_SCROLL);
		scrolledTiles.setExpandVertical(true);
		scrolledTiles.setExpandHorizontal(true);

		final Composite tiles = new Composite(scrolledTiles, SWT.NONE);
		tiles.setLayout(new GridLayout(1, false));

	
		/*
		 * SERVICES
		 */

		tileServices = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tileServices = new GridLayout(1, false);
		gl_tileServices.marginWidth = 0;
		gl_tileServices.marginHeight = 0;
		tileServices.setLayout(gl_tileServices);
		tileServices.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tileServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		tileServices.setCursor(cursor);

		createLiveTop(Tile.SERVICES, tileServices, "Services");
		liveServices = createLiveTile(tileServices);
		// set and update the live tile text
		liveServices.setText("");
		updateLiveTileServices(0);

		/*
		 * GEOGRAPHIC RESTRICTION
		 */

		tileGeographic = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tileGeographic = new GridLayout(1, false);
		gl_tileGeographic.marginWidth = 0;
		gl_tileGeographic.marginHeight = 0;
		tileGeographic.setLayout(gl_tileGeographic);
		tileGeographic.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tileGeographic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tileGeographic.setCursor(cursor);

		createLiveTop(Tile.GEOGRAPHIC, tileGeographic, "Geographic area");
		liveGeographic = createLiveTile(tileGeographic);
		// set and update the live tile text
		liveGeographic.setText("");
		updateLiveTileGeographic(null, 0);

		/*
		 * TIME
		 */

		tileTime = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tileTime = new GridLayout(1, false);
		gl_tileTime.marginWidth = 0;
		gl_tileTime.marginHeight = 0;
		tileTime.setLayout(gl_tileTime);
		tileTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tileTime.setCursor(cursor);

		createLiveTop(Tile.TIME, tileTime, "Time");
		liveTime = createLiveTile(tileTime);
		// set and update the live tile text
		liveTime.setText("");
		updateLiveTileTime(TimeFacet.ALL, 0);

		/*
		 * OBSERVED PROPERTIES
		 */

		tileProperties = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tileProperties = new GridLayout(1, false);
		gl_tileProperties.marginWidth = 0;
		gl_tileProperties.marginHeight = 0;
		tileProperties.setLayout(gl_tileProperties);
		tileProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tileProperties.setCursor(cursor);

		createLiveTop(Tile.PROPERTIES, tileProperties, "Observed properties");
		liveProperties = createLiveTile(tileProperties);
		// set and update the live tile text
		liveProperties.setText("");
		updateLiveTileObservedProperties(0, 0);

		/*
		 * FORMATS
		 */

		tileFormats = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tileFormats = new GridLayout(1, false);
		gl_tileFormats.marginWidth = 0;
		gl_tileFormats.marginHeight = 0;
		tileFormats.setLayout(gl_tileFormats);
		tileFormats.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tileFormats.setCursor(cursor);

		createLiveTop(Tile.FORMATS, tileFormats, "Data formats");
		liveFormats = createLiveTile(tileFormats);
		// set and update the live tile text
		liveFormats.setText("");
		updateLiveTileFormats(getActiveFormatRestriction(), 0);
		

		/*
		 * PREVIEW
		 */

		tilePreview = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tilePreview = new GridLayout(1, false);
		gl_tilePreview.marginWidth = 0;
		gl_tilePreview.marginHeight = 0;
		tilePreview.setLayout(gl_tilePreview);
		tilePreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tilePreview.setCursor(cursor);

		createLiveTop(Tile.PREVIEW, tilePreview, "Preview");
		livePreview = createLiveTile(tilePreview);
		// set and update the live tile text
		livePreview.setText("");
		updateLiveTilePreview(0, 0);

		/*
		 * EXPORT
		 */

		tileExport = new Composite(tiles, SWT.BORDER);
		GridLayout gl_tileExport = new GridLayout(1, false);
		gl_tileExport.marginWidth = 0;
		gl_tileExport.marginHeight = 0;
		tileExport.setLayout(gl_tileExport);
		tileExport.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tileExport.setCursor(cursor);

		createLiveTop(Tile.EXPORT, tileExport, "Export sensor data");
		liveExport = createLiveTile(tileExport);
		// set and update the live tile text
		liveExport.setText("");
		updateLiveTileExport(0, 0);


		scrolledTiles.setContent(tiles);
		scrolledTiles.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolledTiles.getClientArea();
				scrolledTiles.setMinSize(tiles.computeSize(r.width, SWT.DEFAULT));
			}
		});

		/*
		 * STACKS
		 */

		stack = new Composite(sashSensors, SWT.NONE);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);

		/*
		 * STACK: services
		 */

		stackServices = new Composite(stack, SWT.NONE);
		stackServices.setLayout(new GridLayout(1, false));

		// default
		stackLayout.topControl = stackServices;

		Label lblServices = new Label(stackServices, SWT.NONE);
		lblServices.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblServices.setText("Services");

		// composite to hold two different tool bars 
		final Composite compositeToolbar = new Composite(stackServices, SWT.NONE);
		compositeToolbar.setLayout(new GridLayout(2, false));
		compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final ToolBar toolBarServices = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBarServices.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		ToolItem itemServicesManage = new ToolItem(toolBarServices, SWT.NONE);
		itemServicesManage.setText("Add...");
		itemServicesManage.setImage(imgAdd);
		// manage services listener
		itemServicesManage.addSelectionListener(new SelectionAdapter() {
			
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				// create and open dialog to manage services 
				ManageQuerySetServicesDialog dialog = 
						new ManageQuerySetServicesDialog(site.getShell(), 
								QuerySetTab.this, ServiceType.SOS);
				int res = dialog.open();
				if (res == IDialogConstants.OK_ID) {
					
					// toggle layers - send event
					eventAdminService.sendEvent(new Event(EventTopic.QS_TOGGLE_SERVICES.toString(), 
							new HashMap<String, Object>() { 
						{
							put("object", offeringLayer);
							put("value", getServices());
						}
					}));
					
					// count active services 
					int countActiveServices = 0;
					for (Service service : getServices()) 
						countActiveServices += service.isActive() ? 1 : 0;
					// update live services tile
					updateLiveTileServices(countActiveServices);				

					// refresh viewer as input might have changed 
					tableViewerSosServices.refresh();
					
					// mark as dirty
					setDirty(true);

					// TODO: need to update layers as layers might have been 
					//       deleted
				}
				dialog.close();
			}
		});
		
		final ToolItem itemServiceRemove = new ToolItem(toolBarServices, SWT.NONE);
		itemServiceRemove.setText("Remove");
		itemServiceRemove.setImage(imgDelete);
		// default
		itemServiceRemove.setEnabled(false);
		// manage services listener
		itemServiceRemove.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				Collection<Service> servicesToRemove = new ArrayList<Service>();
				// get services to be deleted
				Collection<?> selected = SwtUtil.getSelection(tableViewerSosServices);
				for (Object o : selected) {
					// just make sure we are dealing with the right type
					if (o instanceof Service) {
						Service service = (Service) o;
						// important: update the model status, 
						// deactivate the service
						service.deactivate();
						// add the service to be removed 
						servicesToRemove.add(service); 
					}
				}
				
				// send event
				eventAdminService.sendEvent(new Event(EventTopic.QS_TOGGLE_SERVICES.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", offeringLayer);
						put("value", getServices());
					}
				}));
				
				// remove the services from the model
				getServices().removeAll(servicesToRemove);
				// count active services 
				int countActiveServices = 0;
				for (Service service : getServices()) 
					countActiveServices += service.isActive() ? 1 : 0;
				// update live services tile
				updateLiveTileServices(countActiveServices);				
				
				// refresh viewer as input might have changed 
				tableViewerSosServices.refresh();
			}
		});		
		
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
				SwtUtil.createHelpController(stackServices, 
						tltmHelp, compositeToolbar, 
						"These services are specific to this query set. " + 
						"Services can be added or removed as appropriate.");
			}
		});

		// services 

		tableViewerSosServices = createServiceTableViewer(stackServices);
		
		tableServices = tableViewerSosServices.getTable();
		tableServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableServices.setHeaderVisible(true);
		tableServices.setLinesVisible(false);
		
		// listener to update tool bar items 
		tableViewerSosServices.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				itemServiceRemove.setEnabled(!event.getSelection().isEmpty());
			}
		});

		// input has to be set after the @{link TableViewerColumns} are defined		
		tableViewerSosServices.setContentProvider(new ServiceContentProvider(ServiceType.SOS));
		tableViewerSosServices.setUseHashlookup(true);
		tableViewerSosServices.setInput(services);
		
		/*
		 * STACK: geographic
		 */

		stackGeographicArea = new Composite(stack, SWT.NONE);
		stackGeographicArea.setLayout(new GridLayout(1, false));

		Label lblGeographic = new Label(stackGeographicArea, SWT.NONE);
		lblGeographic.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblGeographic.setText("Geographic area");

		Label lblGeographicExplanation = new Label(stackGeographicArea, SWT.WRAP);
		lblGeographicExplanation.setText(
				"Use this tool to select an area on the map to restrict " +
				"the sensor offerings included in this query set.");
		lblGeographicExplanation.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		final String strSectorOn = "Select region";
		final String strSectorOff = "Clear region";

		Button btnRegion = new Button(stackGeographicArea, SWT.PUSH);
		btnRegion.setText(strSectorOn);
		btnRegion.setImage(imgSectorSelection);
		btnRegion.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		btnClearRegion = new Button(stackGeographicArea, SWT.PUSH);
		btnClearRegion.setText(strSectorOff);
		btnClearRegion.setImage(imgSectorClear);
		btnClearRegion.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		// default
		btnClearRegion.setEnabled(false);

		btnRegion.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				// send notification that sector is enabled
				eventAdminService.sendEvent(new Event(EventTopic.QS_REGION_ENABLED.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", getMapId());
					}
				}));
				// enable 'clear region' button
				btnClearRegion.setEnabled(true);
				// mark as dirty
				setDirty(true);
			}
		});

		btnClearRegion.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				// send notification that sector should be cleared
				eventAdminService.sendEvent(new Event(EventTopic.QS_REGION_DISABLED.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", getMapId());
					}
				}));				
				// disable 'clear region' button
				btnClearRegion.setEnabled(false);
				// mark as dirty
				setDirty(true);
			}
		});

		/*
		 * STACK: properties
		 */

		stackProperties = new Composite(stack, SWT.NONE);
		stackProperties.setLayout(new GridLayout(1, false));

		Label lblProperties = new Label(stackProperties, SWT.NONE);
		lblProperties.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblProperties.setText("Observed properties");

		final ToolBar toolBarProperties = new ToolBar(stackProperties, SWT.FLAT | SWT.RIGHT);

		itemClearProperties = new ToolItem(toolBarProperties, SWT.NONE);
		itemClearProperties.setText("Clear all facets");
		itemClearProperties.setImage(imgClear);
		// default
		itemClearProperties.setEnabled(false);
		// listener
		itemClearProperties.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				// clear OBSERVED PROPERTIES facets
				eventAdminService.sendEvent(new Event(EventTopic.QS_FACET_CLEARED.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", getMapId());
						put("value", Facet.OBSERVED_PROPERTY);
					}
				}));				
			}
		});

		treeViewerObservedProperties =
				new CheckboxTreeViewer(stackProperties,	SWT.BORDER);
		treeObservedProperties = treeViewerObservedProperties.getTree();
		treeObservedProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// listener to properly handle tree check box selections
		treeObservedProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					TreeItem item = (TreeItem) e.item;
					boolean checked = item.getChecked();
					UIUtil.checkItems(item, checked);
					UIUtil.checkPath(item.getParentItem(), checked, false);
				}
			}
		});

		// listener to update facets
		treeObservedProperties.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					TreeItem item = (TreeItem) e.item;
					
					// collect all the facet changes 
					final Collection<FacetChangeToggle> changes = 
							collectFacets(item);
					
					// update state of facet changes
					updateFacetState(changes);

					// fire event
					eventAdminService.sendEvent(new Event(EventTopic.QS_FACET_CHANGED.toString(), 
							new HashMap<String, Object>() { 
						{
							put("object", getMapId());
							put("value", changes);
						}
					}));
					
					// mark as dirty
					setDirty(true);
				}
			}
		});

		ContentProvider contentProvider = new ContentProvider();
		LabelProvider labelProvider = new LabelProvider();

		treeViewerObservedProperties.setContentProvider(contentProvider);
		treeViewerObservedProperties.setLabelProvider(labelProvider);
		treeViewerObservedProperties.setInput(observedPropertiesHolder);
		treeViewerObservedProperties.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object elmt = event.getElement();
				if (elmt instanceof ObservedProperty) {
					ObservedProperty op = (ObservedProperty) elmt;
					// update model element 
					op.setChecked(event.getChecked());
				}
			}
		});
		treeViewerObservedProperties.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				if (element instanceof Category) {
					Category category = (Category) element;
					// a category is grayed if some and only some children
					// are checked
					boolean some = false;
					boolean all = true;
					for (ObservedProperty property :
						category.getObservedProperties()) {
						if (isActiveFacet(property))
							some = true;
						else
							all = false;
					}
					return some && !all;
				}
				// default
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				if (element instanceof Category) {
					Category category = (Category) element;
					// a category is considered checked if some of its children
					// are checked (isGrayed() also has to be considered)
					boolean checked = false;
					for (ObservedProperty property :
						category.getObservedProperties()) {
						if (isActiveFacet(property)) {
							checked = true;
							break;
						}
					}
					return checked;
				} else if (element instanceof ObservedProperty) {
					// an observed property is checked if it is an active facet
					ObservedProperty property = (ObservedProperty) element;
					if (isActiveFacet(property)) {
						return true;
					}
				}
				return false;
			}
		});

		/*
		 * STACK: time
		 */

		stackTime = new Composite(stack, SWT.NONE);
		stackTime.setLayout(new GridLayout(1, false));

		Label lblTime = new Label(stackTime, SWT.NONE);
		lblTime.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblTime.setText("Time");

		Label lblTimeExplanation = new Label(stackTime, SWT.WRAP);
		lblTimeExplanation.setText(
				"Only include offerings that provide sensor data during a certain period");
		lblTimeExplanation.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		Composite compositeTimeButtons = new Composite(stackTime, SWT.NONE);
		compositeTimeButtons.setLayout(new GridLayout(1, true));
		compositeTimeButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final Button btnTimeNoRestriction = new Button(compositeTimeButtons, SWT.TOGGLE);
		btnTimeNoRestriction.setText(TimeFacet.ALL.toString());
		btnTimeNoRestriction.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		btnTimeNoRestriction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTimeButtonSelection(e.widget);
				updateTimeRestriction(TimeFacet.ALL);
			}
		});

		// selected by default
		btnTimeNoRestriction.setSelection(true);

		final Button btnTime24h = new Button(compositeTimeButtons, SWT.TOGGLE);
		btnTime24h.setText(TimeFacet.ONEDAY.toString());
		btnTime24h.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		btnTime24h.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTimeButtonSelection(e.widget);
				updateTimeRestriction(TimeFacet.ONEDAY);
			}
		});

		final Button btnTimeOneWeek = new Button(compositeTimeButtons, SWT.TOGGLE);
		btnTimeOneWeek.setText(TimeFacet.ONEWEEK.toString());
		btnTimeOneWeek.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		btnTimeOneWeek.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTimeButtonSelection(e.widget);
				updateTimeRestriction(TimeFacet.ONEWEEK);
			}
		});

		final Button btnTimeCustom = new Button(compositeTimeButtons, SWT.TOGGLE);
		btnTimeCustom.setText("Custom...");
		btnTimeCustom.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		btnTimeCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTimeButtonSelection(e.widget);

				UIUtil.showInfoMessage("To implement...");
			}
		});

		btnTimeCustom.setEnabled(false);

//		DateTime calendarStart = new DateTime (compositeTimeButtons, SWT.CALENDAR | SWT.BORDER);
//		calendarStart.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
//		calendarStart.setEnabled(false);
//
//		DateTime calendarEnd = new DateTime (compositeTimeButtons, SWT.CALENDAR);
//		calendarEnd.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
//		calendarEnd.setEnabled(true);


		/*
		 * STACK: formats
		 */

		stackFormats = new Composite(stack, SWT.NONE);
		stackFormats.setLayout(new GridLayout(1, false));
		stackFormats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label lblFormats = new Label(stackFormats, SWT.NONE);
		lblFormats.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblFormats.setText("Data formats");

		final Button btnFormatsAll = new Button(stackFormats, SWT.TOGGLE);
		btnFormatsAll.setText(FormatFacet.ALL.toString());
		btnFormatsAll.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		// default
		btnFormatsAll.setSelection(true);

		final Button btnFormatsSupported = new Button(stackFormats, SWT.TOGGLE);
		btnFormatsSupported.setText(FormatFacet.SUPPORTED.toString());
		btnFormatsSupported.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		new Label(stackFormats, SWT.NONE).setText("Supported formats");

		TableViewer tableViewerSupportedFormats =
				CheckboxTableViewer.newCheckList(stackFormats, SWT.BORDER);

		tableViewerSupportedFormats.setContentProvider(contentProvider);
		tableViewerSupportedFormats.setInput(SupportedResponseFormats.values());

		final Table tableSupportedFormats = tableViewerSupportedFormats.getTable();
		tableSupportedFormats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableSupportedFormats.setEnabled(false);

		btnFormatsAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFormatButtonSelection(e.widget);
				// un-check all
				for (TableItem item : tableSupportedFormats.getItems()) {
					item.setChecked(false);
				}
				updateFormatRestriction(FormatFacet.ALL);
			}
		});

		btnFormatsSupported.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFormatButtonSelection(e.widget);
				// check all
				for (TableItem item : tableSupportedFormats.getItems()) {
					item.setChecked(true);
				}
				updateFormatRestriction(FormatFacet.SUPPORTED);
			}
		});

		new Label(stackFormats, SWT.NONE).setText("Available formats");

		tableViewerUnsupportedFormats =
				CheckboxTableViewer.newCheckList(stackFormats, SWT.BORDER);

		tableViewerUnsupportedFormats.setContentProvider(contentProvider);
		tableViewerUnsupportedFormats.setInput(availableFormatsHolder);

		Table tableUnsupportedFormats = tableViewerUnsupportedFormats.getTable();
		tableUnsupportedFormats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableUnsupportedFormats.setEnabled(false);


		/*
		 * STACK: preview
		 */

		stackPreview = new Composite(stack, SWT.NONE);
		stackPreview.setLayout(new GridLayout(1, false));
		stackPreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lblPreview = new Label(stackPreview, SWT.NONE);
		lblPreview.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblPreview.setText("Preview");

		new Label(stackPreview, SWT.NONE).setText("Sensor offering IDs");

		tableViewerSensorOfferings = new TableViewer(stackPreview);

		tableSensorOfferings = tableViewerSensorOfferings.getTable();

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1, false));
		tableSensorOfferings.setLayout(tableLayout);
		tableSensorOfferings.setHeaderVisible(false);

		TableColumn col1 = new TableColumn(tableSensorOfferings, SWT.FILL);
		col1.setText("Sensor offering ID");
		tableSensorOfferings.showColumn(col1);

		GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
		gd.heightHint = 200;
		tableSensorOfferings.setLayoutData(gd);

		tableViewerSensorOfferings.setContentProvider(contentProvider);
		tableViewerSensorOfferings.setLabelProvider(labelProvider);
		tableViewerSensorOfferings.setUseHashlookup(false);
		tableViewerSensorOfferings.setInput(sensorOfferingsHolder);
		
		tableViewerSensorOfferings.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					StructuredSelection selection =
							(StructuredSelection) event.getSelection();

					if (selection.size() == 1) {
						Object first = selection.getFirstElement();
						if (first instanceof SensorOfferingItem) {
							SensorOfferingItem item = (SensorOfferingItem) first;

							/*
							 * Update the options for selecting observed properties
							 */
							updateObservedPropertiesSelection(item);

						}
					} else {

						/*
						 * Clear observed properties
						 */
						updateObservedPropertiesSelection(null);

						// TODO: send event to map to clear highlighting
					}
				}
			}
		});

		// listening to double clicks on sensor offering list
		tableViewerSensorOfferings.addDoubleClickListener(new IDoubleClickListener() {
			@SuppressWarnings("serial")
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object elmt =
							((StructuredSelection) selection).getFirstElement();
					if (elmt instanceof SensorOfferingItem) {
						SensorOffering offering =
								((SensorOfferingItem) elmt).getSensorOffering();

						final Position pos =
								WorldWindUtils.getCentralPosition(offering);

						// fire event to fly to location 
						eventAdminService.sendEvent(new Event(EventTopic.QS_FLY_TO_LATLON.toString(), 
								new HashMap<String, Object>() { 
							{
								put("object", this);
								put("value", new LatLon(pos.getLatitude().degrees,
										pos.getLongitude().degrees));
							}
						}));
						
					}
				}
			}
		});

		lblObservedProperties = new Label(stackPreview, SWT.NONE);
		lblObservedProperties.setText(strNoObservedProperties);

		comboObservedProperties = new Combo(stackPreview, SWT.READ_ONLY);
		comboObservedProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		/*
		 * Listener for changes in observed property selection
		 */
		comboObservedProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				prepareForPreview();
			}
		});

		Label lbl1 = new Label(stackPreview, SWT.SEPARATOR | SWT.HORIZONTAL);
		lbl1.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		/*
		 * Preview stack
		 */
		compositePreview = new Composite(stackPreview, SWT.NONE);
		stackLayoutPreview = new StackLayout();
		compositePreview.setLayout(stackLayoutPreview);
		compositePreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		

		/*
		 * Preview stack - Top
		 */
		compositePreviewNeeded = new Composite(compositePreview, SWT.NONE);
		compositePreviewNeeded.setLayout(new GridLayout());
		compositePreviewNeeded.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite compositeFetchData = new Composite(compositePreviewNeeded, SWT.NONE);
		compositeFetchData.setLayout(new GridLayout(1, true));
		compositeFetchData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		btnFetchPreview = new Button(compositeFetchData, SWT.PUSH);
		btnFetchPreview.setText("Preview sensor data");
		btnFetchPreview.setImage(imgChart);
		btnFetchPreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		btnFetchPreview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				SensorOfferingItem offeringItem = getSensorOfferingSelection();
				if (offeringItem != null) {

					/*
					 * Initiate data preview
					 */

					try {

						// determine the period to use for previewing data
						Period period = getActivePreviewFetchPeriod();

						new ProgressMonitorDialog(site.getShell()).run(true,
								true, new DataFetcher(offeringItem, period));

						// update variables
						updateAvailableVariables(offeringItem);

						// preview sensor data
						previewSensorData();

					} catch (InvocationTargetException e) {
						Throwable t = e.getCause();
						if (t != null) {
							if (t instanceof SocketTimeoutException) {
								String msg = "The connection timed out. " +
										"Please try again later or change the " +
										"timeout preferences.";
								UIUtil.showErrorMessage(msg);
							} else {
								String msg = t.getMessage();
								if (msg != null)
									UIUtil.showErrorMessage(msg);
								else
									UIUtil.showErrorMessage("Unknown error occured");
							}
						}
					} catch (InterruptedException e) {
						UIUtil.showErrorMessage("The operation was interrupted: " +
								e.getMessage());
					}
				}
			}
		});

		/*
		 * Preview stack - Bottom
		 */
		compositePreviewAvailable = new Composite(compositePreview, SWT.NONE);
		compositePreviewAvailable.setLayout(new GridLayout());
		compositePreviewAvailable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		new Label(compositePreviewAvailable, SWT.NONE).setText("Plot type");

		Composite compositePlotTypes = new Composite(compositePreviewAvailable, SWT.NONE);
		compositePlotTypes.setLayout(new GridLayout(2, true));
		compositePlotTypes.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false, 1, 1));

		final Button btnTimeSeries = new Button(compositePlotTypes, SWT.TOGGLE | SWT.WRAP);
		btnTimeSeries.setSelection(true);
		btnTimeSeries.setText("Time\nSeries");
		btnTimeSeries.setImage(imgChart);
		GridData gridData = new GridData(SWT.NONE, SWT.FILL, false, true);
		gridData.heightHint = 38;
		btnTimeSeries.setLayoutData(gridData);


		final Button btnContour = new Button(compositePlotTypes, SWT.TOGGLE | SWT.WRAP);
		btnContour.setSelection(false);
		btnContour.setText("Contour\nplot");
		btnContour.setImage(UIUtil.getImage("icons/fugue/spectrum.png"));
		gridData = new GridData(SWT.NONE, SWT.FILL, false, true);
		gridData.heightHint = 38;
		// TODO: should be included when supported 
		gridData.exclude = true;
		btnContour.setLayoutData(gridData);
		

		lbl1 = new Label(compositePreviewAvailable, SWT.SEPARATOR | SWT.HORIZONTAL);
		lbl1.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		final Composite plotOptions = new Composite(compositePreviewAvailable, SWT.NONE);
		plotOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		final StackLayout plotOptionsStackLayout = new StackLayout();
		plotOptions.setLayout(plotOptionsStackLayout);

		/*
		 * Time series plot options 
		 */
		final Composite plotTimeSeriesOptions = new Composite(plotOptions, SWT.NONE);
		plotTimeSeriesOptions.setLayout(new GridLayout(2, false));

		Label lblDomain = new Label(plotTimeSeriesOptions, SWT.NONE);
		lblDomain.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDomain.setText("Domain");

		comboPlotTimeSeriesDomain = new Combo(plotTimeSeriesOptions, SWT.READ_ONLY);
		comboPlotTimeSeriesDomain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblRange = new Label(plotTimeSeriesOptions, SWT.NONE);
		lblRange.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblRange.setText("Range");

		tableTimeSeriesVariables = new Table(plotTimeSeriesOptions,
				SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tableTimeSeriesVariables.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		/*
		 * Contour plot options 
		 */
		final Composite plotContourOptions = new Composite(plotOptions, SWT.NONE);
		plotContourOptions.setLayout(new GridLayout(2, false));
		
		new Label(plotContourOptions, SWT.NONE).setText("Contour");
		
		
		// default plot options 
		plotOptionsStackLayout.topControl = plotTimeSeriesOptions;
		
		// plot type selection listeners 
		
		btnTimeSeries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// update status of buttons
				btnTimeSeries.setSelection(true);
				btnContour.setSelection(false);
				// change stack and force re-layout 
				plotOptionsStackLayout.topControl = plotTimeSeriesOptions;
				plotOptions.layout();
			}
		});

		btnContour.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// update status of buttons
				btnTimeSeries.setSelection(false);
				btnContour.setSelection(true);
				// change stack and force re-layout 
				plotOptionsStackLayout.topControl = plotContourOptions;
				plotOptions.layout();
			}
		});


		Button btnPreview = new Button(compositePreviewAvailable, SWT.CENTER);
		btnPreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnPreview.setText("Update preview");
		btnPreview.setImage(UIUtil.getImage("icons/fugue/chart.png"));

		btnPreview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				previewSensorData();
			}
		});

		stackLayoutPreview.topControl = compositePreviewNeeded;

		/*
		 * STACK: summary
		 */

		stackExport = new Composite(stack, SWT.NONE);
		stackExport.setLayout(new GridLayout(1, false));

		Label lblSummary = new Label(stackExport, SWT.NONE);
		lblSummary.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblSummary.setText("Export sensor data");
		
		Composite composite = new Composite(stackExport, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label lblExport = new Label(composite, SWT.WRAP);
		lblExport.setText("You can download sensor data in bulk by clicking the button below");
		lblExport.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		btnExportData = new Button(stackExport, SWT.TOGGLE);
		btnExportData.setText("Export data");
		btnExportData.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		// default 
		btnExportData.setEnabled(false);

		btnExportData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportData();
			}
		});

		// default stack 
		compositeOuterLayout.topControl = sashSensors;
		compositeOuterStack.layout();
		showingSensorStack = true;
		
		/*
		 * Maps
		 */
		
		compositeMaps = new Composite(compositeOuterStack, SWT.NONE);
		compositeMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				
		createMapsContents(compositeMaps);
		
		/* 
		 * Dispose listener
		 */
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				
				QuerySetTab tab = QuerySetTab.this;
				
				// dispose of resources 
				disposeResources();
				
				// NOTE: the count of tabs does not include the one that 
				// is being disposed
				if (tab.getParent().getItemCount() < 1) {

					// get source provider service 
					ISourceProviderService sourceProviderService =
							(ISourceProviderService) PlatformUI.getWorkbench().
							getActiveWorkbenchWindow().
							getService(ISourceProviderService.class);

					// get our service
					QuerySetOpenState stateService = 
							(QuerySetOpenState) sourceProviderService
							.getSourceProvider(QuerySetOpenState.STATE);
					
					// activate change 
					stateService.setNoQuerySetOpen();
				}
			}
		});		

	}
	
	/**
	 * Creates the UI for the map section of a Query Set tab  
	 * 
	 * @param parent
	 */
	private void createMapsContents(final Composite parent) {

		compositeStackMapsLayout = new StackLayout();
		parent.setLayout(compositeStackMapsLayout);
		
		/*
		 * Saved maps
		 */
		
		compositeSavedMaps = new Composite(parent, SWT.NONE);
		compositeSavedMaps.setLayout(new GridLayout(1, false));
		compositeSavedMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// this is the default stack on top 
		compositeStackMapsLayout.topControl = compositeSavedMaps;


		// button for switching the stack to find more maps 
		Button btnSwitchStackMoreMaps = new Button(compositeSavedMaps, SWT.NONE);
		btnSwitchStackMoreMaps.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		btnSwitchStackMoreMaps.setText("Find maps");
		btnSwitchStackMoreMaps.setImage(imgAddMap);
		btnSwitchStackMoreMaps.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// reset variable to indicate that no WMS service selection
				// has been made
				findMapsNoServiceSelection = true;
				
				// put the right stack on top 
				compositeStackMapsLayout.topControl = compositeAvailableMaps;
				parent.layout();
				
				// refresh the services in case there was a change 
				tableViewerWmsServices.refresh();
			}
		});	
		
		final Group groupMaps = new Group(compositeSavedMaps, SWT.BORDER);
		groupMaps.setLayout(new GridLayout(1, false));
		groupMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		groupMaps.setText("Saved maps");
		
		final Composite compositeSavedMapsToolbar = new Composite(groupMaps, SWT.NONE);
		compositeSavedMapsToolbar.setLayout(new GridLayout(2, false));
		compositeSavedMapsToolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));		
		
		ToolBar toolBarSavedMaps = new ToolBar(compositeSavedMapsToolbar, SWT.FLAT | SWT.RIGHT);
		toolBarSavedMaps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final ToolItem tltmSavedMapDelete = new ToolItem(toolBarSavedMaps, SWT.NONE);
		tltmSavedMapDelete.setText("Delete map");
		tltmSavedMapDelete.setImage(imgDelete);
		// disabled by default
		tltmSavedMapDelete.setEnabled(false);
		// listener to remove maps saved in the Query Set 
		tltmSavedMapDelete.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				Collection<?> selection = SwtUtil.getSelection(treeViewerSavedMaps);
				int count = selection.size();
				
				MessageDialog dialog = 
						UIUtil.getConfirmDialog(site.getShell(), 
								"Delete saved maps", 
								"Are you sure you want to delete the" +
								(count > 1 ? count : "") + 
								" selected map" + (count > 1 ? "s" : "") + 
								" from this query set?");
				int result = dialog.open();
				// return and do nothing if the user cancels the action
				if (result != MessageDialog.OK) {
					return;
				}
				
				final Collection<IMapLayer> mapsToDelete = new ArrayList<IMapLayer>();
				
				for (Object obj : selection) {
					// make sure we are dealing with the right model object type
					if (obj instanceof WmsSavedMap) {
						WmsSavedMap map = (WmsSavedMap) obj;
						// remember the map IDs to be deleted from the map viewer
						mapsToDelete.add(map);
						// remove the selected map from the viewer input collection 
						getSavedMaps().remove(map);
					}
				}

				// update the viewers whose input may have changed
				treeViewerSavedMaps.refresh();
				
				// notify listeners that the maps with the given IDs should
				// be deleted - send event 
				eventAdminService.sendEvent(new Event(EventTopic.QS_LAYERS_DELETE.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", this);
						put("value", mapsToDelete);
					}
				}));
			}
		});
		
		final ToolItem tltmSavedMapRefresh = new ToolItem(toolBarSavedMaps, SWT.NONE);
		tltmSavedMapRefresh.setText("Refresh");
		tltmSavedMapRefresh.setImage(imgRefresh);
		// listener to remove maps saved in the Query Set 
		tltmSavedMapRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				treeViewerSavedMaps.refresh();
			}
		});
		
		ToolBar toolBarSavedMapsHelp = new ToolBar(compositeSavedMapsToolbar, SWT.FLAT | SWT.RIGHT);
		toolBarSavedMapsHelp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		
		final ToolItem tltmSavedMapHelp = new ToolItem(toolBarSavedMapsHelp, SWT.NONE);
		tltmSavedMapHelp.setText("");
		tltmSavedMapHelp.setImage(imgQuestion);
		tltmSavedMapHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// create the help controller 
				SwtUtil.createHelpController(compositeSavedMaps, 
						tltmSavedMapHelp, groupMaps, 
						"Saved WMS maps will be listed below. " + 
						"Click the 'Find maps' button above to find more maps. \n\n" + 
						"Maps can be re-arranged using drag and drop. " + 
						"Layers towards the bottom of the list will " +
						"have higher priority and will be shown above other " +
						"layers in the geo-browser.");
			}
		});		
		
		treeViewerSavedMaps = new CheckboxTreeViewer(groupMaps, SWT.BORDER | SWT.MULTI);
		Tree treeSavedMaps = treeViewerSavedMaps.getTree();
		treeSavedMaps.setHeaderVisible(true);
		treeSavedMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewerColumn treeViewerSavedMapName = new TreeViewerColumn(treeViewerSavedMaps, SWT.NONE);
		TreeColumn trclmnSavedMapName = treeViewerSavedMapName.getColumn();
		trclmnSavedMapName.setWidth(100);
		trclmnSavedMapName.setText("Name");
		treeViewerSavedMapName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsSavedMap) {
					WmsSavedMap model = (WmsSavedMap) element;
					return model.getName();
				}
				return "-";
			}
		});
		
		TreeViewerColumn treeViewerSavedMapTitle = new TreeViewerColumn(treeViewerSavedMaps, SWT.NONE);
		TreeColumn trclmnSavedMapTitle = treeViewerSavedMapTitle.getColumn();
		trclmnSavedMapTitle.setWidth(100);
		trclmnSavedMapTitle.setText("Title");
		treeViewerSavedMapTitle.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsSavedMap) {
					WmsSavedMap model = (WmsSavedMap) element;
					return model.getWmsLayerTitle();
				}
				return "-";
			}
		});
		
		TreeViewerColumn treeViewerSavedMapNotes = new TreeViewerColumn(treeViewerSavedMaps, SWT.NONE);
		TreeColumn trclmnSavedMapNotes = treeViewerSavedMapNotes.getColumn();
		trclmnSavedMapNotes.setWidth(200);
		trclmnSavedMapNotes.setText("Notes");
		treeViewerSavedMapNotes.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsSavedMap) {
					WmsSavedMap model = (WmsSavedMap) element;
					return model.getNotes();
				}
				return "-";
			}
		});		
		
		TreeViewerColumn treeViewerSavedMapUrl = new TreeViewerColumn(treeViewerSavedMaps, SWT.NONE);
		TreeColumn trclmnSavedMapUrl = treeViewerSavedMapUrl.getColumn();
		trclmnSavedMapUrl.setWidth(200);
		trclmnSavedMapUrl.setText("Service URL");
		treeViewerSavedMapUrl.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsSavedMap) {
					WmsSavedMap model = (WmsSavedMap) element;
					return model.getServiceEndpoint();
				}
				return "-";
			}
		});	
		
		// drag-n-drop support 
		Transfer[] transferTypes = { LocalSelectionTransfer.getTransfer() };
		treeViewerSavedMaps.addDragSupport(DND.DROP_MOVE, transferTypes, 
				new SavedMapsDragListener(treeViewerSavedMaps));
		treeViewerSavedMaps.addDropSupport(DND.DROP_MOVE, transferTypes, 
				new SavedMapsDropListener(treeViewerSavedMaps, savedMaps));
	
		// listener to update tool bar items, based on selection 
		treeViewerSavedMaps.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					StructuredSelection structuredSelection = 
							(StructuredSelection) selection; 
					// activate or deactivate relevant toolbar items
					// delete map 
					tltmSavedMapDelete.setEnabled(!structuredSelection.isEmpty());
				}
			}
		});
		
		/*
		 * Add a listener to detect when items are clicked, to:
		 * 
		 * 1. Maintain model status (active vs. inactive) 
		 * 2. Notify listeners that a map was activated/deactivated  
		 * 
		 */
		treeSavedMaps.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					Object item = e.item;
					if (item instanceof TreeItem) {
						TreeItem treeItem = (TreeItem) item;
						Object data = treeItem.getData();
						// only react to items with the right data model 
						if (data instanceof WmsSavedMap) {
							final WmsSavedMap map = (WmsSavedMap) data;
							
							// maintain activity status of model object 
							map.setActive(treeItem.getChecked());
							
							 // notify listeners that the layer should be toggled 
							eventAdminService.sendEvent(new Event(EventTopic.QS_MAPS_LAYER_TOGGLE.toString(), 
									new HashMap<String, Object>() { 
								{
									put("object", map);
								}
							}));
						}
					}
				}
			}			
		});			
		
		treeViewerSavedMaps.setContentProvider(new InnerContentProvider());
		treeViewerSavedMaps.setInput(savedMaps);
		
	
		/*
		 * Available maps 
		 */
		
		// helps to keep track of selected WMS services 
		final Service currentlySelectedService = new Service(ServiceType.WMS);
		final Service oldSelectedService = new Service(ServiceType.WMS);
		
		
		compositeAvailableMaps = new Composite(parent, SWT.NONE);
		compositeAvailableMaps.setLayout(new GridLayout(1, false));
		compositeAvailableMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// button to switch stack to go back to saved maps 
		Button btnSwitchStackBack = new Button(compositeAvailableMaps, SWT.NONE);
		btnSwitchStackBack.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		btnSwitchStackBack.setText("Back to saved maps");
		btnSwitchStackBack.setImage(imgBackControl);
		btnSwitchStackBack.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {

				// clear the selection of services and maps for this 
				// query set 
				mapDiscoveryClearSelection();
			
				/*
				 * notify listeners that all layers from 
				 * the map discovery (with service) should be removed 
				 */
				eventAdminService.sendEvent(new Event(EventTopic.QS_MAPS_DELETE_FROM_SERVICE.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", this);
					}
				}));				
				
				// put the right stack on top 
				compositeStackMapsLayout.topControl = compositeSavedMaps;
				parent.layout();
			}
		});		
		
		Group groupWmsMaps = new Group(compositeAvailableMaps, SWT.BORDER);
		groupWmsMaps.setLayout(new GridLayout(1, false));
		groupWmsMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		groupWmsMaps.setText("Available maps");

		SashForm sashFormMaps = new SashForm(groupWmsMaps, SWT.VERTICAL);
		sashFormMaps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeServices = new Composite(sashFormMaps, SWT.NONE);
		compositeServices.setLayout(new GridLayout(1, false));
		compositeServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ToolBar toolBarServices = new ToolBar(compositeServices, SWT.FLAT | SWT.RIGHT);
		toolBarServices.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ToolItem tltmManageServices = new ToolItem(toolBarServices, SWT.NONE);
		tltmManageServices.setText("Manage services...");
		tltmManageServices.setImage(imgDatabase);
		// listener
		tltmManageServices.addSelectionListener(new SelectionAdapter() {
			@Override 
			public void widgetSelected(SelectionEvent event) {
				// create and open the dialog to manage services 
				ManageAllServicesDialog dialog =
						new ManageAllServicesDialog(UIUtil.getShell(), 
								ServiceRoot.getInstance());
				dialog.open();
				dialog.close();
				// update the viewers whose input may have changed				
				tableViewerWmsServices.refresh();
			}
		});		
		
		ToolItem tltmRefreshServices = new ToolItem(toolBarServices, SWT.NONE);
		tltmRefreshServices.setText("Refresh");
		tltmRefreshServices.setImage(imgRefresh);
		// check all listener
		tltmRefreshServices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// update the viewers whose input may have changed				
				tableViewerWmsServices.refresh();
			}
		});	
		
		// tree viewer showing services 
		tableViewerWmsServices = new TableViewer(compositeServices, SWT.BORDER | SWT.MULTI);
		Table tableServices = tableViewerWmsServices.getTable();
		tableServices.setHeaderVisible(true);
		tableServices.setLinesVisible(true);
		tableServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tblViewerColumnServiceName = new TableViewerColumn(tableViewerWmsServices, SWT.NONE);
		TableColumn tbclmnServiceName = tblViewerColumnServiceName.getColumn();
		tbclmnServiceName.setWidth(200);
		tbclmnServiceName.setText("Service name");
		// label provider 
		tblViewerColumnServiceName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service model = (Service) element;
					return model.getName();
				}
				return "";		
			}
		});
		
		TableViewerColumn tblViewerColumnServiceUrl = new TableViewerColumn(tableViewerWmsServices, SWT.NONE);
		TableColumn tbclmnServiceUrl = tblViewerColumnServiceUrl.getColumn();
		tbclmnServiceUrl.setWidth(200);
		tbclmnServiceUrl.setText("Service URL");
		// label provider
		tblViewerColumnServiceUrl.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service model = (Service) element;
					return model.getEndpoint();
				}
				return "";
			}
		});	
		
		/*
		 * Selection listener to the services viewer. 
		 * 
		 * The listener help to maintain which maps that should be visible in 
		 * the map viewer. Maps from non-selected services are removed 
		 * from preview.  
		 * 
		 */
		tableViewerWmsServices.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("serial")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = 
							(IStructuredSelection) selection;
					Object elmt = structuredSelection.getFirstElement();
					
					// make sure we are handling the right model object 
					if (elmt instanceof Service) {
						final Service service = (Service) elmt; 

						// check if we have actually changed selection 
						// if this is not the first time a selection is made
						// (i.e. only if there was a previous selection)
						if (!findMapsNoServiceSelection) {
							// check if we have actually changed selection
							String currentEndpoint = 
									currentlySelectedService.getEndpoint();
							if (currentEndpoint != null) {
								// if we haven't changed service selection, 
								// do nothing
								if (service.getEndpoint().equals(currentEndpoint)) 
									return; 
							}
							findMapsNoServiceSelection = false;
						}
						
						// remember old selection
						oldSelectedService.setEndpoint(currentlySelectedService.getEndpoint());
						
						// remember last selected end point 
						currentlySelectedService.setEndpoint(service.getEndpoint());
						
						// reset number of selected layers 
						countSelectedWmsLayers = 0;
						
						// remove layers from previously selected service 
						final String oldEndpoint = oldSelectedService.getEndpoint();
						if (oldEndpoint != null) {
							/*
							 * notify listeners that all layers from 
							 * the old service should be removed 
							 */
							eventAdminService.sendEvent(new Event(EventTopic.QS_MAPS_DELETE_FROM_SERVICE.toString(), 
									new HashMap<String, Object>() { 
								{
									put("object", this);
									put("value", oldEndpoint);
								}
							}));							
						}

						// run in separate thread  
						Job job = new Job("Contacting WMS service") {
							protected IStatus run(IProgressMonitor monitor) { 
								monitor.beginTask("Retrieving map layers from WMS", 
										IProgressMonitor.UNKNOWN);
								
								// contact WMS service and request layers
								Collection<WmsLayerInfo> layers = 
										WmsUtil.getLayers(service.getEndpoint());
								
								if (layers != null) {
									
									final Collection<WmsMapLayer> maps = new ArrayList<WmsMapLayer>();
									for (WmsLayerInfo layerInfo : layers) {
										// add object to model collection 
										WmsMapLayer layer = new WmsMapLayer();
										// set map layer properties 
										layer.setServiceEndpoint(service.getEndpoint());
										layer.setName(layerInfo.getName());
										layer.setWmsLayerTitle(layerInfo.getTitle());
										maps.add(layer);
									}

									System.out.println("Number of maps: " + maps.size());

									// update UI component 
									UIUtil.update(new Runnable() {
										@Override
										public void run() {
											// enable viewer
											treeFiltered.setEnabled(true);
											treeWmsLayers.setEnabled(true);
											// update the input to the layer tree viewer 
											treeViewerWmsLayers.setInput(maps);
											treeViewerWmsLayers.refresh();
										}
									});

								} else {
									log.error("Something went wrong when contacting " + 
											"the WMS at " + service.getEndpoint());
									
									// update UI component 
									UIUtil.update(new Runnable() {
										@Override
										public void run() {
											// disable viewer
											treeFiltered.setEnabled(false);
											treeWmsLayers.setEnabled(false);
											// disable tool items 
											tltmSaveSelected.setEnabled(false);
											// update the input to the layer tree viewer 
											treeViewerWmsLayers.setInput(new Object[0]);
											treeViewerWmsLayers.refresh();
										}
									});									
									
									UIUtil.showErrorMessage("Something went wrong when contacting WMS at: \n\n" + 
											service.getEndpoint() + ". \n\nPlease try again later.");
								}								
								
								monitor.done(); 
								return org.eclipse.core.runtime.Status.OK_STATUS; 
							} 
						}; 
						job.setUser(true);
						job.schedule();						

					}
				}
			}
		});
		
		// only show WMSs
		tableViewerWmsServices.setContentProvider(new ServiceContentProvider(ServiceType.WMS));
		tableViewerWmsServices.setUseHashlookup(true);
		tableViewerWmsServices.setInput(ServiceRoot.getInstance());
		
		Composite compositeAvailableMapLayers = new Composite(sashFormMaps, SWT.NONE);
		compositeAvailableMapLayers.setLayout(new GridLayout(1, false));
		compositeAvailableMapLayers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ToolBar toolBarLayers = new ToolBar(compositeAvailableMapLayers, SWT.FLAT | SWT.RIGHT);
		
		tltmSaveSelected = new ToolItem(toolBarLayers, SWT.NONE);
		tltmSaveSelected.setText("Save active maps");
		tltmSaveSelected.setImage(imgSave);
		tltmSaveSelected.setEnabled(false);
		// listener, handles saving maps to the Query Set 
		tltmSaveSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// keep track of added map layers 
				int count = 0;
				for (TreeItem item : treeWmsLayers.getItems()) {
					if (item.getChecked()) {
						Object data = item.getData();
						// add the object that are of the right type 
						if (data instanceof WmsMapLayer) {
							WmsMapLayer mapLayer = (WmsMapLayer) data;
							// convert the object to a sub-class 
							WmsSavedMap map = WmsSavedMap.copy(mapLayer);
							// check that the map does not already exist
							boolean add = true;
							for (MapLayer existing : getSavedMaps()) {
								WmsSavedMap savedMap = (WmsSavedMap) existing;
								// skip map layers that already exist
								if (savedMap.getServiceEndpoint().equals(map.getServiceEndpoint()) && 
										savedMap.getWmsLayerTitle().equals(map.getWmsLayerTitle())) {
									add = false;
									break;
								}
							}
							// add the map to the list of saved maps, if 
							// appropriate 
							if (add) {
								// set active to false by default
								map.setActive(false);
								getSavedMaps().add(map);								
								count++;
							}
						}
					}
				}
				
				if (count > 0) {
				
					// show message 
					String msg = 
							(count > 1 ? count : "One") + 
							" map" + (count > 1 ? "s were" : " was") + 
							" saved to your query set!";
					UIUtil.showInfoMessage("Maps added to query set", msg);
					
					// update the viewers whose input may have changed
					treeViewerSavedMaps.refresh();
					
				} else {
					
					// show message 
					UIUtil.showInfoMessage("No maps added to query set", 
							"No maps were added, they might already exist in the query set!");					
				}
				
			}
		});
		
		PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(final Viewer viewer, final Object element) {
				TreeViewer treeViewer = (TreeViewer) viewer;
				int numberOfColumns = treeViewer.getTree().getColumnCount();
				boolean isMatch = false;
				for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
					ColumnLabelProvider labelProvider = 
							(ColumnLabelProvider)treeViewer.getLabelProvider(columnIndex);
					String labelText = labelProvider.getText(element);
					isMatch |= wordMatches(labelText);
				}
				return isMatch;	
			}
		};
		
		treeFiltered = 
				new FilteredTree(compositeAvailableMapLayers, SWT.BORDER | SWT.CHECK, filter, true);
		// disabled by default 
		treeFiltered.setEnabled(false);
		treeViewerWmsLayers = treeFiltered.getViewer();
		
		treeWmsLayers = treeViewerWmsLayers.getTree();
		// show headers 
		treeWmsLayers.setHeaderVisible(true);
		// disabled by default 
		treeFiltered.setEnabled(false);
		treeWmsLayers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewerColumn treeViewerColumnName = new TreeViewerColumn(treeViewerWmsLayers, SWT.NONE);
		TreeColumn trclmnLayerName = treeViewerColumnName.getColumn();
		trclmnLayerName.setWidth(100);
		trclmnLayerName.setText("Name");
		treeViewerColumnName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsMapLayer) {
					WmsMapLayer model = (WmsMapLayer) element;
					return model.getName();
				}
				return "-";
			}
		});
		
		TreeViewerColumn treeViewerColumnTitle = new TreeViewerColumn(treeViewerWmsLayers, SWT.NONE);
		TreeColumn trclmnLayerTitle = treeViewerColumnTitle.getColumn();
		trclmnLayerTitle.setWidth(100);
		trclmnLayerTitle.setText("Title");
		treeViewerColumnTitle.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsMapLayer) {
					WmsMapLayer model = (WmsMapLayer) element;
					return model.getWmsLayerTitle();
				}
				return "-";
			}
		});
		
		TreeViewerColumn treeViewerColumnService = new TreeViewerColumn(treeViewerWmsLayers, SWT.NONE);
		TreeColumn trclmnService = treeViewerColumnService.getColumn();
		trclmnService.setWidth(200);
		trclmnService.setText("Service");
		treeViewerColumnService.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WmsMapLayer) {
					WmsMapLayer model = (WmsMapLayer) element;
					return model.getServiceEndpoint();
				}
				return "-";
			}
		});	
		
		/*
		 * Add a listener to detect when items are clicked, to:
		 * 
		 * 1. Maintain model status (active vs. inactive) 
		 * 2. Activate or deactivate relevant toolbar items 
		 * 3. Notify listeners that a map was activated/deactivated  
		 * 
		 */
		treeWmsLayers.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("serial")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					Object item = e.item;
					if (item instanceof TreeItem) {
						TreeItem treeItem = (TreeItem) item;
						Object data = treeItem.getData();
						// only react to items with the right data model 
						if (data instanceof WmsMapLayer) {
							final WmsMapLayer mapLayer = (WmsMapLayer) data;
							
							// maintain activity status of model object 
							mapLayer.setActive(treeItem.getChecked());
							
							// keep track of selected items
							countSelectedWmsLayers += treeItem.getChecked() ? 1 : -1;
							
							// activate or deactivate relevant toolbar items
							tltmSaveSelected.setEnabled(countSelectedWmsLayers > 0);
							
							 // notify listeners that the layer should be toggled 
							eventAdminService.sendEvent(new Event(EventTopic.QS_MAPS_LAYER_TOGGLE.toString(), 
									new HashMap<String, Object>() { 
								{
									put("object", mapLayer);
								}
							}));							
						}
					}
				}
			}			
		});	
		
		treeViewerWmsLayers.setContentProvider(new InnerContentProvider());
		// default - empty input 
		treeViewerWmsLayers.setInput(new Object[0]);
	}
	
	/**
	 * Activates and shows the sensor stack item 
	 */
	public void showSensorStack() {
		compositeOuterLayout.topControl = sashSensors;
		compositeOuterStack.layout();
		showingSensorStack = true;
	}
	
	/**
	 * Activates and shows the maps stack item 
	 */
	public void showMapStack() {
		compositeOuterLayout.topControl = compositeMaps;
		compositeOuterStack.layout();				
		showingSensorStack = false;
	}
	
	/**
	 * Returns true if the sensor stack is currently on top 
	 * 
	 * @return
	 */
	public boolean isShowingSensorStack() {
		return showingSensorStack;
	}
	
	/**
	 * Creates a table viewer for services  
	 * 
	 * @param composite
	 * @return
	 */
	private CheckboxTableViewer createServiceTableViewer(Composite composite) {
		
		CheckboxTableViewer tableViewer = 
				CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.MULTI);

		TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
		colName.getColumn().setText("Name");
		colName.getColumn().setWidth(200);
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getName();
				}
				return "";
			}
		});
		
		final TableViewerColumn colUrl = new TableViewerColumn(tableViewer, SWT.NONE);
		colUrl.getColumn().setText("Url");
		colUrl.getColumn().setWidth(200);
		colUrl.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Service) {
					Service service = (Service) element;
					return service.getName();
				}
				return "";
			}
		});
		
		// services state listener
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				
				// update model status (active or inactive)
				Object elmt = event.getElement();
				if (elmt instanceof Service) {
					Service service = (Service) elmt;
					// important: update model status 
					service.setActive(event.getChecked());
				}
				
				// updates the selected services 
				updateSelectedServices();
				
				// mark as dirty
				setDirty(true);
			}
		});
		
		return tableViewer;
	}
	
	/**
	 * Manually checks active services 
	 * 
	 */
	public void checkActiveServices() {

		// must refresh the viewer first in case the model changed 
		tableViewerSosServices.refresh();

		// iterate over services and check items appropriately 
		for (Service service : getServices(ServiceType.SOS)) {
			tableViewerSosServices.setChecked(service, service.isActive());
		}

		// refresh again to show updated checked statuses 
		tableViewerSosServices.refresh();
	}
	
	/**
	 * Updates the selected services 
	 * 
	 */
	@SuppressWarnings("serial")
	public void updateSelectedServices() {
		
		// count the active services
		int countActiveServices = 0;
		for (Service service : getServices()) 
			countActiveServices += (service.isActive() ? 1 : 0);

		// active services might have changed - send event
		eventAdminService.sendEvent(new Event(EventTopic.QS_TOGGLE_SERVICES.toString(), 
				new HashMap<String, Object>() { 
			{
				put("object", offeringLayer);
				put("value", getServices());
			}
		}));
		
		// update live services tile
		updateLiveTileServices(countActiveServices);		
	}
	
	/**
	 * Manually checks active saved WMS maps 
	 * 
	 */
	public void checkActiveSavedMaps() {

		// must refresh the viewer first in case the model changed 
		treeViewerSavedMaps.refresh();

		// iterate over services and check items appropriately 
		for (MapLayer map : getSavedMaps()) {
			treeViewerSavedMaps.setChecked(map, map.isActive());
		}

		// refresh again to show updated checked statuses 
		treeViewerSavedMaps.refresh();
	}	
	
	/**
	 * Updates the active WMS maps
	 * 
	 */
	@SuppressWarnings("serial")
	public void updateSavedMaps() {
		for (final MapLayer map : getSavedMaps()) {
			if (map.isActive()) {
				// notify listeners that the layer should be toggled 
				eventAdminService.sendEvent(new Event(EventTopic.QS_MAPS_LAYER_TOGGLE.toString(), 
						new HashMap<String, Object>() { 
					{
						put("object", map);
					}
				}));
			}
		}
	}	
	
	/**
	 * Sets the bounding box region for the sensor offerings 
	 * 
	 * @param bbox
	 */
	@SuppressWarnings("serial")
	public void setSensorOfferingBoundingBox(final double[] bbox) {
		
		// update region buttons 
		btnClearRegion.setEnabled(true);
		
		// notify that the bounding box region should be set 
		eventAdminService.sendEvent(new Event(EventTopic.QS_REGION_SET.toString(), 
				new HashMap<String, Object>() { 
			{
				put("object", getMapId());
				put("value", bbox);
			}
		}));
	}
	
	/**
	 * Refresh viewers 
	 */
	public void refreshViewers() {
		tableViewerSosServices.refresh();
		treeViewerObservedProperties.refresh();
		tableViewerWmsServices.refresh();
		treeViewerSavedMaps.refresh();
	}

	/**
	 * Generate a StyledText widget as a header
	 *
	 * @param tile 
	 * @param parent
	 * @param title
	 * @return
	 */
	private Composite createLiveTop(final Tile tile, Composite parent, String title) {
		
		Composite composite = new Composite(parent, SWT.TRANSPARENT);
		composite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		composite.setLayoutData(gd);

		Label label = new Label(composite, SWT.NONE);
		// default image
		label.setImage(imgDotRed);

		StyledText styledText = new StyledText(composite, SWT.READ_ONLY | SWT.WRAP);
		styledText.setEnabled(false);
		styledText.setBlockSelection(true);
		styledText.setEditable(false);
		styledText.setDoubleClickEnabled(false);
		styledText.setCaret(null);
		styledText.setAlignment(SWT.RIGHT);
		styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));	
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		styledText.setAlignment(SWT.LEFT);
		styledText.setText(title);
		styledText.setFont(fontInactive);

		// add listeners 
		Composite c = null;
		switch (tile) {
		case SERVICES:
			c = tileServices;
			break;
		case GEOGRAPHIC: 
			c = tileGeographic;
			break;
		case TIME: 
			c = tileTime;
			break;
		case PROPERTIES:
			c = tileProperties;
			break;
		case FORMATS:
			c = tileFormats;
			break;
		case PREVIEW:
			c = tilePreview;
			break;
		case EXPORT:
			c = tileExport;
			break;
		}
		
		MouseAdapter adapter = new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					activateTile(tile);
				}
			};
		
		if (c != null) {
			c.addMouseListener(adapter);
		}
		
		composite.addMouseListener(adapter);
		label.addMouseListener(adapter);
		
		return composite;
	}

	/**
	 * Creates a live tile composite
	 *
	 * @param parent
	 * @return
	 */
	private StyledText createLiveTile(Composite parent) {
		StyledText styledText= new StyledText(parent, SWT.READ_ONLY | SWT.WRAP);
		styledText.setRightMargin(5);
		styledText.setBottomMargin(5);
		styledText.setTopMargin(5);
		styledText.setEnabled(false);
		styledText.setBlockSelection(true);
		styledText.setEditable(false);
		styledText.setDoubleClickEnabled(false);
		styledText.setCaret(null);
		styledText.setAlignment(SWT.RIGHT);
		styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		return styledText;
	}


	/**
	 * Preview the sensor data
	 *
	 * @param offeringItem
	 * @param observedProperty
	 * @param domainVariable
	 * @param rangeVariables
	 */
	@SuppressWarnings("serial")
	private void previewData(SensorOfferingItem offeringItem,
			String observedProperty,
			Field domainVariable, Collection<Field> rangeVariables)
	{

		Service service = offeringItem.getService();
		SensorOffering sensorOffering = offeringItem.getSensorOffering();

		// create the data fetching object
		DataFetcher fetcher =
				new DataFetcher(offeringItem, getActivePreviewFetchPeriod());

		try {

			SosDataRequest dataRequest =
					fetcher.makeDataRequests(service,
							sensorOffering, observedProperty);
			
			SensorData sensorData = fetcher.executeRequest(dataRequest);

			final DataPreviewEvent dataPreview =
					new DataPreviewEvent(sensorOffering.getGmlId(),
							observedProperty, sensorData,
							domainVariable, rangeVariables);

			// notify the data preview view to display the data
			eventAdminService.sendEvent(new Event(EventTopic.QS_PREVIEW_PLOT.toString(), 
					new HashMap<String, Object>() { 
				{
					put("object", this);
					put("value", dataPreview);
				}
			}));			

		} catch (ResponseFormatNotSupportedException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (SocketTimeoutException e) {
			log.warn("Socket timeout: " + e.getMessage());
			UIUtil.showInfoMessage("The connection timed out. " +
					"Please try again later or change timout settings " +
					"Preferences.");
		} catch (ExceptionReportException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}


	/**
	 * Returns the selected sensor offering if there is one, null otherwise
	 *
	 * @return
	 */
	private SensorOfferingItem getSensorOfferingSelection() {
		ISelection selection = tableViewerSensorOfferings.getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection structured =
					(StructuredSelection) selection;

			Object first = structured.getFirstElement();

			if (first instanceof SensorOfferingItem) {
				return ((SensorOfferingItem) first);
			}
		}
		// default
		return null;
	}

	/**
	 * Activates the given tile
	 *
	 * @param tile
	 */
	private void activateTile(Tile tile) {

		switch (tile) {
		case SERVICES:

			stackLayout.topControl = stackServices;

			/*
			 * Make the services table viewer the selection provider
			 */
			setActiveSelectionProvider(tableViewerSosServices);
			intermediator.setSelectionProviderDelegate(getActiveSelectionProvider());

			break;

		case GEOGRAPHIC:

			stackLayout.topControl = stackGeographicArea;

			break;

		case PROPERTIES:

			stackLayout.topControl = stackProperties;

			break;

		case TIME:

			stackLayout.topControl = stackTime;

			break;

		case FORMATS:

			stackLayout.topControl = stackFormats;

			break;

		case PREVIEW:

			stackLayout.topControl = stackPreview;

			// bring the property sheet to the front by default
			if (!previewTileActive) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().
						getActivePage().showView(IPageLayout.ID_PROP_SHEET);
				} catch (PartInitException exception) {
					log.error("Part init exception: " + exception.getMessage());
				}
			}

			/*
			 * Make the sensor offering list viewer the selection provider
			 */
			setActiveSelectionProvider(tableViewerSensorOfferings);
			intermediator.setSelectionProviderDelegate(getActiveSelectionProvider());

			break;

		case EXPORT:

			stackLayout.topControl = stackExport;

			break;
		}

		previewTileActive = (tile.equals(Tile.PREVIEW) ? true : false);

		// update the stack layout
		stack.layout();

		// updates the selected tile style
		updateSelectedTile(tile);
	}

	/**
	 * Sets the active structure viewer
	 *
	 * @param viewer
	 */
	private void setActiveSelectionProvider(StructuredViewer viewer) {
		activeSelectionProvider = viewer;
	}

	/**
	 * Returns the active selection provider
	 *
	 * @return
	 */
	public StructuredViewer getActiveSelectionProvider() {
		return activeSelectionProvider;
	}

	/**
	 * Updates the UI with available variables
	 *
	 * @param offeringItem
	 */
	private void updateAvailableVariables(SensorOfferingItem offeringItem) {

		SensorOffering sensorOffering = offeringItem.getSensorOffering();
		String offeringId = sensorOffering.getGmlId();

		String property = comboObservedProperties.getText();
		String observedProperty =
				(String)comboObservedProperties.getData(property);

		Variables variables =
				VariablesHolder.getInstance().getVariables(offeringId,
						observedProperty);

		if (variables != null) {

			// show details for previewing data
			showPreviewDetails(true);

			String[] vars = variables.getVariableStrings();
			/*
			 * Update domain
			 */
			comboPlotTimeSeriesDomain.setItems(variables.getVariableStrings());
			comboPlotTimeSeriesDomain.setEnabled(true);

			/*
			 * Update range
			 */
			tableTimeSeriesVariables.removeAll();
			for (String var : vars) {
				TableItem item = new TableItem(tableTimeSeriesVariables, SWT.NONE);
				item.setText(var);
			}
			if (vars.length > 0)
				tableTimeSeriesVariables.setEnabled(true);

			// guess the default domain variable
			guessTimeSeriesDomainVarible(variables);

			// guess the default range variable
			guessTimeSeriesRangeVaribles(variables);

		}
	}

	/**
	 * Prepare for preview
	 *
	 * - See if we have variables to show
	 * - If so, update plot information
	 * - If not, enable the 'prepare preview' button
	 */
	private void prepareForPreview() {

		SensorOfferingItem offeringItem = getSensorOfferingSelection();
		if (offeringItem != null) {

			SensorOffering offering = offeringItem.getSensorOffering();
			String offeringId = offering.getGmlId();
			String property = comboObservedProperties.getText();
			String observedProperty =
					(String)comboObservedProperties.getData(property);

			if (observedProperty != null) {
				VariablesHolder holder = VariablesHolder.getInstance();
				Variables variables =
						holder.getVariables(offeringId, observedProperty);

				/*
				 * We have variables ready
				 *
				 */
				if (variables != null) {

					// show details
					showPreviewDetails(true);

					// create variables
					comboPlotTimeSeriesDomain.setItems(variables.getVariableStrings());
					comboPlotTimeSeriesDomain.setEnabled(true);

					tableTimeSeriesVariables.removeAll();
					for (String var : variables.getVariableStrings()) {
						TableItem item =
								new TableItem(tableTimeSeriesVariables, SWT.NONE);
						item.setText(var);
					}
					tableTimeSeriesVariables.setEnabled(true);

					// guess the default domain variable
					guessTimeSeriesDomainVarible(variables);

					// guess the default domain variable
					guessTimeSeriesRangeVaribles(variables);

					// disable button
					btnFetchPreview.setEnabled(false);

					// automatically update plot 
					previewSensorData();
					
				} else {

					// hide details
					showPreviewDetails(false);

					// clear variables
					comboPlotTimeSeriesDomain.removeAll();
					comboPlotTimeSeriesDomain.setEnabled(false);

					tableTimeSeriesVariables.removeAll();
					tableTimeSeriesVariables.setEnabled(false);

					// enable button
					btnFetchPreview.setEnabled(true);
				}
			}
		}
	}

	/**
	 * Toggles the preview stack
	 *
	 * @param show
	 */
	private void showPreviewDetails(boolean show) {
		if (show) {
			// show
			stackLayoutPreview.topControl = compositePreviewAvailable;
		} else {
			// hide
			stackLayoutPreview.topControl = compositePreviewNeeded;
		}
		// update layout
		compositePreview.layout();
	}

	/**
	 *
	 * @param varibles
	 */
	private void guessTimeSeriesDomainVarible(Variables varibles) {
		// guess the default domain variable
		int index = -1;
		int i = 0;
		for (Field field : varibles.getVariables()) {
			if (field.isTimeField()) {
				index = i;
				break;
			}
			// TODO: better guessing mechanism
			else if (field.toString().contains("date")) {
				index = i;
				break;
			}
			i++;
		}

		// select it if we can
		if (index != -1) {
			comboPlotTimeSeriesDomain.select(index);
		}
	}

	/**
	 *
	 * @param varibles
	 */
	private void guessTimeSeriesRangeVaribles(Variables varibles) {

		// guess the default range variable
		for (Field field : varibles.getVariables()) {
			if (!TimeSeriesUtil.notRangeCandidate(field)) {
				// select the first one for now...
				for (TableItem item : tableTimeSeriesVariables.getItems()) {
					if (item.getText().equals(field.getName()))
						item.setChecked(true);
					else
						item.setChecked(false);
				}
				break;
			}

		}
	}


	/**
	 * Collects the facets to use as restriction
	 *
	 * @param item
	 * @return
	 */
	private Collection<FacetChangeToggle> collectFacets(TreeItem item) {
		Collection<FacetChangeToggle> changes =
				new ArrayList<FacetChangeToggle>();
		TreeItem[] children = item.getItems();
		Object root = item.getData();
		// we are collecting facets from a category
		if (root instanceof Category) {
			for (TreeItem child : children) {
				Object data = child.getData();
				if (data instanceof ObservedProperty) {
					// get the actual URI value
					String uri = ((ObservedProperty) data).getObservedProperty();
					FacetChangeToggle toggle =
							new FacetChangeToggle(Facet.OBSERVED_PROPERTY,
									child.getChecked(), uri);
					changes.add(toggle);
				}
			}
		}
		// we are only looking at a single observed property
		else if (root instanceof ObservedProperty) {
			// get the actual URI value
			String uri = ((ObservedProperty) root).getObservedProperty();
			FacetChangeToggle toggle =
					new FacetChangeToggle(Facet.OBSERVED_PROPERTY,
							item.getChecked(), uri);
			changes.add(toggle);
		}

		return changes;
	}

	/**
	 * Updates the observed properties for this query set
	 *
	 * @param facetData
	 * @param noMatchingOfferings
	 */
	public void updateObservedProperties(final FacetData facetData,
			final int noMatchingOfferings)
	{
		
		// collect the selected observed properties
		final int checked = getSelectedObservedProperties().size();

		// retain model information (state: active/inactive) 
		Collection<ObservedProperty> checkedOPs = 
				new ArrayList<ObservedProperty>();
		for (Category cat : observedPropertiesHolder.getCategories()) {
			for (ObservedProperty op : cat.getObservedProperties()) {
				if (op.isChecked())
					checkedOPs.add(op);
			}
		}

		// update the viewer model
		observedPropertiesHolder.setCategories(facetData);
		
		// update model information (state: active/inactive) 
		for (Category cat : observedPropertiesHolder.getCategories()) {
			for (ObservedProperty op : cat.getObservedProperties()) {
				// update model object from retained information (see above)
				if (checkedOPs.contains(op)) 
					op.setChecked(true);
				// update model object when loading a saved query set 
				if (activeObservedPropertyURIs.contains(op.getObservedProperty()))
					op.setChecked(true);
			}
		}
		
		// reset the active observed properties
		activeObservedPropertyURIs.clear();

		UIUtil.update(new Runnable() {
			@Override
			public void run() {
				
				if (checked > 0) {
					itemClearProperties.setEnabled(true);
				} else {
					itemClearProperties.setEnabled(false);
				}
		
				treeViewerObservedProperties.refresh();
				treeViewerObservedProperties.expandAll();

				// update live tile
				updateLiveTileObservedProperties(checked,
						noMatchingOfferings);
			}
		});
	}

	/**
	 *
	 * @param items
	 * @return
	 */
	private Collection<String> findCheckedObservedProperties(TreeItem[] items) {
		Collection<String> checked = new HashSet<String>();
		for (TreeItem item : items) {
			checked.addAll(findCheckedObservedProperties(item));
		}
		return checked;
	}

	/**
	 *
	 * @param item
	 * @return
	 */
	private Collection<String> findCheckedObservedProperties(TreeItem item) {
		TreeItem[] children = item.getItems();
		if (children.length == 0) {
			// add the item
			Collection<String> properties = new HashSet<String>();
			Object data = item.getData();
			if (item.getChecked() && data instanceof ObservedProperty) {
				ObservedProperty prop = (ObservedProperty) data;
				properties.add(prop.getObservedProperty());
			}
			return properties;
		}
		return findCheckedObservedProperties(children);
	}

	/**
	 * Updates the sensor offerings for this query set
	 *
	 * @param sector
	 * @param countBySector
	 */
	public void updateGeographicArea(final Sector sector,
			final int countBySector)
	{
		this.sector = sector;
		UIUtil.update(new Runnable() {
			@Override
			public void run() {
				// update live tile
				updateLiveTileGeographic(sector, countBySector);
			}
		});
	}

	/**
	 * Updates tile info
	 *
	 * @param count
	 */
	public void updateTime(final int count)
	{
		UIUtil.update(new Runnable() {
			@Override
			public void run() {
				// update live tile
				updateLiveTileTime(getActiveTimePeriod(), count);
			}
		});
	}


	/**
	 * Updates the sensor offerings for this query set
	 *
	 * @param offerings
	 */
	public void updateSensorOfferings(final Collection<SensorOfferingItem> offerings)
	{

		// update the viewer model
		sensorOfferingsHolder.setSensorOfferings(offerings);

		UIUtil.update(new Runnable() {
			@Override
			public void run() {
				tableViewerSensorOfferings.refresh();
				
				int selectedProperties = getSelectedObservedProperties().size();

				// update live tile
				updateLiveTilePreview(offerings.size(), selectedProperties);

				// update live tile
				updateLiveTileExport(offerings.size(), selectedProperties);
			}
		});
	}

	/**
	 * Updates the possibilities for selecting an observed property
	 *
	 * @param offeringItem
	 */
	private void updateObservedPropertiesSelection(SensorOfferingItem offeringItem) {

		if (offeringItem != null) {
			java.util.List<String> properties =
					offeringItem.getSensorOffering().getObservedProperties();
			java.util.List<String> labels = new ArrayList<String>();

			for (String property : properties) {
				String label = Labeling.labelProperty(property);

				labels.add(label);

				// save the URI as data on the widget
				comboObservedProperties.setData(label, property);
			}
			// set the combo box values
			comboObservedProperties.setItems(labels.toArray(new String[labels.size()]));

			// select the first one
			if (labels.size() > 0) {
				// update label
				lblObservedProperties.setText(labels.size() + " observed properties");
				// enable combo box
				comboObservedProperties.setEnabled(true);
				// select first item
				comboObservedProperties.select(0);
				// prepare for preview
				prepareForPreview();
			} else {
				// update label
				lblObservedProperties.setText(strNoObservedProperties);
				// disable combo box
				comboObservedProperties.setEnabled(false);
			}
		} else {
			// update label
			lblObservedProperties.setText(strNoObservedProperties);
			// clear observed properties
			comboObservedProperties.setItems(new String[0]);
			// disable combo box
			comboObservedProperties.setEnabled(false);
		}
		comboObservedProperties.redraw();
	}

	/**
	 * Updates tile info
	 *
	 * @param count
	 */
	public void updateFormats(Collection<String> formats, final int count) {

		// update the viewer model
		availableFormatsHolder.setAvailableFormats(formats);

		UIUtil.update(new Runnable() {
			@Override
			public void run() {

				tableViewerUnsupportedFormats.refresh();

				// update live tile
				updateLiveTileFormats(getActiveFormatRestriction(), count);
			}
		});
	}

	/**
	 * Returns the map layers that are part of this query set  
	 * 
	 * @return
	 */
	public Collection<IMapLayer> getMapLayers() {
		Collection<IMapLayer> maps = new ArrayList<IMapLayer>();
		// add the sensor offering layer
		maps.add(offeringLayer);
		// add all other maps 
		maps.addAll(getSavedMaps());
		return maps;
	}

	/**
	 * Returns map IDs associated with this query set
	 *
	 * Implements the {@link MapIdentifier} interface
	 */
	@Override
	public MapId getMapId() {
		return offeringLayer.getMapId();
	}

	/**
	 * Sets the map IDs 
	 *
	 * Implements the {@link MapIdentifier} interface
	 */	
	@Override 
	public void setMapId(MapId mapId) {
		offeringLayer.setMapId(mapId);
	}
	
	/**
	 * Adds a service to this query set 
	 * 
	 * Implements @{link ServiceManager}
	 * 
	 * @param service
	 */
	@Override
	public boolean addService(Service service) {
		if (!services.contains(service))
			return services.add(service);
		return false;
	}
	
	/**
	 * Removes a service from this query set 
	 * 
	 * Implements @{link ServiceManager}
	 * 
	 * @param service 
	 */
	@Override
	public boolean removeService(Service service) {
		return services.remove(service);
	}
	
	/**
	 * Returns the services for this query set 
	 * 
	 * Implements @{link ServiceManager}
	 * 
	 * @return
	 */
	@Override
	public Collection<Service> getServices() {
		return services;
	}
	
	/**
	 * Returns the services matching the given type 
	 * 
	 * Implements @{link ServiceManager} 
	 * 
	 * @param type
	 */
	@Override
	public Collection<Service> getServices(ServiceType type) {
		Collection<Service> res = new ArrayList<Service>();
		for (Service service : services) {
			if (service.getServiceType().equals(type))
				res.add(service);
		}
		return res;
	}
	
	/**
	 * Selects an offering, invoked by the map view
	 *
	 * @param offeringItem
	 */
	public void selectSensorOffering(final SensorOfferingItem offeringItem) {

		UIUtil.update(new Runnable() {
			@Override
			public void run() {

				// activate the preview tile
				activateTile(Tile.PREVIEW);

				StructuredSelection selection =
						new StructuredSelection(offeringItem);

				tableViewerSensorOfferings.setSelection(selection, true);
				tableViewerSensorOfferings.reveal(selection);

				intermediator.fireSelectionChanged(selection);
			}
		});
	}

	/**
	 * Executes the steps for previewing sensor data
	 *
	 */
	private void previewSensorData() {

		// show plot view 
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().
			getActivePage().showView(DataPreviewView.ID);
		} catch (PartInitException exception) {
			log.error("Part init exception: " + exception.getMessage());
		}

		final Collection<Field> rangeVariables = new ArrayList<Field>();
		for (TableItem tableItem : tableTimeSeriesVariables.getItems()) {
			if (tableItem.getChecked()) {
				rangeVariables.add(new Field(tableItem.getText()));
			}
		}

		final Field domain =
				new Field(comboPlotTimeSeriesDomain.getText());

		final SensorOfferingItem offeringItem = getSensorOfferingSelection();

		// validate
		if (offeringItem != null &&
				rangeVariables.size() > 0 &&
				!domain.getName().equals(""))
		{

			// get observed property 
			String property = comboObservedProperties.getText();
			final String observedProperty =
					(String)comboObservedProperties.getData(property);

			Job job = new Job("Updating preview") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					try {

						monitor.beginTask("Updating preview",
								IProgressMonitor.UNKNOWN);

						/*
						 * Preview the data
						 */
						previewData(offeringItem, observedProperty,
								domain, rangeVariables);

					} finally {
						monitor.done();
					}

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}


	/**
	 * Update live tile
	 *
	 * @param noOfServices
	 */
	private void updateLiveTileServices(int noOfServices) {
		String text = "";
		boolean warning = false;
		if (noOfServices <= 0) {
			text = "No services selected";
			warning = true;
		}
		else if (noOfServices == 1)
			text = "One service selected";
		else
			text = noOfServices + " services selected";

		// set text
		liveServices.setText(text);

		updateTile(Tile.SERVICES, warning);
	}

	/**
	 * Update live tile
	 *
	 * @param sector
	 * @param noOfOfferings
	 */
	private void updateLiveTileGeographic(Sector sector, int noOfOfferings) {

		String text = "";

		if (sector == null) {
			text += "No restriction ";
		} else {
			text += "Restriction active"; 
//					Util.join(sector.asList(), ",");
		}

		text += "\n\n";

		boolean warning = false;
		if (noOfOfferings <= 0) {
			text += "No sensor offerings in region";
			warning = true;
		}
		else if (noOfOfferings == 1)
			text += "One sensor offering in region";
		else
			text += noOfOfferings + " sensor offerings in region";

		// set text
		liveGeographic.setText(text);

		updateTile(Tile.GEOGRAPHIC, warning);
	}

	/**
	 * Update live tile
     *
	 * @param noProperties Number of observed properties
	 * @param noMatches Number of matching offerings
	 */
	private void updateLiveTileObservedProperties(int noProperties, int noMatches) {

		String text = "";
		boolean warning = false;

		if (noProperties <= 0) {
			text += "No observed properties selected";
		}
		else if (noProperties == 1)
			text += "One observed property selected";
		else
			text += noProperties + " observed properties selected";

		text += "\n\n";

		if (noMatches <= 0) {
			text += "No matched sensor offerings";
			warning = true;
		}
		else if (noMatches == 1)
			text += "One matching sensor offering";
		else
			text += noMatches + " matching sensor offerings";

		// set text
		liveProperties.setText(text);

		updateTile(Tile.PROPERTIES, warning);
	}

	/**
	 * Update live tile
     *
	 * @param timeFacet
	 */
	private void updateLiveTileTime(TimeFacet timeFacet, int count) {

		String text = "";
		boolean warning = false;

		switch (timeFacet) {
		case ALL:
			text += "No restriction";
			break;
		case ONEDAY:
			text += "Last 24 hours";
			break;
		case ONEWEEK:
			text += "Last week";
			break;
		}

		text += "\n\n";

		if (count < 0) {
			text += "All sensor offerings matching";
		}
		else if (count == 0) {
			text += "No matched sensor offerings";
			warning = true;
		}
		else if (count == 1)
			text += "One matching sensor offering";
		else
			text += count + " matching sensor offerings";

		// set text
		liveTime.setText(text);

		updateTile(Tile.TIME, warning);
	}

	/**
	 * Update live tile
     *
	 * @param timeFacet
	 */
	private void updateLiveTileFormats(FormatFacet formatFacet, int count) {

		String text = "";
		boolean warning = false;

		switch (formatFacet) {
		case ALL:
			text += "No restriction";
			break;
		case SUPPORTED:
			text += "Only supported formats";
			break;
		}

		text += "\n\n";

		if (count == 0) {
			text += "No matched sensor offerings";
			warning = true;
		}
		else if (count == 1)
			text += "One matching sensor offering";
		else
			text += count + " matching sensor offerings";

		// set text
		liveFormats.setText(text);

		updateTile(Tile.FORMATS, warning);
	}

	/**
	 * Update live tile
     *
	 * @param timeFacet
	 * @param selectedProperties
	 */
	private void updateLiveTilePreview(int count, int selectedProperties) {
		String text = "";
		boolean warning = false;
		
		if (count > 0) {
			text += "Let's look at some data!";
		} else {
			text += "No data to preview";
			warning = true;
		}
			
		// set text
		livePreview.setText(text);

		updateTile(Tile.PREVIEW, warning);
	}

	/**
	 * Update live tile
     *
	 * @param timeFacet
	 * @param selectedProperties
	 */
	private void updateLiveTileExport(int count, int selectedProperties) {
		String text = "";
		boolean warning = false;
		
		if (count > 0 && selectedProperties > 0) {
			text += "Let's execute the query set!";
			
		} else {
			text += "Empty query set";
			if (selectedProperties <= 0)
				text += "\n\nNo observed properties selected";
				
			warning = true;
		}
		
		// update button status
		if (btnExportData != null)
			btnExportData.setEnabled(!warning);

		// set text
		liveExport.setText(text);

		updateTile(Tile.EXPORT, warning);
	}


	/**
	 * Update tile status
	 *
	 * @param tile
	 * @param warning
	 */
	private void updateTile(Tile tile, boolean warning) {

		boolean active = tileActive.equals(tile);

		if (warning) {
			if (active)
				tileStatus.put(tile, Status.ACTIVE_WARNING);
			else
				tileStatus.put(tile, Status.INACTIVE_WARNING);
		} else {
			if (active)
				tileStatus.put(tile, Status.ACTIVE_OK);
			else
				tileStatus.put(tile, Status.INACTIVE_OK);
		}

		// update tile
		updateTile(tile);
	}

	/**
	 * Updates the selected tile
	 *
	 * @param tile
	 */
	private void updateSelectedTile(Tile tile) {

		// make all ACTIVE tiles INACTIVE, but maintain warning level
		for (Tile s : tileStatus.keySet()) {
			if (tileStatus.get(s).equals(Status.ACTIVE_OK))
				tileStatus.put(s, Status.INACTIVE_OK);
			else if (tileStatus.get(s).equals(Status.ACTIVE_WARNING))
				tileStatus.put(s, Status.INACTIVE_WARNING);
		}

		// make the tile active
		if (tileStatus.get(tile).equals(Status.INACTIVE_WARNING))
			tileStatus.put(tile, Status.ACTIVE_WARNING);
		else
			tileStatus.put(tile, Status.ACTIVE_OK);

		tileActive = tile;

		updateTiles();
	}


	/**
	 * Updates all tiles according to their status
	 *
	 */
	private void updateTiles() {
		for (Tile section : Tile.values()) {
			updateTile(section);
		}
	}

	/**
	 * Updates the given tile section according to their status
	 *
	 * @param section
	 */
	private void updateTile(Tile section) {
		updateTileStatus(section, tileStatus.get(section));
	}

	/**
	 * Updates the status of the given control
	 *
	 * @param composite
	 * @param status
	 */
	private void updateTileStatus(Composite composite, Status status) {

		Control[] controls = composite.getChildren();
		if (controls.length == 2) {
			// update the top part
			Control top = controls[0];
			if (top instanceof Composite) {
				Composite comp = (Composite) top;
				for (Control child : comp.getChildren()) {
					// update the image label 
					if (child instanceof Label) {
						Label label = (Label) child;
						switch (status) {
						case ACTIVE_OK:
						case INACTIVE_OK:
							label.setImage(imgDotGreen);
							break;
						case ACTIVE_WARNING:
						case INACTIVE_WARNING:
							label.setImage(imgDotRed);
							break;

						}	
					} 
					// update the top label 
					else if (child instanceof StyledText) {
						StyledText styledText = (StyledText) child;
						// switch
						switch (status) {
						case ACTIVE_OK:
						case ACTIVE_WARNING:
							styledText.setFont(fontActive);
							break;
						case INACTIVE_OK:
						case INACTIVE_WARNING:
							styledText.setFont(fontInactive);
							break;

						}
					}
				}
			}
			// update the bottom part 
			Control bottom = controls[1];
			if (bottom instanceof StyledText) {
				StyledText styledText = (StyledText) bottom;
				// switch
				switch (status) {
				case ACTIVE_OK:
				case ACTIVE_WARNING:
					styledText.setBackground(colorBg);
					break;
				case INACTIVE_OK:
				case INACTIVE_WARNING:
					styledText.setBackground(colorWidgetShadow);
					break;
				}							
			}
		}
	}

	/**
	 * Updates the status of the given section tile
	 *
	 * @param section
	 * @param status
	 */
	private void updateTileStatus(Tile section, Status status) {

		if (status != null) {
			switch (section) {
			case SERVICES:
				updateTileStatus(tileServices, status);
				break;
			case GEOGRAPHIC:
				updateTileStatus(tileGeographic, status);
				break;
			case PROPERTIES:
				updateTileStatus(tileProperties, status);
				break;
			case TIME:
				updateTileStatus(tileTime, status);
				break;
			case FORMATS:
				updateTileStatus(tileFormats, status);
				break;
			case PREVIEW:
				updateTileStatus(tilePreview, status);
				break;
			case EXPORT:
				updateTileStatus(tileExport, status);
				break;
			}
		} else
			log.warn("Tile status was null");
	}

	/**
	 * Handles the toggling of buttons when selecting a time period
	 *
	 * @param widget Button just clicked
	 */
	private void updateTimeButtonSelection(Widget widget) {
		if (widget instanceof Control) {
			Control control = (Control) widget;
			for (Control child : control.getParent().getChildren()) {
				if (child instanceof Button &&
						(child.getStyle() & SWT.TOGGLE) != 0) {
					((Button) child).setSelection(false);
				}
			}
			if (widget instanceof Button) {
				((Button) widget).setSelection (true);
			}
		}
	}

	/**
	 * Handles the toggling of buttons when selecting a format facet
	 *
	 * @param widget Button just clicked
	 */
	private void updateFormatButtonSelection(Widget widget) {
		if (widget instanceof Control) {
			Control control = (Control) widget;
			for (Control child : control.getParent().getChildren()) {
				if (child instanceof Button &&
						(child.getStyle() & SWT.TOGGLE) != 0) {
					((Button) child).setSelection(false);
				}
			}
			if (widget instanceof Button) {
				((Button) widget).setSelection (true);
			}
		}
	}

	/**
	 * Notify about time facet change
	 *
	 * @param timeFacet
	 */
	@SuppressWarnings("serial")
	private void updateTimeRestriction(TimeFacet timeFacet) {

		// save the currently specified time period type
		setActiveTimePeriod(timeFacet);

		 // update live time tile
//		updateLiveTileTime(timeFacet);

		final FacetChangeToggle change =
				new FacetChangeToggle(Facet.TIME_PERIOD, true,
						timeFacet.toString());
		
		eventAdminService.sendEvent(new Event(EventTopic.QS_FACET_CHANGED.toString(), 
				new HashMap<String, Object>() { 
			{
				put("object", getMapId());
				put("value", change);
			}
		}));		
	}

	/**
	 * Notify about format facet change
	 *
	 * @param formatFacet
	 */
	@SuppressWarnings("serial")
	private void updateFormatRestriction(FormatFacet formatFacet) {

		// save the currently specified format restriction
		setActiveFormatRestriction(formatFacet);

		switch (formatFacet) {
		case ALL:

			// clear response FORMAT facets
			eventAdminService.sendEvent(new Event(EventTopic.QS_FACET_CLEARED.toString(), 
					new HashMap<String, Object>() { 
				{
					put("object", getMapId());
					put("value", Facet.RESPONSE_FORMAT);
				}
			}));
			
			break;

		case SUPPORTED:

			// construct list of supported facets
			final java.util.List<FacetChangeToggle> changes =
				new ArrayList<FacetChangeToggle>();

			for (SupportedResponseFormats format : SupportedResponseFormats.values()) {
				FacetChangeToggle change =
						new FacetChangeToggle(Facet.RESPONSE_FORMAT, true,
								format.toString());
				changes.add(change);
			}

			eventAdminService.sendEvent(new Event(EventTopic.QS_FACET_CHANGED.toString(), 
					new HashMap<String, Object>() { 
				{
					put("object", getMapId());
					put("value", changes);
				}
			}));			

			break;
		}
	}


	/**
	 * Saves the current facet state
	 *
	 * @param mapId
	 * @param facets
	 */
    private void updateFacetState(Collection<FacetChangeToggle> facets)
    {
		for (FacetChangeToggle change : facets) {

			/*
			 * NOTE: we are here only interested in keeping track of
			 *       the observed properties facets
			 */
			if (change.getStatus()) {
				// keep constraint if it was turned on
				activeFacets.add(change);
			} else {
				// remove constraint if it was turned off
				activeFacets.remove(change);
			}
		}
    }

    /**
     * Exports data 
     * 
     */
    private void exportData() {
    	
    	Collection<SensorOfferingItem> offeringItems = 
    			sensorOfferingsHolder.getSensorOfferings();
    	
    	Collection<String> observedProperties = getSelectedObservedProperties();

    	// TODO: do not hard code the time period
    	Collection<DownloadModel> models =
    			DownloadModelHelper.createDownloadModels(offeringItems, 
    					observedProperties, new Period(24, 0, 0, 0));

    	/*
    	 * Open dialog to select download location  
    	 */
    	DirectoryDialog dialogDir = new DirectoryDialog(site.getShell());
        dialogDir.setText("Select destination folder");
        String dir = dialogDir.open();
        if (dir != null) {
        	File folder = new File(dir);
        	/*
        	 * Open fetch progress dialog 
        	 */
        	GetObservationProgressDialog dialog = 
        			new GetObservationProgressDialog(site.getShell(), folder);
        	dialog.setInput(models);
        	dialog.open();         	
        }
    }
    
 
    /**
     * Collects the active observed properties facets 
     * 
     * @return
     */
    private Collection<String> getSelectedObservedProperties() {
    	Collection<String> properties = new ArrayList<String>();
    	for (FacetChangeToggle facet : activeFacets) {
    		if (facet.getFacet().equals(Facet.OBSERVED_PROPERTY) && 
    				facet.getStatus()) 
    		{
    			// add the facet
    			properties.add(facet.getValue());
    		}
    	}
    	return properties;
    }
    
    /**
     * Returns true if the observed property is part of the active facets,
     * false otherwise
     *
     * @param property
     * @return
     */
    private boolean isActiveFacet(ObservedProperty property) {
    	for (FacetChangeToggle facet : activeFacets) {
    		if (facet.getValue().equals(property.toString()))
    			return true;
    	}
    	// default
    	return false;
    }

	/**
	 * Sets the active time period
	 *
	 * @param timeFacet
	 */
	private void setActiveTimePeriod(TimeFacet timeFacet) {
		this.activeTimeFacet = timeFacet;
	}

	/**
	 * Returns the active time period
	 *
	 * @return
	 */
	private TimeFacet getActiveTimePeriod() {
		return activeTimeFacet;
	}

	/**
	 * Sets the active format restriction
	 *
	 * @param timeFacet
	 */
	private void setActiveFormatRestriction(FormatFacet formatFacet) {
		this.activeFormatFacet = formatFacet;
	}

	/**
	 * Returns the active time period
	 *
	 * @return
	 */
	private FormatFacet getActiveFormatRestriction() {
		return activeFormatFacet;
	}

	/**
	 * Returns the period for which to download preview sensor data
	 *
	 * @return
	 */
	private Period getActivePreviewFetchPeriod() {
		TimeFacet timeFacet = getActiveTimePeriod();
		switch (timeFacet) {
		// a user will be able to download "ALL" data, but the preview
		// period will default to 24 hours
		case ALL:
		case ONEDAY:
			// 24 hours
			return new Period(24, 0, 0, 0);
		case ONEWEEK:
			// one week
			return new Period(0, 0, 1, 0, 0, 0, 0, 0);
		default:
			// 24 hours
			return new Period(24, 0, 0, 0);
		}
	}
	
	/**
	 * Clears the selection of services and map layers for this 
	 * query set 
	 * 
	 */
	public void mapDiscoveryClearSelection() {
		// clear selection of services 
		tableViewerWmsServices.setSelection(new StructuredSelection());	

		// clear selection of layers from previously selected service
		treeViewerWmsLayers.setInput(new Object[0]);
	}

	/**
	 * Enumeration for the tiles of the discovery interface
	 *
	 * @author Jakob Henriksson
	 *
	 */
	private enum Tile {
		SERVICES,
		GEOGRAPHIC,
		PROPERTIES,
		TIME,
		FORMATS,
		PREVIEW,
		EXPORT
	}

	private enum Status {
		NONE,
		ACTIVE_OK,
		ACTIVE_WARNING,
		INACTIVE_OK,
		INACTIVE_WARNING
	}

	/*
	 * Dispose of used resources 
	 */
	private void disposeResources() {
		if (imgDocument != null)
			imgDocument.dispose();
		if (imgSectorSelection != null)
			imgSectorSelection.dispose();
		if (imgSectorClear != null)
			imgSectorClear.dispose();
		if (imgLike != null)
			imgLike.dispose();
		if (imgDislike != null)
			imgDislike.dispose();
		if (imgChart != null)
			imgChart.dispose();
		if (imgMap != null)
			imgMap.dispose();
		if (imgDatabase != null)
			imgDatabase.dispose();
		if (imgDelete != null)
			imgDelete.dispose();
		if (imgClear != null)
			imgClear.dispose();
		if (imgSave != null)
			imgSave.dispose();
		if (imgRefresh != null)
			imgRefresh.dispose();
		if (imgDotRed != null)
			imgDotRed.dispose();
		if (imgDotGreen != null)
			imgDotGreen.dispose();
		if (imgAddMap != null)
			imgAddMap.dispose();
		if (imgBackControl != null) 
			imgBackControl.dispose();
		if (imgQuestion != null)
			imgQuestion.dispose();
		if (imgAdd != null)
			imgAdd.dispose();
		if (imgArrowUp != null)
			imgArrowUp.dispose();
		if (imgArrowDown != null)
			imgArrowDown.dispose();
		
		if (colorBg != null)
			colorBg.dispose();
		
	}

	/**
	 * Returns true if the query set is dirty
	 *
	 * @return
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Sets whether the query set is dirty or not
	 * 
	 * @param status 
	 */
	public void setDirty(boolean status) {
		// update status
		dirty = status;
		// update name
		setText(querySetName);
	}
	
	/**
	 * Returns true if the query set has been saved before
	 *
	 * @return
	 */
	public boolean isSaved() {
		return saved;
	}

	/**
	 * Sets whether the query set has been saved before 
	 * 
	 * @param status 
	 */
	public void setSaved(boolean status) {
		// update status
		saved = status;
	}	
	
	@Override
	public void setText(String text) {
		this.querySetName = text;
		super.setText((isDirty() ? dirtyPrefix : "") + text);
	}
	
	public String getQuerySetName() {
		return querySetName;
	}
	
	/**
	 * Names, or renames, a query set 
	 * 
	 * @param tab
	 */
	public void nameQuerySet() {

		IInputValidator validator = new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if (newText.trim().equals(""))
					return "You must provide a non-empty name";
				// input OK
				return null;
			}
		};

		InputDialog dialog =
				new InputDialog(Display.getCurrent().getActiveShell(),
						"Please provide a name", "Name:",
						getQuerySetName(), validator);

		if (dialog.open() == Window.OK) {
			String name = dialog.getValue().trim();
			// set new name
			setText(name);
		}
	}	
	
	/**
	 * Adds a saved WMS map 
	 * 
	 * @param map
	 * @return
	 */
	public boolean addSavedMap(MapLayer map) {
		if (!savedMaps.contains(map))
			return savedMaps.add(map);
		return false;
	}
	
	/**
	 * Returns the saved WMS maps
	 * 
	 * @return
	 */
	public Collection<MapLayer> getSavedMaps() {
		return savedMaps;
	}
	
	/**
	 * Returns the observed properties holder 
	 * 
	 * @return
	 */
	public ObservedPropertiesHolder getObservedPropertiesHolder() {
		return observedPropertiesHolder;
	}
	
	/**
	 * Sets the active observed properties 
	 * 
	 * @param ops
	 */
	@SuppressWarnings("serial")
	public void setActiveObservedProperties(final Collection<String> ops) {
		
		final Collection<FacetChangeToggle> changes = 
				new ArrayList<FacetChangeToggle>();
		
		for (String opUri : ops) {
			FacetChangeToggle facet =
					new FacetChangeToggle(Facet.OBSERVED_PROPERTY,
							true, opUri);
			changes.add(facet);
		}
		
		// update state of facet changes
		updateFacetState(changes);
		
		// indicate that the following observed property URIs should 
		// be set to be true by default 
		this.activeObservedPropertyURIs.addAll(ops);

		// fire event
		eventAdminService.sendEvent(new Event(EventTopic.QS_FACET_CHANGED.toString(), 
				new HashMap<String, Object>() { 
			{
				put("object", getMapId());
				put("value", changes);
			}
		}));		
	}
	
	/**
	 * Returns the specified sector 
	 * 
	 * @return
	 */
	public Sector getSector() {
		return sector;
	}
	
	/**
	 * Content provider for service viewers
	 * 
	 * @author Jakob Henriksson 
	 *
	 */
	private static class ServiceContentProvider implements IStructuredContentProvider {

		Object[] EMPTY_ARRAY = new Object[0];
		
		private ServiceType serviceType;

		/**
		 * Constructor 
		 * 
		 * @param serviceType
		 */
		public ServiceContentProvider(ServiceType serviceType) {
			this.serviceType = serviceType;
		}

		@Override
		public Object[] getElements(Object element) {
			if (element instanceof ServiceManager) {
				return ((ServiceManager) element).getServices(serviceType).toArray();
			}
			// most generic, an array 
			else if (element instanceof ArrayList) {
				return ((ArrayList<?>) element).toArray();
			} 
			return EMPTY_ARRAY;
		}
		
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}	
	
	/**
	 * Content provider for service viewers
	 * 
	 * @author Jakob Henriksson 
	 *
	 */
	private static class InnerContentProvider implements ITreeContentProvider {

		Object[] EMPTY_ARRAY = new Object[0];

		@Override
		public Object[] getChildren(Object parent) {

			// handles collections 
			if (parent instanceof Collection<?>) {
				return ((Collection<?>) parent).toArray();
			} 

			return EMPTY_ARRAY;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}		
}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset.ui;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;
import org.joda.time.Period;

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
import com.iai.proteus.dialogs.ManageServicesDialog;
import com.iai.proteus.dialogs.ServiceContentProvider;
import com.iai.proteus.exceptions.ResponseFormatNotSupportedException;
import com.iai.proteus.map.WorldWindUtils;
import com.iai.proteus.model.MapId;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.plot.Variables;
import com.iai.proteus.plot.VariablesHolder;
import com.iai.proteus.queryset.DataExporter;
import com.iai.proteus.queryset.DataFetcher;
import com.iai.proteus.queryset.Facet;
import com.iai.proteus.queryset.FacetChangeToggle;
import com.iai.proteus.queryset.FacetData;
import com.iai.proteus.queryset.FormatFacet;
import com.iai.proteus.queryset.QuerySetEvent;
import com.iai.proteus.queryset.QuerySetEventListener;
import com.iai.proteus.queryset.QuerySetEventNotifier;
import com.iai.proteus.queryset.QuerySetEventType;
import com.iai.proteus.queryset.TimeFacet;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.views.DataPreviewEvent;
import com.iai.proteus.views.DataPreviewView;
import com.iai.proteus.views.MapIdentifier;
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
	implements MapIdentifier, QuerySetEventListener
{

	private static final Logger log = Logger.getLogger(QuerySetTab.class);

	/*
	 * True if changes have not been saved, false otherwise
	 */
	private boolean dirty;

	/*
	 * Prefix added to dirty/modified query sets
	 */
	public static String dirtyPrefix = "*";

	/*
	 * This array holds the map IDs that are used to
	 * synchronize between workspace views and the map views
	 */
	private ArrayList<MapId> mapIds;

	/*
	 * Holds all the sensor offerings
	 */
	private SensorOfferingsHolder sensorOfferingsHolder;

	/*
	 * Holds all the observed properties
	 */
	private ObservedPropertiesHolder observedPropertiesHolder;

	/*
	 * Holds all the available sensor data formats
	 */
	private AvailableFormatsHolder availableFormatsHolder;


	// active facets that constrain what is being displayed
	private Set<FacetChangeToggle> activeFacets;


	/*
	 * Stacks
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
	private boolean showingMapStack;
	
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
	private CheckboxTableViewer tableViewerLikedServices;
	private Table tableLikedServices;

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

	private Button btnFetchPreview;
	private Button btnExportData;

	private ToolItem itemClearProperties;
	
	


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
	private Image imgSensors;
	private Image imgMap; 

	/*
	 * Colors
	 */
	private Color colorActiveOk;
	private Color colorActiveWarning;
	private Color colorInactiveOk;
	private Color colorInactiveWarning;

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
	public QuerySetTab(IWorkbenchPartSite site,
			SelectionProviderIntermediate intermediator,
			CTabFolder parent, int style)
	{
		super(parent, style);

		this.site = site;
		this.intermediator = intermediator;

		/*
		 * Colors
		 */
		colorActiveOk = SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);
//		colorActiveOk = new Color(Display.getCurrent(), 216, 255, 216);
		colorActiveWarning = SWTResourceManager.getColor(SWT.COLOR_DARK_RED);
//		colorActiveWarning = new Color(Display.getCurrent(), 255, 170, 170);

//		colorInactiveOk = new Color(Display.getCurrent(), 216, 255, 216);
		colorInactiveOk = SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);
//		colorInactiveWarning = new Color(Display.getCurrent(), 255, 170, 170);
		colorInactiveWarning = SWTResourceManager.getColor(SWT.COLOR_DARK_RED);

		tileStatus = new HashMap<Tile, Status>();

		activeFacets = new HashSet<FacetChangeToggle>();

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

		/*
		 * Resources
		 */
		imgDocument = UIUtil.getImage("icons/fugue/document.png");
		imgSectorSelection = UIUtil.getImage("icons/fugue/zone--plus.png");
		imgSectorClear = UIUtil.getImage("icons/fugue/zone--minus.png");
		imgLike = UIUtil.getImage("icons/fugue/star.png");
		imgDislike = UIUtil.getImage("icons/fugue/star-empty.png");
		imgSensors = UIUtil.getImage("icons/fugue/chart.png");
		imgMap = UIUtil.getImage("icons/fugue/map.png");

		cursor = getDisplay().getSystemCursor(SWT.CURSOR_HAND);

		/*
		 * Defaults
		 */

		this.tileActive = Tile.SERVICES;
		this.activeTimeFacet = TimeFacet.ALL;
		this.activeFormatFacet = FormatFacet.ALL;

		setText(dirtyPrefix + "Untitled");
		setImage(imgDocument);
		// unsaved
		dirty = false;

		mapIds = new ArrayList<MapId>();
		/*
		 * Generate and add the default map Id for this map layer
		 */
		MapId defaultMapId = MapId.generateNewMapId();
		mapIds.add(defaultMapId);

		/*
		 * Create tab interface
		 */
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

		// force the creation of the default map (so we at least can use
		// the selector tool)
		QuerySetEventNotifier.getInstance().fireEvent(this,
				QuerySetEventType.QUERYSET_INITIALIZE_LAYER, defaultMapId);

		// make this class listener for events
		QuerySetEventNotifier.getInstance().addListener(this);
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

		createLiveTop(tileServices, "Services >");
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

		createLiveTop(tileGeographic, "Geographic area >");
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

		createLiveTop(tileTime, "Time >");
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

		createLiveTop(tileProperties, "Observed properties >");
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

		createLiveTop(tileFormats, "Data formats >");
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

		createLiveTop(tilePreview, "Preview >");
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

		createLiveTop(tileExport, "Export sensor data >");
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

		final ToolBar toolBarServices = new ToolBar(stackServices, SWT.FLAT | SWT.RIGHT);

		ToolItem itemServicesManage = new ToolItem(toolBarServices, SWT.NONE);
		itemServicesManage.setText("Manage services");
		itemServicesManage.setImage(UIUtil.getImage("icons/fugue/database.png"));
		// manage services listener
		itemServicesManage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ManageServicesDialog dialog =
						new ManageServicesDialog(site.getShell());
				int res = dialog.open();
				if (res == IDialogConstants.OK_ID) {
					tableViewerLikedServices.refresh();
				}
			}
		});

//		new ToolItem(toolBarServices, SWT.SEPARATOR);

//		ToolItem itemServicesAll = new ToolItem(toolBarServices, SWT.NONE);
//		itemServicesAll.setText("All");
//		itemServicesAll.setImage(UIUtil.getImage("icons/fugue/ui-check-box.png"));
//		// check all listener
//		itemServicesAll.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// check all
//				tableViewerServices.setAllChecked(true);
//				// TODO: notify listeners of change
//			}
//		});
//
//		ToolItem itemServicesNone = new ToolItem(toolBarServices, SWT.NONE);
//		itemServicesNone.setText("None");
//		itemServicesNone.setImage(UIUtil.getImage("icons/fugue/ui-check-box-uncheck.png"));
//		// check none listener
//		itemServicesNone.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// check none
//				tableViewerServices.setAllChecked(false);
//				// TODO: notify listeners of change
//			}
//		});
		
		// "liked" services 

		tableViewerLikedServices = createServiceTableViewer(stackServices);
		
		tableLikedServices = tableViewerLikedServices.getTable();
		tableLikedServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableLikedServices.setHeaderVisible(true);
		tableLikedServices.setLinesVisible(false);

		tableViewerLikedServices.setContentProvider(new ServiceContentProvider());
		tableViewerLikedServices.setUseHashlookup(true);
		tableViewerLikedServices.setInput(ServiceRoot.getInstance());
		
		// "disliked" services
		
//		tableViewerDislikedServices = createServiceTableViewer(stackServices);
//		
//		tableDislikedServices = tableViewerDislikedServices.getTable();
//		tableDislikedServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		tableDislikedServices.setHeaderVisible(true);
//		tableDislikedServices.setLinesVisible(false);
//
//		tableViewerDislikedServices.setContentProvider(new ServiceContentProvider());
//		tableViewerDislikedServices.setUseHashlookup(true);
//		tableViewerDislikedServices.setInput(ServiceRoot.getInstance());
//		
		Collection<TableViewer> viewers = new ArrayList<TableViewer>();
		viewers.add(tableViewerLikedServices);
//		viewers.add(tableViewerDislikedServices);
		
		
		// add listeners 
//		addDoubleClickListener(tableViewerLikedServices, viewers); 
//		addDoubleClickListener(tableViewerDislikedServices, viewers);


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

		final Button btnGeographic = new Button(stackGeographicArea, SWT.PUSH);
		btnGeographic.setText(strSectorOn);
		btnGeographic.setImage(imgSectorSelection);
		btnGeographic.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));

		final Button btnClearRegion = new Button(stackGeographicArea, SWT.PUSH);
		btnClearRegion.setText(strSectorOff);
		btnClearRegion.setImage(imgSectorClear);
		btnClearRegion.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		// default
		btnClearRegion.setEnabled(false);

		btnGeographic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// send notification that sector is enabled
				QuerySetEventNotifier.getInstance().fireEvent(this,
						QuerySetEventType.QUERYSET_REGION_ENABLED,
						getDefaultMapId());
				// enable 'clear region' button
				btnClearRegion.setEnabled(true);
			}
		});

		btnClearRegion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// send notification that sector should be cleared
				QuerySetEventNotifier.getInstance().fireEvent(this,
						QuerySetEventType.QUERYSET_REGION_DISABLE,
						getDefaultMapId());
				// disable 'clear region' button
				btnClearRegion.setEnabled(false);
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
		itemClearProperties.setImage(UIUtil.getImage("icons/fugue/cross.png"));
		// default
		itemClearProperties.setEnabled(false);
		// listener
		itemClearProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// clear OBSERVED PROPERTIES facets
				QuerySetEventNotifier.getInstance().fireEvent(getDefaultMapId(),
						QuerySetEventType.QUERYSET_FACET_CLEAR,
						Facet.OBSERVED_PROPERTY);
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					TreeItem item = (TreeItem) e.item;
					final Collection<FacetChangeToggle> changes = collectFacets(item);
					
					// update state of facet changes
					updateFacetSelection(null, (java.util.List<?>) changes);

					// fire event
					new Thread(new Runnable() {
						public void run() {
							QuerySetEventNotifier.getInstance().fireEvent(getDefaultMapId(),
									QuerySetEventType.QUERYSET_FACET_CHANGE, changes);
						}
					}).start();
				}
			}
		});

		ContentProvider contentProvider = new ContentProvider();
		LabelProvider labelProvider = new LabelProvider();

		treeViewerObservedProperties.setContentProvider(contentProvider);
		treeViewerObservedProperties.setLabelProvider(labelProvider);
		treeViewerObservedProperties.setInput(observedPropertiesHolder);
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

		tableViewerSupportedFormats.setContentProvider(ArrayContentProvider.getInstance());
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

//		tableViewerUnsupportedFormats.setContentProvider(ArrayContentProvider.getInstance());
		tableViewerUnsupportedFormats.setContentProvider(contentProvider);
//		tableViewerUnsupportedFormats.setLabelProvider(labelProvider);
//		tableViewerUnsupportedFormats.setUseHashlookup(true);
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
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object elmt =
							((StructuredSelection) selection).getFirstElement();
					if (elmt instanceof SensorOfferingItem) {
						SensorOffering offering =
								((SensorOfferingItem) elmt).getSensorOffering();

						Position pos =
								WorldWindUtils.getCentralPosition(offering);

						QuerySetEventNotifier.getInstance().fireEvent(this,
								QuerySetEventType.QUERYSET_FLY_TO_OFFERING,
								new LatLon(pos.getLatitude().degrees,
										pos.getLongitude().degrees));
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
		btnFetchPreview.setImage(UIUtil.getImage("icons/fugue/chart.png"));
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
		btnTimeSeries.setImage(UIUtil.getImage("icons/fugue/chart.png"));
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


		/*
		 * Listeners
		 */

		// SERVICES
		tileServices.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.SERVICES);
			}
		});

		// GEOGRAPHIC
		tileGeographic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.GEOGRAPHIC);
			}
		});

		// PROPERTIES
		tileProperties.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.PROPERTIES);
			}
		});

		// TIME
		tileTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.TIME);
			}
		});

		// FORMATS
		tileFormats.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.FORMATS);
			}
		});

		// PREVIEW
		tilePreview.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.PREVIEW);
			}
		});

		// SUMMARY
		tileExport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				activateTile(Tile.EXPORT);
			}
		});
		
		// default stack 
		compositeOuterLayout.topControl = sashSensors;
		compositeOuterStack.layout();
		showingSensorStack = true;
		showingMapStack = false;
		
		/*
		 * Maps
		 */
		
		compositeMaps = new Composite(compositeOuterStack, SWT.NONE);
		compositeMaps.setLayout(new GridLayout());
		Label labelHolder = new Label(compositeMaps, SWT.NONE);
		labelHolder.setText("Maps - not implemented");
		labelHolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	}
	
	/**
	 * Activates and shows the sensor stack item 
	 */
	public void showSensorStack() {
		compositeOuterLayout.topControl = sashSensors;
		compositeOuterStack.layout();
		showingSensorStack = true;
		showingMapStack = false;
	}
	
	/**
	 * Activates and shows the maps stack item 
	 */
	public void showMapStack() {
		compositeOuterLayout.topControl = compositeMaps;
		compositeOuterStack.layout();				
		showingSensorStack = false;
		showingMapStack = true;
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
				CheckboxTableViewer.newCheckList(composite, SWT.BORDER);

//		TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
//		colName.getColumn().setText("Name");
//		colName.getColumn().setWidth(100);
//		colName.setLabelProvider(new ColumnLabelProvider() {
//			@Override
//			public String getText(Object element) {
//				Service service = (Service) element;
//				return service.getName();
//			}
//		});
		
//		final TableViewerColumn colFav = new TableViewerColumn(tableViewer, SWT.NONE);
//		colFav.getColumn().setText("");
//		colFav.getColumn().setWidth(20);
//		colFav.setLabelProvider(new ColumnLabelProvider() {
//			@Override
//			public String getText(Object element) {
//				return "";
//			}
//			
//			@Override
//			public Image getImage(Object element) {
//				Service service = (Service) element;
//				if (service.isLiked())
//					return imgLike;
//				return imgDislike;
//			}
//		});	
		
		final Table table = tableViewer.getTable();
		
//		TableLayout tableLayout = new TableLayout();
//		tableLayout.addColumnData(new ColumnWeightData(1, true));
////		tableLayout.addColumnData(new ColumnWeightData(1, true));
//		table.setLayout(tableLayout);
		
		// services state listener
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {

				java.util.List<Service> services = new ArrayList<Service>();
				for (TableItem item : table.getItems()) {
					if (item.getChecked()) {
						Object data = item.getData();
						if (data instanceof Service) {
							Service service = (Service) data;
							service.setActive(true);
							services.add(service);
						}
					}
				}

				/*
				 * Notify that this service has been toggled for
				 * this query set
				 */
				QuerySetEventNotifier.getInstance().fireEvent(services,
						QuerySetEventType.QUERYSET_SERVICE_TOGGLE,
						getDefaultMapId());

				/*
				 * Update live services tile
				 */
				updateLiveTileServices(services.size());
			}
		});
		
		
		return tableViewer;
	}

	
	/**
	 * Add a listener to table viewer 
	 * 
	 * @param tableViewer
	 * @param viewersToUpdate
	 */
	private void addDoubleClickListener(TableViewer tableViewer, final Collection<TableViewer> viewersToUpdate) {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection s = event.getSelection();
				if (s instanceof StructuredSelection) {
					StructuredSelection selection = (StructuredSelection) s;
					if (selection.size() == 1) {
						Object data = selection.getFirstElement();
						if (data instanceof Service) {
							Service service = (Service) data;
							if (service.isLiked())
								service.dislike();
							else
								service.like();
							for (TableViewer viewer : viewersToUpdate) {
								viewer.refresh();
							}
						}						
					}
				}
				
			}
		});		
	}
	
	/**
	 * Refresh viewers 
	 */
	public void refreshViewers() {
		tableViewerLikedServices.refresh();
	}

	/**
	 * Generate a StyledText widget as a header
	 *
	 * @param parent
	 * @param title
	 * @return
	 */
	private StyledText createLiveTop(Composite parent, String title) {

		StyledText styledText = new StyledText(parent, SWT.READ_ONLY | SWT.WRAP);
		styledText.setLeftMargin(10);
		styledText.setTopMargin(10);
		styledText.setRightMargin(10);
		styledText.setBottomMargin(10);
		styledText.setEnabled(false);
		styledText.setBlockSelection(true);
		styledText.setEditable(false);
		styledText.setDoubleClickEnabled(false);
		styledText.setCaret(null);
		styledText.setAlignment(SWT.RIGHT);
		styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// set and update the live tile text
		styledText.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		styledText.setAlignment(SWT.LEFT);
		styledText.setText(title);

		return styledText;
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

			DataPreviewEvent dataPreview =
					new DataPreviewEvent(sensorOffering.getGmlId(),
							observedProperty, sensorData,
							domainVariable, rangeVariables);

			// notify the data preview view to display the data
			QuerySetEventNotifier.getInstance().fireEvent(this,
					QuerySetEventType.QUERYSET_PREVIEW_PLOT, dataPreview);

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
			setActiveSelectionProvider(tableViewerLikedServices);
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
		Object parent = item.getData();
		// we are collecting facets from a category
		if (parent instanceof Category) {
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
		else if (parent instanceof ObservedProperty) {
			// get the actual URI value
			String uri = ((ObservedProperty) parent).getObservedProperty();
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

		UIUtil.update(new Runnable() {
			@Override
			public void run() {

				// collect the selected observed properties
//				Collection<String> checked =
//						findCheckedObservedProperties(
//								treeObservedProperties.getItems());
				int checked = getSelectedObservedProperties().size();
				System.out.println("CHECKED: " + checked);

				// update the viewer model
				observedPropertiesHolder.setObservedProperties(facetData);

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
	 * Returns map IDs associated with this query set
	 *
	 * Implements the {@link MapIdentifier} interface
	 */
	@Override
	public ArrayList<MapId> getMapIds() {
		return mapIds;
	}

	/**
	 * Returns the default (first) map id
	 *
	 * Implements the {@link MapIdentifier} interface
	 */
	@Override
	public MapId getDefaultMapId() {
		return mapIds.get(0);
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
	 * @param c
	 * @param status
	 */
	private void updateTileStatusOld(Control c, Status status) {
		Color color = null;
		switch (status) {
		case ACTIVE_OK:
			color = colorActiveOk;
			break;
		case ACTIVE_WARNING:
			color = colorActiveWarning;
			break;
		case INACTIVE_OK:
			color = colorInactiveOk;
			break;
		case INACTIVE_WARNING:
			color = colorInactiveWarning;
			break;
		default:
			color = SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			break;
		}
		if (color != null) {
			c.setBackground(color);
		}

		// update font settings
		if (c instanceof StyledText) {
			StyledText text = (StyledText) c;

			StyleRange styleRange = new StyleRange();
			styleRange.start = 0;
			styleRange.length = text.getText().length();
//			styleRange.fontStyle = SWT.BOLD;

			if (status.equals(Status.ACTIVE_OK) ||
					status.equals(Status.ACTIVE_WARNING)) {
				styleRange.foreground =
						SWTResourceManager.getColor(SWT.COLOR_WHITE);
			} else {
				styleRange.foreground =
						SWTResourceManager.getColor(SWT.COLOR_BLACK);
			}

			text.setStyleRange(styleRange);
		}
	}

	/**
	 * Updates the status of the given control
	 *
	 * @param composite
	 * @param status
	 */
	private void updateTileStatus(Composite composite, Status status) {
		Color color = null;

		// decide colors
		switch (status) {
		case ACTIVE_OK:
			color = colorActiveOk;
			break;
		case ACTIVE_WARNING:
			color = colorActiveWarning;
			break;
		case INACTIVE_OK:
			color = colorInactiveOk;
			break;
		case INACTIVE_WARNING:
			color = colorInactiveWarning;
			break;
		case NONE:
			color = SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
		default:
			color = SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
			break;
		}

		// update color
		if (color != null) {
			composite.setBackground(color);
		}

		Control[] controls = composite.getChildren();
		if (controls.length == 2) {
			Control top = controls[0];
			if (top instanceof StyledText) {
				StyledText styledText = (StyledText) top;
				// switch
				switch (status) {
				case ACTIVE_OK:
				case ACTIVE_WARNING:
//					styledText.setAlignment(SWT.RIGHT);
					styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
					break;
				case INACTIVE_OK:
				case INACTIVE_WARNING:
//					styledText.setAlignment(SWT.LEFT);
					styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
					break;

				}
			}
			Control live = controls[1];
			if (live instanceof Composite && color != null) {
				live.setBackground(color);
				live.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
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
	private void updateTimeRestriction(TimeFacet timeFacet) {

		// save the currently specified time period type
		setActiveTimePeriod(timeFacet);

		 // update live time tile
//		updateLiveTileTime(timeFacet);

		FacetChangeToggle change =
				new FacetChangeToggle(Facet.TIME_PERIOD, true,
						timeFacet.toString());
		QuerySetEventNotifier.getInstance().fireEvent(getDefaultMapId(),
				QuerySetEventType.QUERYSET_FACET_CHANGE, change);
	}

	/**
	 * Notify about format facet change
	 *
	 * @param formatFacet
	 */
	private void updateFormatRestriction(FormatFacet formatFacet) {

		// save the currently specified format restriction
		setActiveFormatRestriction(formatFacet);

		switch (formatFacet) {
		case ALL:

			// clear response FORMAT facets
			QuerySetEventNotifier.getInstance().fireEvent(getDefaultMapId(),
					QuerySetEventType.QUERYSET_FACET_CLEAR, Facet.RESPONSE_FORMAT);

			break;

		case SUPPORTED:

			// construct list of supported facets
			java.util.List<FacetChangeToggle> changes =
				new ArrayList<FacetChangeToggle>();

			for (SupportedResponseFormats format : SupportedResponseFormats.values()) {
				FacetChangeToggle change =
						new FacetChangeToggle(Facet.RESPONSE_FORMAT, true,
								format.toString());
				changes.add(change);
			}

			QuerySetEventNotifier.getInstance().fireEvent(getDefaultMapId(),
					QuerySetEventType.QUERYSET_FACET_CHANGE, changes);

			break;
		}
	}

	/**
	 * Implements #{link QuerySetEventListener} interface.
	 *
	 */
	@Override
	public void querySetEventHandler(QuerySetEvent event) {
		Object obj = event.getEventObject();
		Object value = event.getValue();

		if (obj instanceof MapId) {
			MapId mapId = (MapId) obj;

			// only listen to info about our own query set
			if (mapId.equals(getDefaultMapId())) {

				switch (event.getEventType()) {
				case QUERYSET_FACET_CLEAR:

					if (value instanceof Facet) {
						Facet facet = (Facet) value;
						if (facet.equals(Facet.OBSERVED_PROPERTY)) {
							// remove all remembered observed properties facets
							activeFacets.clear();
						}
					}

					break;

				case QUERYSET_FACET_CHANGE:

//					if (value instanceof java.util.List) {
//
//						// update state of facet changes
//						updateFacetSelection((MapId) obj, (java.util.List<?>) value);
//
//					} else if (value instanceof FacetChangeToggle) {
//
//						// update state of facet changes
//						updateFacetSelection((MapId) obj, (FacetChangeToggle) value);
//					}

					break;

				}

				// update the viewer
				UIUtil.update(new Runnable() {
					@Override
					public void run() {
						treeViewerObservedProperties.refresh();		
					}
				});
				
			}
		}
	}

    /**
     * Notifies the appropriate layers of the change in facet selection
     *
     * @param mapId
     * @param facet
     */
    private void updateFacetSelection(MapId mapId, FacetChangeToggle facet)
    {
    	Collection<FacetChangeToggle> changes = new ArrayList<FacetChangeToggle>();
    	changes.add(facet);

    	updateFacetState(mapId, changes);
    }


    /**
     * Notifies the appropriate layers of the change in facet selection
     *
     * @param mapId
     * @param facets
     */
    private void updateFacetSelection(MapId mapId, Collection<?> facets) {

    	/*
    	 * Make sure we are dealing with objects of the right type
    	 */
    	Collection<FacetChangeToggle> changes = new ArrayList<FacetChangeToggle>();
    	for (Object obj : facets) {
    		if (obj instanceof FacetChangeToggle) {
    			changes.add((FacetChangeToggle) obj);
    		}
    	}

    	updateFacetState(mapId, changes);
    }

    /**
     * Saves the current facet state
     *
     * @param mapId
     * @param facets
     */
    private void updateFacetState(MapId mapId,
    		Collection<FacetChangeToggle> facets)
    {
		for (Object obj : facets) {

			if (obj instanceof FacetChangeToggle) {

				FacetChangeToggle change = (FacetChangeToggle) obj;

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
     * Exports data 
     * 
     */
    private void exportData2() {
    	
    	Collection<SensorOfferingItem> offeringItems = 
    			sensorOfferingsHolder.getSensorOfferings();
    	
    	Collection<String> observedProperties = getSelectedObservedProperties();

    	// TODO: do not hard code the time period
    	Collection<DownloadModel> models =
    			DownloadModelHelper.createDownloadModels(offeringItems, 
    					observedProperties, new Period(24, 0, 0, 0));
    	
    	File folder = new File("/Users/b0kaj/Downloads/data");

    	try {
    		
			new ProgressMonitorDialog(site.getShell()).run(true,
					true, new DataExporter(models, folder));
			
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Override
	public void dispose() {
		super.dispose();
		imgDocument.dispose();
		imgDocument = null;
		imgSectorSelection.dispose();
		imgSectorSelection = null;
		imgSectorClear.dispose();
		imgSectorClear = null;
		imgLike.dispose();
		imgLike = null;
		imgDislike.dispose();
		imgDislike = null;
		imgSensors.dispose();
		imgSensors = null;
		imgMap.dispose();
		imgMap = null;

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
	 * Flags the query set as dirty
	 */
	public void setDirty() {
		dirty = true;
		String name = getText();
		if (!name.startsWith(dirtyPrefix)) {
			setText(dirtyPrefix + name);
		}
	}
}

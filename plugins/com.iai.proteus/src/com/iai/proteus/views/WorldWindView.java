/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;
import gov.nasa.worldwindx.examples.util.ToolTipController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.eclipse.albireo.core.SwingControl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.iai.proteus.Activator;
import com.iai.proteus.common.LatLon;
import com.iai.proteus.common.event.Event;
import com.iai.proteus.common.event.EventListener;
import com.iai.proteus.common.event.EventNotifier;
import com.iai.proteus.common.event.EventType;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.communityhub.Group;
import com.iai.proteus.map.DataPlotProvenanceLayer;
import com.iai.proteus.map.GeoRssLayer;
import com.iai.proteus.map.MarkerSelection;
import com.iai.proteus.map.SectorSelector;
import com.iai.proteus.map.SelectionNotifier;
import com.iai.proteus.map.SensorOfferingMarker;
import com.iai.proteus.map.SensorOfferingPlacemark;
import com.iai.proteus.map.SensorOfferingRegion;
import com.iai.proteus.map.WorldWindUtils;
import com.iai.proteus.map.wms.MapAVKey;
import com.iai.proteus.map.wms.WmsCache;
import com.iai.proteus.map.wms.WmsLayerInfo;
import com.iai.proteus.map.wms.WmsUtil;
import com.iai.proteus.model.MapId;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.map.WmsMapLayer;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.workspace.Query;
import com.iai.proteus.model.workspace.QuerySos;
import com.iai.proteus.model.workspace.QueryWmsMap;
import com.iai.proteus.queryset.Facet;
import com.iai.proteus.queryset.FacetChangeToggle;
import com.iai.proteus.queryset.QuerySetEvent;
import com.iai.proteus.queryset.QuerySetEventListener;
import com.iai.proteus.queryset.QuerySetEventNotifier;
import com.iai.proteus.queryset.QuerySetEventType;
import com.iai.proteus.queryset.SosOfferingLayer;
import com.iai.proteus.queryset.ui.SensorOfferingItem;
import com.iai.proteus.util.ProteusUtil;
import com.iai.proteus.views.layers.TimedPointPlacemarkLayer;

/**
 * A World Wind View making use of the "Albireo" plug-in to embed World Wind
 *
 * @author Jakob Henriksson
 *
 */
public class WorldWindView extends ViewPart
	implements QuerySetEventListener, EventListener, SelectListener,
		ISelectionListener
{

	private static final Logger log = Logger.getLogger(WorldWindView.class);

	public static final String ID = "com.iai.proteus.view.WorldWindView";

	private SwingControl swingControl = null;

	private static WorldWindowGLCanvas world;
    private Globe roundGlobe;
    private FlatGlobe flatGlobe;

	/*
	 * Fixed Layers
	 */
	private DataPlotProvenanceLayer layerDataPlotProvenance;

	private RenderableLayer layerDataTraceMarkers;
	private RenderableLayer layerDataTracePolyline;
	
	private SectorSelector selector;

    // the ToolTipController can be used to show a tool tip over markers and such
    protected ToolTipController toolTipController;

    private RenderableLayer tooltipLayer;
    private GlobeAnnotation tooltipAnnotation;

    private GeoRssLayer geoRssLayer; 
    
	/*
	 * Actions
	 */

    static
    {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
//            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
//            		"World Wind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        }
        else if (Configuration.isWindowsOS())
        {
        	// prevents flashing during window resizing
            System.setProperty("sun.awt.noerasebackground", "true");
        }
    }


	/**
	 * Constructor
	 *
	 */
	public WorldWindView() {

//		Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
//		Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());

		world = new WorldWindowGLCanvas();

		Model model = (Model) WorldWind.createConfigurationComponent(
				AVKey.MODEL_CLASS_NAME);

		getWwd().setModel(model);
		
		this.flatGlobe = new EarthFlat();
        this.roundGlobe = getWwd().getModel().getGlobe();

		initializeSectorSelection();

//		layerSelection = new SelectionLayer(getWwd());
		// add layer
//		getWwd().getModel().getLayers().add(layerSelection);

		layerDataPlotProvenance = new DataPlotProvenanceLayer(getWwd());
		// add layer
		getWwd().getModel().getLayers().add(layerDataPlotProvenance);

		layerDataTraceMarkers = new TimedPointPlacemarkLayer();
		layerDataTraceMarkers.setEnabled(false);

		layerDataTracePolyline = new RenderableLayer();
		layerDataTracePolyline.setName("Data trace lines");
		layerDataTracePolyline.setPickEnabled(false);

		getWwd().getModel().getLayers().add(layerDataTraceMarkers);
		getWwd().getModel().getLayers().add(layerDataTracePolyline);

		this.toolTipController =
				new ToolTipController(this.getWwd(), AVKey.DISPLAY_NAME, null);

		getWwd().addSelectListener(toolTipController);

		tooltipAnnotation =
			new GlobeAnnotation("", Position.fromDegrees(0, 0, 0));
		tooltipAnnotation.getAttributes().setSize(new Dimension(270, 0));
		tooltipAnnotation.getAttributes().setDistanceMinScale(1);
		tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
		tooltipAnnotation.getAttributes().setVisible(false);
		tooltipAnnotation.setAlwaysOnTop(true);

		tooltipLayer = new RenderableLayer();
		tooltipLayer.addRenderable(tooltipAnnotation);
		getWwd().getModel().getLayers().add(tooltipLayer);
		
		geoRssLayer = new GeoRssLayer(getWwd());
		getWwd().getModel().getLayers().add(geoRssLayer);
	}

	/**
	 * Create the control 
	 * 
	 */
	@Override
	public void createPartControl(final Composite parent) {

		swingControl = new SwingControl(parent, SWT.NONE) {
			@Override
			public Composite getLayoutAncestor() {
				return parent;
			}

			@Override
			protected JComponent createSwingComponent() {

				final JPanel panel = new JPanel(new BorderLayout());
				panel.add(world, BorderLayout.CENTER);
				return panel;
			}
		};

		/* add select listener that responds to clicks */
		getWwd().getInputHandler().addSelectListener(this);

		/*
		 * Listen to events
		 */
		EventNotifier.getInstance().addListener(this);
		QuerySetEventNotifier.getInstance().addListener(this);

		// add a listener to selections in @{link DiscoverView} 
		getSite().getPage().addSelectionListener(DiscoverView.ID, this);
		
		// add a listener to selections in @{link CommunityGroupView} 
		getSite().getPage().addSelectionListener(CommunityGroupView.ID, this);
	}


	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars()
//				.getMenuManager();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (swingControl != null) {
			swingControl.setFocus();
		}
	}

	@Override
	public void dispose() {
		if (swingControl != null) {
			swingControl.dispose();
		}
		super.dispose();
	}

	private WorldWindowGLCanvas getWwd() {
		return world;
	}

	/**
	 * Implements SelectListener
	 *
	 * @param event
	 */
	@Override
	public void selected(SelectEvent event) {

		if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {

			// get the picked object
			PickedObject picked = event.getTopPickedObject();

			if (picked != null) {

				Object pickedObj = picked.getObject();

				/*
				 * we only handle sensor markers, if something else
				 * is clicked, return
				 */
				if (!(pickedObj instanceof SensorOfferingMarker)) {
					return;
				}

				// convert to our marker object
				SensorOfferingMarker marker =
						((SensorOfferingMarker) picked.getObject());

				// populate selection object
				final MarkerSelection selection = new MarkerSelection();
				selection.add(marker);

				// notify listeners of selection
				SelectionNotifier.getInstance().selectionChanged(selection);

			}
		}
	}

	/**
	 * Returns the layer with the given map ID, null if it does not exist
	 *
	 * @param mapId
	 * @return
	 */
	private Layer getLayer(MapId mapId) {
		LayerList allLayers = getWwd().getModel().getLayers();
		Layer layer = allLayers.getLayerByName(mapId.toString());
		if (layer != null)
			return layer;
		log.warn("Could not find the layer with map id: " + mapId);
		return null;
	}

	/**
	 * Returns the layers corresponding to the MapLayer model object,
	 * an empty list of none was found
	 *
	 * @param mapLayer
	 * @return
	 */
	private List<Layer> getLayers(MapLayer mapLayer) {
		List<Layer> foundLayers = new ArrayList<Layer>();
		LayerList allLayers = getWwd().getModel().getLayers();
		for (MapId mapId : mapLayer.getMapIds()) {
			Layer layer = allLayers.getLayerByName(mapId.toString());
			if (layer != null)
				// only add the layer if it did not already exist
				if (!foundLayers.contains(layer))
					foundLayers.add(layer);
			else
				log.warn("No layer found with ID: " + mapId);
		}
		return foundLayers;
	}

	/**
	 * Updates the colors of the renderables in the layer
	 *
	 * @param mapLayer
	 */
	private void updateColorOfLayer(MapLayer mapLayer) {
		for (Layer layer : getLayers(mapLayer)) {
			// we can only really change the color of sensor offering layers
			if (layer != null && layer instanceof SosOfferingLayer) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				for (Renderable r : offeringLayer.getRenderables()) {
					Material material = new Material(mapLayer.getColor());
					if (r instanceof SensorOfferingPlacemark) {
						SensorOfferingPlacemark sop = (SensorOfferingPlacemark) r;
						sop.getAttributes().setLineMaterial(material);
					} else if (r instanceof SensorOfferingRegion) {
						SensorOfferingRegion sor = (SensorOfferingRegion) r;
						sor.getAttributes().setOutlineMaterial(material);
					}
				}
			}
		}
		getWwd().redrawNow();
	}


	/**
	 * Deletes layers with the given MapIds, if they exist
	 *
	 * @param mapId
	 */
	private void deleteLayers(Collection<MapId> mapIds) {
		for (MapId mapId : mapIds) {
			deleteLayer(mapId);
		}
	}

	/**
	 * Deletes a layer with a given MapId, if it exists
	 *
	 * @param mapId
	 */
	private void deleteLayer(MapId mapId) {
		Layer layer = getLayer(mapId);
		if (layer != null) {
			if (layer instanceof SosOfferingLayer) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				// prepare to delete sensor offering layer
				offeringLayer.prepareLayerDelete();
			}
			// remove layer
			getWwd().getModel().getLayers().remove(layer);
		}
	}

	private void createSosLayer(final MapId mapId, final Service service) {
		createSosLayer(mapId, new ArrayList<Service>() {
			private static final long serialVersionUID = 1L;
			{
				add(service);
			}
		});
	}

	private void createOrUpdateSosLayer(final MapId mapId, final List<Service> services) {

		Job job = new Job("Downloading Capabilities documents...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {

					monitor.beginTask("Downloading Capabilities documents...",
							services.size());

					List<Renderable> allMarkers = new ArrayList<Renderable>();
					for (Service service : services) {

						SosCapabilities capabilities =
								SosUtil.getCapabilities(service.getServiceUrl());
						List<Renderable> markers =
								WorldWindUtils.getCapabilitiesMarkers(capabilities,
										service.getColor());
						allMarkers.addAll(markers);

						monitor.worked(1);

						// TODO: properly handle cancel operation
						if (monitor.isCanceled())
							break;
					}

					// create layer with markers using the default map ID
					createOrUpdateSosLayerWithMarkers(mapId, allMarkers);

				} finally {
					monitor.done();
				}

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private void createSosLayer(final MapId mapId, List<Service> services) {

//		SosCapabilitiesCache cache = SosCapabilitiesCache.getInstance();

		List<Renderable> allMarkers = new ArrayList<Renderable>();

		for (final Service service : services) {

			// NOTE: we assume that the services have cached capabilities
			//       documents

			SosCapabilities capabilities =
					SosUtil.getCapabilities(service.getServiceUrl());
			List<Renderable> markers =
					WorldWindUtils.getCapabilitiesMarkers(capabilities,
							service.getColor());

			allMarkers.addAll(markers);
		}

		// create layer with markers using the default map ID
		createSosLayerWithMarkers(mapId, allMarkers);
	}


	/**
	 * Creates a layer from a list of queries. All queries are assumed
	 * to be of the same type
	 *
	 * @param mapLayer
	 * @param mapId
	 * @param queries
	 */
	private void createLayerFromQueries(MapLayer mapLayer, MapId mapId,
			List<Query> queries)
	{

		if (queries.size() > 0) {
			Query query = queries.get(0);

			/*
			 * Since we assume all queries are of the same type we
			 * do a type check based on the first element, if there is one
			 */
			if (query instanceof QuerySos) {

				// collect the renderables for this layer
				List<Renderable> markers =
						WorldWindUtils.getOfferingMarkers(mapLayer,
								queries);

				// create layer with markers
				createLayerWithMarkers(mapLayer, mapId, markers);

			} else if (query instanceof QueryWmsMap) {

				QueryWmsMap mapQuery = (QueryWmsMap) query;

				/*
				 * Assumption: each WMS map query has a unique
				 *             map Id (hence, layer)
				 */

				Service service = mapQuery.getProvenance().getService();

				WmsLayerInfo layerInfo =
						WmsUtil.getLayer(service.getServiceUrl(),
								mapQuery.getWmsLayerName());

				// try and get the WMS layer from the source
				Object wmsLayer = WmsUtil.getWMSLayer(layerInfo);

				if (wmsLayer != null && wmsLayer instanceof Layer) {
					log.info("A layer was returned from WMS");
					Layer newLayer = (Layer) wmsLayer;
					newLayer.setName(mapId.toString());
					newLayer.setEnabled(true);
					// add layer
					getWwd().getModel().getLayers().add(newLayer);
				}

			}
		} else {
			log.warn("A qurey layer did not contain any queries, " +
					"this was not expected.");
		}

	}

	/**
	 * Initialize a #{link SosOfferingLayer}
	 *
	 * @param mapId
	 */
	private void initializeSosLayer(MapId mapId) {
		SosOfferingLayer offeringLayer =
				new SosOfferingLayer(getWwd(),
						createSectorSelector(), mapId);
		// add marker layer to World Wind
		getWwd().getModel().getLayers().add(offeringLayer);
	}

	/**
	 * Creates a layer with the given markers
	 *
	 * @param mapId
	 * @param markers
	 */
	private void createOrUpdateSosLayerWithMarkers(final MapId mapId, final List<Renderable> markers)
	{
		/*
		 * Create the layer if there are markers to add to it
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {

				Layer layer = getLayer(mapId);

				if (layer != null && layer instanceof SosOfferingLayer) {

					// update existing layer
					SosOfferingLayer offeringLayer =
							(SosOfferingLayer) layer;
					offeringLayer.resetRenderables(markers);
					offeringLayer.setEnabled(true);

				} else {

					// create new layer

					if (markers.size() > 0) {

						// the name of the layer is set automatically
						SosOfferingLayer offeringLayer =
								new SosOfferingLayer(getWwd(),
										createSectorSelector(), mapId);
						offeringLayer.setRenderables(markers);
						offeringLayer.setEnabled(true);

						// add marker layer to World Wind
						getWwd().getModel().getLayers().add(offeringLayer);

					} else {
						log.warn("Tried to create a layer, but there were no markers.");
					}
				}


				// the name of the layer is set automatically
				//					SosOfferingLayer offeringLayer =
				//							new SosOfferingLayer(getWwd(), selector, mapId);
				//					offeringLayer.setRenderables(markers);
				//					offeringLayer.setEnabled(true);

				/* add marker layer to World Wind */
				//					layers.add(offeringLayer);
			}
		}).start();
	}

	/**
	 * Creates a layer with the given markers
	 *
	 * @param mapId
	 * @param markers
	 */
	private void createSosLayerWithMarkers(final MapId mapId, final List<Renderable> markers)
	{
		/*
		 * Create the layer if there are markers to add to it
		 */
		if (markers.size() > 0) {

			new Thread(new Runnable() {
				@Override
				public void run() {

					LayerList layers = getWwd().getModel().getLayers();

					// the name of the layer is set automatically
					SosOfferingLayer offeringLayer =
							new SosOfferingLayer(getWwd(), selector, mapId);
					offeringLayer.setRenderables(markers);
					offeringLayer.setEnabled(true);

					/* add marker layer to World Wind */
					layers.add(offeringLayer);
				}
			}).start();

		} else {
			log.warn("Tried to create a layer, but there were no markers.");
		}
	}

	/**
	 * Creates a layer with the given markers
	 *
	 * @param mapLayer
	 * @param mapId
	 * @param markers
	 */
	private void createLayerWithMarkers(final MapLayer mapLayer,
			final MapId mapId, final List<Renderable> markers)
	{
		/*
		 * Create the layer if there are markers to add to it
		 */
		if (markers.size() > 0) {

			new Thread(new Runnable() {
				@Override
				public void run() {

					LayerList layers = getWwd().getModel().getLayers();

					// the name of the layer is set automatically
					SosOfferingLayer offeringLayer =
							new SosOfferingLayer(getWwd(), selector, mapId);
					offeringLayer.setRenderables(markers);
					offeringLayer.setEnabled(mapLayer.isActive());

					/* add marker layer to World Wind */
					layers.add(offeringLayer);
				}
			}).start();

		} else {
			log.warn("Tried to create a layer, but there were no markers.");
		}
	}


	/**
	 * Initializes the selection of a sector
	 *
	 */
	private void initializeSectorSelection() {

		this.selector = new SectorSelector(getWwd());
		this.selector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
		this.selector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
		this.selector.setBorderWidth(3);

	}

	/**
	 * Creates and returns a SectorSelector object for constraining areas
	 *
	 * @return
	 */
	private SectorSelector createSectorSelector() {
		SectorSelector selector = new SectorSelector(getWwd());
		selector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
		selector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
		selector.setBorderWidth(3);
		return selector;
	}

	/**
	 * Handles events
	 *
	 * @param event
	 */
	@Override
	public void querySetEventHandler(QuerySetEvent event) {

		Object obj = event.getEventObject();
		QuerySetEventType type = event.getEventType();
		Object value = event.getValue();

		switch (type) {
		
		case QUERYSET_INITIALIZE_LAYER:
			/*
			 * Initialize layer
			 */
			if (value instanceof MapId) {
				initializeSosLayer((MapId) value);
			}

			break;

		case QUERYSET_SERVICE_TOGGLE:
			/*
			 * Toggle layer
			 */
			if (obj instanceof List<?> && value instanceof MapId) {

				setServices((MapId) value, (ArrayList<?>) obj);
			}

			break;

		case QUERYSET_LAYERS_DELETE:
			/*
			 * Delete layers
			 */
			if (value instanceof Collection<?>) {
				deleteLayers((Collection<MapId>)value);
			}

			break;

		case QUERYSET_LAYERS_ACTIVATE:
			/*
			 * Show a specific set of layers
			 */
			if (value instanceof Collection<?>) {

				activateLayers((Collection<MapId>) value);
			}

			break;

		case QUERYSET_REGION_ENABLED:
			/*
			 * Enables geographical area selection
			 */
			if (value instanceof MapId) {

				enableRegionSelection((MapId) value);
			}

			break;

		case QUERYSET_REGION_DISABLE:
			/*
			 * Disables geographical area selection
			 */
			if (value instanceof MapId) {

				disableRegionSelection((MapId) value);
			}

			break;

		case QUERYSET_FACET_CHANGE:

			if (obj instanceof MapId) {

				if (value instanceof List) {

					// notify all layers of the facet changes
					notifyLayerOfFacetSelection((MapId) obj, (List<?>) value);

				} else if (value instanceof FacetChangeToggle) {

					// notify all layers of the facet change
					notifyLayerOfFacetSelection((MapId) obj,
							(FacetChangeToggle) value);
				}
			}

			break;

		case QUERYSET_FACET_CLEAR:

			if (value instanceof Facet) {
				// notify all layers of the facet changes
				notifyLayerOfFacetClearing((MapId) obj, (Facet) value);
			}

			break;

		case QUERYSET_FLY_TO_OFFERING:

			if (value instanceof LatLon) {
				LatLon ll = (LatLon)value;
				/*
				 * Fly to location
				 */
				PointOfInterest point =
						WorldWindUtils.getPointOfInterest(ll.getLat(),
								ll.getLon());

				WorldWindUtils.moveToLocation(getWwd().getView(), point);
			}

			break;
			
		case QUERYSET_MAP_TOGGLE_LAYER:

			if (obj instanceof WmsMapLayer) {
				WmsMapLayer mapLayer = (WmsMapLayer) obj;

				/*
				 * Check if it exists
				 */
				Layer layer = getLayer(mapLayer.getDefaultMapId());
				if (layer != null) {
					// if it does, toggle it and we're done
					layer.setEnabled(mapLayer.isActive());
					break;
				}

				/*
				 * If it did not, try and get it
				 */
				Object wmsLayer = getWMSLayer(mapLayer);
				
				if (wmsLayer != null && wmsLayer instanceof Layer) {
					log.trace("A layer was returned from WMS: " + wmsLayer);
					Layer newLayer = (Layer) wmsLayer;
					newLayer.setName(mapLayer.getDefaultMapId().toString());
					newLayer.setEnabled(true);
					// add attributes
					newLayer.setValue(MapAVKey.WMS_SERVICE, 
							mapLayer.getServiceEndpoint());
					// add layer
					getWwd().getModel().getLayers().add(newLayer);
				} else {
					log.warn("Return object was not a Layer object");
				}
				
			}
			
			break;
			
		case QUERYSET_MAP_REMOVE_LAYERS_FROM_SERVICE:
			
			/*
			 * Removes layers from a WMS service that is no longer 
			 * active. When the service is activated, new layers
			 * will be created 
			 */
			if (value instanceof String) {
				int count = 0;
				LayerList layerList = getWwd().getModel().getLayers();
				for (Layer layer : layerList) {
					Object keyValue = layer.getValue(MapAVKey.WMS_SERVICE);
					if (keyValue != null && keyValue instanceof String) {
						if (keyValue.equals(value)) {
							layerList.remove(layer);
							log.trace("Removed layer " + layer.getName());
							count++;
						}
					}
				}
				log.trace("Removed " + count + " layers related to: " + 
						value);
			}
			
			break;
		}
	}

	/**
	 * Toggles the services on or off for the given map (query set)
	 *
	 * @param mapId
	 * @param serviceObjects
	 */
	private void setServices(MapId mapId, List<?> serviceObjects) {
		List<Service> services = new ArrayList<Service>();
		for (Object obj : serviceObjects) {
			if (obj instanceof Service) {
				services.add((Service) obj);
			}
		}
		// create the layer for the first time
		createOrUpdateSosLayer(mapId, services);
	}

	/**
	 * Toggles the services on or off for the given map (query set)
	 *
	 * @param map
	 * @param service
	 */
	private void toggleService(MapIdentifier map, Service service) {
		Layer layer = getLayer(map.getDefaultMapId());
		if (layer != null) {
			if (layer instanceof SosOfferingLayer) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				if (service.isActive()) {
					// add the service
//					offeringLayer.
//					service.get

				} else {
					// remove the service
				}
			} else {
				log.error("The layer we are modifying is not an SosOfferingLayer.");
			}
		} else {
			// create the layer for the first time
			createSosLayer(map.getDefaultMapId(), service);
		}
	}

	/**
	 * Activate the layers with the matching IDs, hide other layers
	 *
	 * @param mapIds
	 */
	private void activateLayers(Collection<MapId> mapIds) {

		// hide other layers
		for (Layer layer : getWwd().getModel().getLayers()) {
			if (layer instanceof SosOfferingLayer) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				if (mapIds.contains(offeringLayer.getMapId())) {
					// show layers with given IDs
					offeringLayer.setEnabled(true);
					offeringLayer.showSector();
				} else {
					// hide other layers
					offeringLayer.setEnabled(false);
					offeringLayer.hideSector();
				}
			}
		}
	}

	/**
	 * Enables geographical region selection in a @{link SosOfferingLayer}.
	 *
	 * @param mapId
	 */
	private void enableRegionSelection(MapId mapId) {
		Layer layer = getLayer(mapId);
		if (layer != null) {
			if (layer instanceof SosOfferingLayer) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				offeringLayer.enableSector();
			}
		}
	}

	/**
	 * Disables geographical region selection in a @{link SosOfferingLayer}.
	 *
	 * @param mapId
	 */
	private void disableRegionSelection(MapId mapId) {
		Layer layer = getLayer(mapId);
		if (layer != null) {
			if (layer instanceof SosOfferingLayer) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				offeringLayer.disableSector();
				offeringLayer.clearSector();
			}
		}
	}

//
//	/**
//	 * Handles events
//	 *
//	 * @param event
//	 */
//	@Override
//	public void workspaceModelUpdate(WorkspaceEvent event) {
//
//		Object obj = event.getEventObject();
//		final Object value = event.getValue();
//		WorkspaceEventType type = event.getEventType();
//
//		switch (type) {
//			case WORKSPACE_LAYER_TOGGLE:
//				/*
//				 * Toggle layer
//				 */
//				if (obj instanceof MapLayer)
//					toggleLayer((MapLayer) obj);
//
//				break;
//
//			case WORKSPACE_LAYER_DELETE:
//				/*
//				 * Delete layer
//				 */
//				if (obj instanceof MapLayer) {
//					deleteLayers((MapLayer) obj);
//				}
//
//				break;
//
//			case WORKSPACE_LAYER_RECREATE:
//				/*
//				 * Re-create layer
//				 */
//				if (obj instanceof MapLayer) {
//					recreateMapLayer((MapLayer)obj);
//				}
//
//				break;
//
//			case WORKSPACE_LAYER_COLOR_CHANGE:
//				/*
//				 * Update layer
//				 */
//				if (obj instanceof MapLayer)
//					updateColorOfLayer((MapLayer) obj);
//
//				break;
//
//			case WORKSPACE_MODEL_UPDATED:
//
//				/*
//				 * Recreate layer reflecting changes to query layer
//				 */
//				if (obj instanceof QueryLayer) {
//
//					// if we are based a Query as the value of the event
//					// make use of it to optimize the re-creation process
//					if (value != null && value instanceof Query) {
//						Query query = (Query) value;
//
//						recreateMapLayer((QueryLayer) obj, query);
//
//					} else {
//
//						recreateMapLayer((QueryLayer) obj);
//					}
//				}
//
//				break;
//
//			case WORKSPACE_FLY_TO_OFFERING:
//				if (value instanceof LatLon) {
//					LatLon ll = (LatLon)value;
//					/*
//					 * Fly to location
//					 */
//					PointOfInterest point =
//						WorldWindUtils.getPointOfInterest(ll.getLat(),
//								ll.getLon());
//
//					WorldWindUtils.moveToLocation(getWwd().getView(), point);
//				}
//				break;
//
//			case WORKSPACE_WMS_TOGGLE:
//
//				if (obj instanceof WmsMapLayer) {
//					WmsMapLayer mapLayer = (WmsMapLayer) obj;
//
//					/*
//					 * Check if it exists
//					 */
//					Layer layer = getLayer(mapLayer.getDefaultMapId());
//					if (layer != null) {
//						// if it does, toggle it and we're done
//						layer.setEnabled(mapLayer.isActive());
//						break;
//					}
//
//					/*
//					 * If it did not, try and get it
//					 */
//					Object wmsLayer = getWMSLayer(mapLayer);
//
//					if (wmsLayer != null && wmsLayer instanceof Layer) {
//						log.info("A layer was returned from WMS");
//						Layer newLayer = (Layer) wmsLayer;
//						newLayer.setName(mapLayer.getDefaultMapId().toString());
//						newLayer.setEnabled(true);
//						// add layer
//						getWwd().getModel().getLayers().add(newLayer);
//					}
//				}
//
//
//
//				if (obj instanceof LayerInfo) {
//					LayerInfo layerInfo = (LayerInfo) obj;
//					LayerList layers = getWwd().getModel().getLayers();
//
//					String name = layerInfo.getName();
//
//					if ((Boolean) value) {
//						// show layer
//						Layer layer = layers.getLayerByName(name);
//						if (layer != null) {
//							// show layer if it already was there
//							layer.setEnabled(true);
//						} else {
//							// we need to get the layer
//							Object wmsLayer = WMSUtil.getWMSLayer(layerInfo);
//							if (wmsLayer instanceof Layer) {
//								log.info("A layer was returned from WMS");
//								layer = (Layer) wmsLayer;
//								layer.setName(name);
//								layer.setEnabled(true);
//								layers.add(layer);
//							} else if (wmsLayer instanceof ElevationModel) {
//								log.info("An elevation model was returned from WMS");
//								ElevationModel model = (ElevationModel) wmsLayer;
//								model.setName(name);
//								CompoundElevationModel compoundModel =
//									(CompoundElevationModel) getWwd().getModel().getGlobe().getElevationModel();
//								if (!compoundModel.getElevationModels().contains(model))
//									compoundModel.addElevationModel(model);
//							} else {
//								log.info("A layer was not returned from WMS, " +
//										"but a different object: " +  wmsLayer);
//							}
//						}
//					} else {
//						// hide layer
//						Layer layer = layers.getLayerByName(name);
//						if (layer != null) {
//							layer.setEnabled(false);
//						} else {
//							// try elevation models...
//							CompoundElevationModel compoundModel =
//									(CompoundElevationModel) getWwd().getModel().getGlobe().getElevationModel();
//							ElevationModel ourModel = null;
//							for (ElevationModel model : compoundModel.getElevationModels()) {
//								if (model.getName().equals(name)) {
//									ourModel = model;
//									break;
//								}
//							}
//							if (ourModel != null) {
//								compoundModel.removeElevationModel(ourModel);
//							} else {
//								log.info("Layer should be hidden, but was not found");
//							}
//						}
//					}
//
//				}
//
//				break;
//		}
//	}

	/**
	 * Handles events
	 *
	 * @param event
	 */
	@Override
	public void update(Event event) {

		final Object value = event.getValue();
		EventType type = event.getEventType();

		switch (type) {

		case MAP_TOGGLE_LAYER:

			if (value instanceof String) {
				String layerName = (String) value;
				// the value can have a list of layer names
				String[] layers = layerName.split(",");
				for (String name : layers) {
					// toggle layer
					toggleMapBaseLayers(name.trim());
				}
			}

			break;
			
		case MAP_TOGGLE_GLOBE_TYPE:
			if (value instanceof String) {
				String globeType = (String) value;
				if (globeType.equalsIgnoreCase("flat")) {
					// enable flat globe
//					enableFlatGlobe(false);
					// show GeoRSS layer
					geoRssLayer.setEnabled(true);
				} else {
					// disable flat globe
//					enableFlatGlobe(false);
					// hide GeoRSS layer
					geoRssLayer.setEnabled(false);
				}
			}
			

		}
	}

	/**
	 * Listening to selection changes in the workspace view
	 *
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		String partId = part.getSite().getId();
		
		/*
		 * For @{link DiscoverView}  
		 */
		if (partId.equals(DiscoverView.ID)) {

			List<SensorOffering> sensorOfferings = new ArrayList<SensorOffering>();

			if (!selection.isEmpty()) {
				if (selection instanceof StructuredSelection) {
					StructuredSelection structuredSelection =
							(StructuredSelection) selection;

					Iterator<?> iterator = structuredSelection.iterator();

					while (iterator.hasNext()) {
						Object element = iterator.next();
						
						if (element instanceof SensorOfferingItem) {

							SensorOffering sensorOffering =
									((SensorOfferingItem) element).getSensorOffering();
							sensorOfferings.add(sensorOffering);

						} 
					}

					// show the selection
					showSelectionInOfferingLayer(sensorOfferings);
				}
			}
		} 
		/*
		 * For @{link CommunityGroupView}
		 */
		else if (partId.equals(CommunityGroupView.ID)) {
			
			if (!selection.isEmpty()) {
				if (selection instanceof StructuredSelection) {
					StructuredSelection structuredSelection =
							(StructuredSelection) selection;
					Object element = structuredSelection.getFirstElement();
					if (element instanceof Group) {
						Group group = (Group) element;

						// get service address from preference store
						IPreferenceStore store =
								Activator.getDefault().getPreferenceStore();						

						String feed = 
								ProteusUtil.getAlertFeed(store, group.getId());

						geoRssLayer.useFeed(feed);	
					}
				}
			}
		}
	}

	/**
	 * Informs the active layers that the list of sensor offerings should
	 * be highlighted
	 *
	 * @param sensorOfferings
	 */
	private void showSelectionInOfferingLayer(List<SensorOffering> sensorOfferings) {
		for (Layer layer : getWwd().getModel().getLayers()) {
			if (layer instanceof SosOfferingLayer && layer.isEnabled()) {
				SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
				offeringLayer.showSelection(sensorOfferings);
			}
		}
	}

	/**
	 * TODO: to write 
	 *
	 * @param mapLayer
	 * @return
	 */
	private Object getWMSLayer(WmsMapLayer mapLayer) {

		WmsCache cache = WmsCache.getInstance();
		
		String serviceEndpoint = mapLayer.getServiceEndpoint();

		if (cache.containsLayers(serviceEndpoint)) {
			
			System.out.println("OK, container the layers");
			
			Collection<WmsLayerInfo> layerInfos =
					cache.getLayers(serviceEndpoint);
			
			System.out.println("Found " + layerInfos.size() + " layers");
			
			for (WmsLayerInfo layerInfo : layerInfos) {
				
				System.out.println("Comparing: " + layerInfo.getName() + 
						" AND " + mapLayer.getName());
				
				System.out.println("TITLE: " + layerInfo.getTitle());
				
				if (layerInfo.getName().equals(mapLayer.getName())) {
					return WmsUtil.getWMSLayer(layerInfo);
				}
			}
			
		} else {

			// TODO: implement 
			log.debug("Should try and fetch the layers...");
		}
		
		log.warn("Something went wrong when getting a WMS layer");
		return null;
	}

    /**
     * Notifies layers that the sector has been cleared
     *
     */
    private void notifyLayersOfClearedRestrictions() {
    	/*
    	 * Notify layers of the change so they can update themselves
    	 */
    	for (Layer layer : getWwd().getModel().getLayers()) {
    		if (layer instanceof SosOfferingLayer) {
    			SosOfferingLayer offeringLayer =
    					(SosOfferingLayer) layer;
    			if (offeringLayer.isEnabled()) {
    				offeringLayer.clearSector();
    				offeringLayer.clearFacets();
    			}
    		}
    	}
    }

    /**
     * Notify layer to clear the facet type
     *
     * @param mapId
     * @param facetType
     */
    private void notifyLayerOfFacetClearing(MapId mapId, Facet facetType) {

    	/*
    	 * Notify layer of the change so it can update
    	 */
    	Layer layer = getLayer(mapId);
    	if (layer != null && layer instanceof SosOfferingLayer) {
    		SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
    		offeringLayer.clearFacetConstraints(facetType);
    	}
    }

    /**
     * Notifies the appropriate layers of the change in facet selection
     *
     * @param mapId
     * @param facet
     */
    private void notifyLayerOfFacetSelection(MapId mapId,
    		FacetChangeToggle facet)
    {
    	List<FacetChangeToggle> changes = new ArrayList<FacetChangeToggle>();
    	changes.add(facet);

    	notifyLayerOfFacetChanges(mapId, changes);
    }


    /**
     * Notifies the appropriate layers of the change in facet selection
     *
     * @param mapId
     * @param facets
     */
    private void notifyLayerOfFacetSelection(MapId mapId, List<?> facets) {

    	/*
    	 * Make sure we are dealing with objects of the right type
    	 */
    	List<FacetChangeToggle> changes = new ArrayList<FacetChangeToggle>();
    	for (Object obj : facets) {
    		if (obj instanceof FacetChangeToggle) {
    			changes.add((FacetChangeToggle) obj);
    		}
    	}

    	notifyLayerOfFacetChanges(mapId, changes);
    }

    /**
     * Notifies the appropriate layers of the change in facet selection
     *
     * @param mapId
     * @param facets
     */
    private void notifyLayerOfFacetChanges(MapId mapId,
    		List<FacetChangeToggle> facets)
    {
    	/*
    	 * Notify layer of the change so it can update
    	 */
    	Layer layer = getLayer(mapId);
    	if (layer != null && layer instanceof SosOfferingLayer) {
    		SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
    		offeringLayer.addOrRemoveFacetConstraints(facets);
    	}
    }

    /**
     * Returns a collection of renderables from SosOfferingLayers that
     * are enabled
     *
     * @return
     */
    private Collection<Renderable> getSelectedOfferingMarkers() {

    	Collection<Renderable> markers =
    			new ArrayList<Renderable>();

//    	if (pickedMarkers.size() > 0) {
//    		for (SensorOfferingMarker marker : pickedMarkers) {
//    			markers.add(marker);
//    		}
//    		return markers;
//    	}

    	for (Layer layer : getWwd().getModel().getLayers()) {
    		if (layer instanceof SosOfferingLayer) {
    			SosOfferingLayer sosLayer = (SosOfferingLayer) layer;
    			if (sosLayer.isEnabled()) {
    				int count = 0;
    				for (Renderable renderable : sosLayer.getRenderables()) {
    					if (renderable instanceof SensorOfferingMarker) {
    						markers.add(renderable);
    						count++;
    					}
    				}
    				log.trace("Layer: " +
    						sosLayer.getName() + " contributed " +
    						count + " markers");
    			}
    		}
    	}
    	return markers;
    }

    /**
     * Toggle visibility of a base layer map
     *
     * @param layerName
     */
    private void toggleMapBaseLayers(String layerName) {
    	for (Layer layer : getWwd().getModel().getLayers()) {
    		if (layer.getName().equalsIgnoreCase(layerName))
    			layer.setEnabled(!layer.isEnabled());
    	}
    }
    
    /**
     * Returns true if the globe is flat
     * 
     * @return
     */
    public boolean isFlatGlobe() {
        return getWwd().getModel().getGlobe() instanceof FlatGlobe;
    }
    
    /**
     * Enables or disables a flat globe
     * 
     * (from NASA's @{link FlatWorldPanel})
     * 
     * @param flat
     */
    private void enableFlatGlobe(boolean flat) {

        if (isFlatGlobe() == flat)
            return;

        if (!flat) {
            // Switch to round globe
            getWwd().getModel().setGlobe(roundGlobe);
            // Switch to orbit view and update with current position
            FlatOrbitView flatOrbitView = (FlatOrbitView)getWwd().getView();
            BasicOrbitView orbitView = new BasicOrbitView();
            orbitView.setCenterPosition(flatOrbitView.getCenterPosition());
            orbitView.setZoom(flatOrbitView.getZoom( ));
            orbitView.setHeading(flatOrbitView.getHeading());
            orbitView.setPitch(flatOrbitView.getPitch());
            getWwd().setView(orbitView);
            // Change sky layer
            LayerList layers = getWwd().getModel().getLayers();
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) instanceof SkyColorLayer)
                    layers.set(i, new SkyGradientLayer());
            }
        }
        else
        {
            // Switch to flat globe
            getWwd().getModel().setGlobe(flatGlobe);
            flatGlobe.setProjection(FlatGlobe.PROJECTION_MERCATOR);
            // Switch to flat view and update with current position
            BasicOrbitView orbitView = (BasicOrbitView)getWwd().getView();
            FlatOrbitView flatOrbitView = new FlatOrbitView();
            flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
            flatOrbitView.setZoom(orbitView.getZoom( ));
            flatOrbitView.setHeading(orbitView.getHeading());
            flatOrbitView.setPitch(orbitView.getPitch());
            getWwd().setView(flatOrbitView);
            // Change sky layer
            LayerList layers = getWwd().getModel().getLayers();
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
        }
        
        getWwd().redraw();
    }    

}

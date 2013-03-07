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
import com.iai.proteus.model.SensorOfferingLayer;
import com.iai.proteus.model.map.IMapLayer;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.map.WmsMapLayer;
import com.iai.proteus.model.map.WmsSavedMap;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.queryset.Facet;
import com.iai.proteus.queryset.FacetChangeToggle;
import com.iai.proteus.queryset.QuerySetEvent;
import com.iai.proteus.queryset.QuerySetEventListener;
import com.iai.proteus.queryset.QuerySetEventNotifier;
import com.iai.proteus.queryset.QuerySetEventType;
import com.iai.proteus.queryset.SosOfferingLayer;
import com.iai.proteus.queryset.ui.SensorOfferingItem;
import com.iai.proteus.util.ProteusUtil;

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

		layerDataTracePolyline = new RenderableLayer();
		layerDataTracePolyline.setName("Data trace lines");
		layerDataTracePolyline.setPickEnabled(false);

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
	 * Returns the layer for the given @{link IMapLayer} 
	 * 
	 * @param mapLayer
	 * @return
	 */
	private Layer getLayer(IMapLayer mapLayer) {
		return getLayer(mapLayer.getMapId());
	}
	
	/**
	 * Returns the layer with the given map ID, null if it does not exist
	 *
	 * @param mapId
	 * @return
	 */
	private Layer getLayer(MapId mapId) {
		for (Layer layer : getWwd().getModel().getLayers()) {
			Object value = layer.getValue(MapAVKey.MAP_ID);
			if (value != null && value instanceof String) {
				String mapIdStr = (String) value;
				if (mapId.toString().equals(mapIdStr)) {
					return layer;
				}
			}
		}
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
	private Collection<Layer> getLayers(MapLayer mapLayer) {
		Collection<Layer> foundLayers = new ArrayList<Layer>();
		LayerList layers = getWwd().getModel().getLayers();
		for (Layer layer : layers) {
			Object value = layer.getValue(MapAVKey.MAP_ID);
			if (value != null && value instanceof String) {
				String mapIdStr = (String) value;
				if (mapLayer.getMapId().toString().equals(mapIdStr)) {
					// only add the layer if it did not already exist
					if (!foundLayers.contains(layer))
						foundLayers.add(layer);
				}
			}
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
	 * Deletes the given map layers (by @{link MapId}), if they exist
	 *
	 * @param mapLayers
	 */
	private void deleteLayers(Collection<IMapLayer> mapLayers) {
		for (IMapLayer mapLayer : mapLayers) {
			deleteLayer(mapLayer);
		}
	}

	/**
	 * Deletes the given map layer (by @{link MapId}), if it exists
	 *
	 * @param mapLayer
	 */
	private void deleteLayer(IMapLayer mapLayer) {
		Layer layer = getLayer(mapLayer);
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

	private void createOrUpdateSosLayer(final IMapLayer mapLayer, final List<Service> services) {

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
					createOrUpdateSosLayerWithMarkers(mapLayer, allMarkers);

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
	 * Initialize a #{link SosOfferingLayer}
	 *
	 * @param offeringLayer
	 */
	private void initializeSosLayer(SensorOfferingLayer offeringLayer) {
		// create layer 
		SosOfferingLayer layer =
				new SosOfferingLayer(getWwd(),
						createSectorSelector(), offeringLayer.getMapId());
		// associate ID with layer 
		layer.setValue(MapAVKey.MAP_ID, offeringLayer.getMapId().toString());
		// add marker layer to World Wind
		getWwd().getModel().getLayers().add(layer);
	}

	/**
	 * Creates a layer with the given markers
	 *
	 * @param mapLayer
	 * @param markers
	 */
	private void createOrUpdateSosLayerWithMarkers(final IMapLayer mapLayer, final List<Renderable> markers)
	{
		/*
		 * Create the layer if there are markers to add to it
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {

				Layer layer = getLayer(mapLayer);

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
										createSectorSelector(), mapLayer.getMapId());
						// associate ID with layer 
						offeringLayer.setValue(MapAVKey.MAP_ID, mapLayer.getMapId().toString());
						// associate id with 
						offeringLayer.setRenderables(markers);
						offeringLayer.setEnabled(true);

						// add marker layer to World Wind
						getWwd().getModel().getLayers().add(offeringLayer);

					} else {
						log.warn("Tried to create a layer, but there were no markers.");
					}
				}
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
					// associate ID with layer 
					offeringLayer.setValue(MapAVKey.MAP_ID, mapId.toString());
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
			if (value instanceof SensorOfferingLayer) {
				initializeSosLayer((SensorOfferingLayer) value);
			}

			break;

		case QUERYSET_SERVICE_TOGGLE:
			/*
			 * Toggle layer
			 */
			if (obj instanceof IMapLayer && value instanceof Collection<?>) {

				setServices((IMapLayer) obj, (ArrayList<?>) value);
			}

			break;

		case QUERYSET_LAYERS_DELETE:
			/*
			 * Delete layers
			 */
			if (value instanceof Collection<?>) {
				
				deleteLayers((Collection<IMapLayer>) value);
			}

			break;

		case QUERYSET_LAYERS_ACTIVATE:
			/*
			 * Show a specific set of layers
			 */
			if (value instanceof Collection<?>) {
				
				activateLayers((Collection<IMapLayer>) value);
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

				// check if the layer exists
				Layer layer = getLayer(mapLayer.getMapId());
				if (layer != null) {
					
					// toggle the layer  
					layer.setEnabled(mapLayer.isActive());
					
					// check if we should remove the 'preview' tag 
					if (obj instanceof WmsSavedMap)
						layer.removeKey(MapAVKey.WMS_MAP_PREVIEW);

					// we're done
					break;
				}
				
				/*
				 * If it did not, try and get it
				 */
				Object wmsLayer = getWmsLayer(mapLayer);
				
				if (wmsLayer != null && wmsLayer instanceof Layer) {
					log.trace("A layer was returned from WMS: " + wmsLayer);
					Layer newLayer = (Layer) wmsLayer;
					newLayer.setName(mapLayer.getMapId().toString());
					newLayer.setEnabled(true);

					// set attributes
					newLayer.setValue(MapAVKey.MAP_ID, mapLayer.getMapId().toString());
					newLayer.setValue(MapAVKey.WMS_SERVICE_URL, 
							mapLayer.getServiceEndpoint());
					if (!(obj instanceof WmsSavedMap))
						// indicates that the layer is a preview from a WMS
						// in contrast to a saved map 
						newLayer.setValue(MapAVKey.WMS_MAP_PREVIEW, true);
						
					// add layer
					getWwd().getModel().getLayers().add(newLayer);
				} else {
					log.warn("Return object was not a Layer object");
				}
				
			}
			
			break;			
			
		case QUERYSET_MAP_REMOVE_LAYERS_FROM_SERVICE:

			/*
			 * Remove all layers with associated 
			 */
			if (value == null) {
				
				LayerList layerList = getWwd().getModel().getLayers();
				for (Layer layer : layerList) {
					Object valueServiceUrl = layer.getValue(MapAVKey.WMS_SERVICE_URL);
					Object valuePreview = layer.getValue(MapAVKey.WMS_MAP_PREVIEW);
					// only remove the layer if it has an associated service
					// and has the @{link MapAVKey.WMS_MAP_PREVIEW} value set
					if (valueServiceUrl != null && valueServiceUrl instanceof String
							&& valuePreview != null && (Boolean)valuePreview) {
						layerList.remove(layer);
						log.trace("Removed layer " + layer.getName());
					}
				}
			}
			/*
			 * Removes layers from a WMS service that is no longer 
			 * active. When the service is activated, new layers
			 * will be created 
			 */
			else if (value instanceof String) {
				
				int count = 0;
				LayerList layerList = getWwd().getModel().getLayers();
				for (Layer layer : layerList) {
					Object valueServiceUrl = layer.getValue(MapAVKey.WMS_SERVICE_URL);
					Object valuePreview = layer.getValue(MapAVKey.WMS_MAP_PREVIEW);
					// only remove the layer if it has an associated service
					// and has the @{link MapAVKey.WMS_MAP_PREVIEW} value set
					if (valueServiceUrl != null && valueServiceUrl instanceof String
							&& valuePreview != null && (Boolean)valuePreview) {
						if (valueServiceUrl.equals(value)) {
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
	 * @param mapLayer
	 * @param serviceObjects
	 */
	private void setServices(IMapLayer mapLayer, List<?> serviceObjects) {
		List<Service> services = new ArrayList<Service>();
		for (Object obj : serviceObjects) {
			if (obj instanceof Service) {
				Service service = (Service) obj;
				// only include active services 
				if (service.isActive())
					services.add(service);
			}
		}
		// create the layer for the first time
		createOrUpdateSosLayer(mapLayer, services);
	}
	
	/**
	 * Activate the layers with the matching IDs, hide other layers
	 *
	 * @param mapLayers
	 */
	private void activateLayers(Collection<IMapLayer> mapLayers) {

		for (Layer layer : getWwd().getModel().getLayers()) {
			
			boolean found = false;
			
			Object obj = layer.getValue(MapAVKey.MAP_ID);
			if (obj != null && obj instanceof String) {
				String mapIdStr = (String) obj;

				for (IMapLayer mapLayer : mapLayers) {
					// we found a matching map layer
					if (mapLayer.getMapId().toString().equals(mapIdStr)) {

						// enable layers appropriately 
						if (layer instanceof SosOfferingLayer) {
							layer.setEnabled(true);
						} else {
							layer.setEnabled(mapLayer.isActive());
						}
						
						// indicate that we found the layer we were looking for
						found = true;
						break;
					}
				}
				
				// if the layer was not found, but did have a 
				// {@link MapAVKey.MAP_ID} value, disable the layer
				// because it is not part of our context
				if (!found) {
					layer.setEnabled(false);
				}
				
				// special handling of @{link SosOfferingLayer} 
				if (layer instanceof SosOfferingLayer) {
					SosOfferingLayer offeringLayer = (SosOfferingLayer) layer;
					if (found) {
						offeringLayer.showSector();
					} else {
						offeringLayer.hideSector();
					}					
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
	private Object getWmsLayer(WmsMapLayer mapLayer) {

		WmsCache cache = WmsCache.getInstance();
		
		String serviceEndpoint = mapLayer.getServiceEndpoint();

		if (cache.containsLayers(serviceEndpoint)) {
			
			Collection<WmsLayerInfo> layerInfos =
					cache.getLayers(serviceEndpoint);
			
			for (WmsLayerInfo layerInfo : layerInfos) {
				
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

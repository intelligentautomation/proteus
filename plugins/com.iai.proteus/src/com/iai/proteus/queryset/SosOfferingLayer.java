package com.iai.proteus.queryset;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.iai.proteus.common.TimeUtils;
import com.iai.proteus.common.event.EventNotifier;
import com.iai.proteus.common.event.EventType;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.map.SectorSelector;
import com.iai.proteus.map.SelectionLayer;
import com.iai.proteus.map.SensorOfferingMarker;
import com.iai.proteus.map.SensorOfferingPlacemark;
import com.iai.proteus.map.WorldWindUtils;
import com.iai.proteus.model.MapId;
import com.iai.proteus.queryset.ui.SensorOfferingItem;

/**
 * A map layer consisting of Sensor Observation Service (SOS) offerings
 *
 * @author Jakob Henriksson
 *
 */
public class SosOfferingLayer extends RenderableLayer
	implements PropertyChangeListener, QuerySetContributor,
		QuerySetMap
{

	private static final Logger log =
		Logger.getLogger(SosOfferingLayer.class);

	private WorldWindow world;
	private MapId mapId;

	// internal tools and layers
	private SectorSelector selector;
	private SelectionLayer layerSelection;

	private Sector latestUserSelection;

	private Iterable<Renderable> allRenderables;
	private boolean markersInitialized;

	// active facets that constrain what is being displayed
	private Set<FacetChangeToggle> facets;

	/**
	 * Constructor
	 *
	 * @param worldWindow
	 * @param selector
	 * @param mapId
	 */
	public SosOfferingLayer(WorldWindow worldWindow, SectorSelector selector,
			MapId mapId)
	{

		if (worldWindow == null) {
			String msg = "World window object was null";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		if (selector == null) {
			String msg = "Sector selector object was null";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		if (mapId == null) {
			String msg = "Map ID was null";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		this.world = worldWindow;
		this.selector = selector;
		this.mapId = mapId;

		// selection layer
		layerSelection = new SelectionLayer(getWwd());
		getWwd().getModel().getLayers().add(layerSelection);

		// defaults
		allRenderables = new ArrayList<Renderable>();
		markersInitialized = false;
		facets = new HashSet<FacetChangeToggle>();

		setName(mapId.toString());
	}

	/**
	 * Clears all facets of the given type
	 *
	 * @param facetType
	 */
	public void clearFacetConstraints(Facet facetType) {
		removeFacets(facetType);
		updateOfferingLayer(getSector());
	}

	/**
	 * Updates the facets
	 *
	 * @param changes
	 */
	public void addOrRemoveFacetConstraints(List<FacetChangeToggle> changes) {

		for (Object obj : changes) {

			if (obj instanceof FacetChangeToggle) {

				FacetChangeToggle change = (FacetChangeToggle) obj;
				log.trace("Update listeners: " +
						(change.getStatus() ? "YES" : "NO") +
						"; facet: " + change.getFacet() +
						"; value: " + change.getValue());

				/*
				 * Handle time-based facets
				 */
				if (change.getFacet().equals(Facet.TIME_PERIOD)) {
					// remove all old TIME PERIOD facets
					removeFacets(Facet.TIME_PERIOD);

					// add the facet restriction if it is different from the
					// 'No restriction' option
					if (!change.getValue().equalsIgnoreCase(TimeFacet.ALL.toString())) {
						getFacets().add(change);
					}

					// keep going through facets
					continue;
				}

				if (change.getStatus()) {
					// keep constraint if it was turned on
					facets.add(change);
				} else {
					// remove constraint if it was turned off
					facets.remove(change);
				}
			}
		}

		updateOfferingLayer(getSector());
	}

	/**
	 * Removes all facets of the given type
	 *
	 * @param type
	 */
	private void removeFacets(Facet type) {

		// remove all time period facets
		List<FacetChangeToggle> toRemove = new ArrayList<FacetChangeToggle>();
		//  collect them
		for (FacetChangeToggle facet : getFacets()) {
			if (facet.getFacet().equals(type))
				toRemove.add(facet);
		}
		// remove them
		getFacets().removeAll(toRemove);
	}

	/**
	 * Updates the layer
	 *
	 * @param sector Geographical sector restriction, may be null
	 */
	private void updateOfferingLayer(Sector sector) {

		Collection<Renderable> filtered = filterOfferingMarkers(sector, false);

		/*
		 * Update layer
		 */
		setRenderables(filtered);
		getWwd().redrawNow();

		/*
		 * Notify that contributions may have changed
		 */
		QuerySetEventNotifier.getInstance().fireEvent(this,
				QuerySetEventType.QUERYSET_OFFERING_LAYER_CONTRIBUTION_CHANGED);
	}

	/**
	 * Filters the markers
	 *
	 * @param sector Geographical sector restriction, may be null
	 * @param sectorOnly filters using sector only if true, filters on facets
	 *                   also if false
	 * @return
	 */
	private Collection<Renderable> filterOfferingMarkers(Sector sector,
			boolean sectorOnly)
	{

		Collection<Renderable> filtered = new ArrayList<Renderable>();

		// we have to work with all markers here, not just the ones
		// that are visible (actively part of the Marker Layer)
		for (Renderable r : allRenderables) {

			if (r instanceof SensorOfferingMarker) {

				SensorOfferingMarker marker = (SensorOfferingMarker)r;
				SensorOffering offering = marker.getSensorOffering();

				// skip markers outside the geographical constraint
				if (!offeringInSector(offering, sector)) {
					continue;
				}

				// if we only consider the sector we are done and can continue
				if (sectorOnly) {
					filtered.add(r);
					continue;
				}

				// indicates whether some of these properties were set
				boolean facetObservedProperty = false;
				boolean facetTime = false;
				boolean facetFormat = false;

				// indicates whether to include marker based on the
				// corresponding facet
				boolean includeObservedProperty = false;
				boolean includeTime = false;
				boolean includeFormat = false;


				boolean conjunction = false;

				/*
				 * If there are no facets, include all offerings
				 */
				if (facets.size() <= 0)
					conjunction = true;

				for (FacetChangeToggle facet : facets) {

					String value = facet.getValue();
					switch (facet.getFacet()) {

					case OBSERVED_PROPERTY:

						facetObservedProperty = true;

						List<String> observationProperties =
							offering.getObservedProperties();
						if (observationProperties.contains(value))
							includeObservedProperty = true;
						break;

					case TIME_PERIOD:

						facetTime = true;

						Date start = offering.getStartTime();
						Date end = offering.getEndTime();

						if (value.equals(TimeFacet.ONEDAY.toString())) {
							includeTime =
									TimeUtils.includesLastDay(start, end);
						} else if (value.equals(TimeFacet.ONEWEEK.toString())) {
							includeTime =
									TimeUtils.includesLastWeek(start, end);
						}

						// if this condition was false, no other condition can
						// override it (logical conjunction)
//						if (!include)
//							conjunction = false;

						break;
//					case FEATURE_OF_INTEREST:
//						List<String> featuresOfInterest =
//						offering.getFeatureOfInterest();
//						if (featuresOfInterest.contains(value))
//							include = true;
//						break;

					case RESPONSE_FORMAT:

						facetFormat = true;

						List<String> responseFormats =
								offering.getResponseFormats();
						if (responseFormats.contains(value))
							includeFormat = true;

						break;

					}
				}

				conjunction = true;
				if (facetObservedProperty && !includeObservedProperty ||
						facetTime && !includeTime ||
						facetFormat && !includeFormat)
					conjunction = false;

//				if (!conjunction && includeObservedProperty && includeTime)
//					conjunction = true;

				if (conjunction) {
					filtered.add(r);
				}

			} else {
				log.warn("A marker in a SOS Offering Layer was not " +
						"an instance of " +
						SensorOfferingPlacemark.class.getName());
			}
		}

		return filtered;
	}

	/**
	 * Collect statistics from the layer, we need to collect:
	 *
	 * 1. How many offerings total
	 * 2. How many offerings in sector
	 * 3. How many offerings match the observed properties facets
	 * 4. How many offerings match the time facet?
	 * 5. How many offerings match the format facet?
	 *
	 * @param sector Geographical sector restriction, may be null
	 * @return
	 */
	public SosOfferingLayerStats collectStats()
	{

		Sector sector = getSector();

		Map<String, Integer> propertyCount = new HashMap<String, Integer>();
		Collection<SensorOfferingItem> offeringItems = new ArrayList<SensorOfferingItem>();
		Collection<String> formats = new HashSet<String>();

		// all offerings
		int countAll = 0;
		// all offerings in sector
		int countInSector = 0;
		// all offerings in sector + matching time
		int countTime = 0;
		// all offerings in sector + matching properties
		int countProperties = 0;
		// all offerings in sector + matching formats
		int countFormats = 0;
		// all offerings in sector + matching facets
		int countFilter = 0;

		// we have to work with all markers here, not just the ones
		// that are visible (actively part of the Marker Layer)
		for (Renderable r : allRenderables) {

			if (r instanceof SensorOfferingMarker) {

				SensorOfferingMarker marker = (SensorOfferingMarker)r;
				SensorOffering offering = marker.getSensorOffering();

				// load data if we need to
				if (!offering.isLoaded()) {
					SosCapabilities capabilities =
							SosUtil.getCapabilities(marker.getService().getServiceUrl());
					offering.loadSensorOffering(capabilities);
				}

				// skip markers outside the geographical constraint
				if (!offeringInSector(offering, sector)) {
					// count the offerings
					countAll++;
					continue;
				} else {
					// count the offerings regardless
					countAll++;
					// count the ones in the sector
					countInSector++;
				}

				// indicates whether some of these properties were set
				boolean facetObservedProperty = false;
				boolean facetTime = false;
				boolean facetFormat = false;

				// indicates whether to include marker based on the
				// corresponding facet
				boolean includeObservedProperty = false;
				// true by default (no time restriction)
				boolean includeTime = false;
				boolean includeFormat = false;

				boolean conjunction = false;

				/*
				 * If there are no facets, include all offerings
				 */
				if (facets.size() <= 0)
					conjunction = true;

				for (FacetChangeToggle facet : facets) {

					String value = facet.getValue();
					switch (facet.getFacet()) {

					case OBSERVED_PROPERTY:

						facetObservedProperty = true;

						List<String> observationProperties =
							offering.getObservedProperties();
						if (observationProperties.contains(value))
							includeObservedProperty = true;
						break;

					case TIME_PERIOD:

						facetTime = true;

						Date start = offering.getStartTime();
						Date end = offering.getEndTime();

						if (value.equals(TimeFacet.ALL.toString())) {
							includeTime = true;
						} else if (value.equals(TimeFacet.ONEDAY.toString())) {
							includeTime =
									TimeUtils.includesLastDay(start, end);
						} else if (value.equals(TimeFacet.ONEWEEK.toString())) {
							includeTime =
									TimeUtils.includesLastWeek(start, end);
						}

						break;

					case RESPONSE_FORMAT:

						facetFormat = true;

						List<String> responseFormats =
								offering.getResponseFormats();
						if (responseFormats.contains(value))
							includeFormat = true;

						break;
					}
				}

				// collect the observed property data
				for (String property : offering.getObservedProperties()) {
					if (!propertyCount.containsKey(property)) {
						// add the first one
						propertyCount.put(property, 1);
					} else {
						// update an existing count
						propertyCount.put(property,
								propertyCount.get(property) + 1);
					}
				}

				// collect the formats
				formats.addAll(offering.getResponseFormats());

				/*
				 * Count statistics
				 *
				 * Only count observed properties if we counted time
				 * and only count format if we counted properties
				 *
				 * This way we achieve a continuously decreasing set of matches
				 */

				if (!facetTime || includeTime) {
					countTime++;

					if (!facetObservedProperty || includeObservedProperty) {
						countProperties++;

						if (!facetFormat || includeFormat)
							countFormats++;
					}
				}

				conjunction = true;
				if (facetObservedProperty && !includeObservedProperty ||
						facetTime && !includeTime ||
						facetFormat && !includeFormat)
					conjunction = false;

				if (conjunction) {

					SensorOfferingItem offeringItem =
							new SensorOfferingItem(marker.getService(), offering);

					offeringItems.add(offeringItem);

					countFilter++;
				}


			} else {
				log.warn("A marker in a SOS Offering Layer was not " +
						"an instance of " +
						SensorOfferingPlacemark.class.getName());
			}
		}

		SosOfferingLayerStats stats = new SosOfferingLayerStats();

		stats.setPropertyCount(propertyCount);
		stats.setSensorOfferingItems(offeringItems);
		stats.setFormats(formats);
		stats.setCountAll(countAll);
		stats.setCountInSector(countInSector);
		stats.setCountTime(countTime);
		stats.setCountProperties(countProperties);
		stats.setCountFormats(countFormats);
		stats.setCountFilter(countFilter);

		return stats;
	}


	/**
	 * Returns the observed properties that this layer contributes to
	 * the faceted search
	 *
	 */
	@Override
	public FacetData getObservedPropertiesContribution() {
		return getObservedPropertiesContribution(getSector());
	}

	/**
	 * Returns the observed properties that this layer contributes to
	 * the faceted search within a given sector and other filtering
	 * criteria
	 *
	 * @param sector
	 * @return
	 */
	private FacetData getObservedPropertiesContribution(Sector sector) {

		Map<String, Integer> propertyCount = new HashMap<String, Integer>();

		System.out.println("TT: Collecting from " +
				filterOfferingMarkers(sector, true).size() + " renderables");

		// we have to consider all markers that we know of
		for (Renderable m : filterOfferingMarkers(sector, true)) {

			if (m instanceof SensorOfferingMarker) {

				SensorOfferingMarker marker = (SensorOfferingMarker)m;
				SensorOffering offering = marker.getSensorOffering();

				// load data if we need to
				if (!offering.isLoaded()) {
					SosCapabilities capabilities =
							SosUtil.getCapabilities(marker.getService().getServiceUrl());
					offering.loadSensorOffering(capabilities);
				}

				// collect the data
				for (String property : offering.getObservedProperties()) {
					if (!propertyCount.containsKey(property)) {
						// add the first one
						propertyCount.put(property, 1);
					} else {
						// update an existing count
						propertyCount.put(property,
								propertyCount.get(property) + 1);
					}
				}

			} else {
				log.warn("A marker in a SOS Offering Layer was not " +
						"an instance of " +
						SensorOfferingMarker.class.getName());
			}
		}

		return new FacetData(propertyCount);
	}

	/**
	 * Returns the observed properties that this layer contributes to
	 * the faceted search
	 *
	 */
	@Override
	public Collection<SensorOfferingItem> getSensorOfferingsContribution() {
		return getSensorOfferingsContribution(getSector());
	}

	/**
	 * Returns the number of offerings that is in this layer restricted by
	 * the region
	 *
	 */
	public int getNoSensorOfferingsInSector() {
		return filterOfferingMarkers(getSector(), true).size();
	}

	/**
	 * Returns the observed properties that this layer contributes to
	 * the faceted search within a given sector and other filtering
	 * criteria
	 *
	 * @param sector
	 * @return
	 */
	private Collection<SensorOfferingItem> getSensorOfferingsContribution(Sector sector) {

		Collection<SensorOfferingItem> offerings = new ArrayList<SensorOfferingItem>();

//		int noOfferingsInRegion = 0;

		// we have to consider all markers that we know of
		for (Renderable m : filterOfferingMarkers(sector, false)) {

			if (m instanceof SensorOfferingMarker) {

				SensorOfferingMarker marker = (SensorOfferingMarker) m;
				SensorOffering offering = marker.getSensorOffering();

				// load data if we need to
				if (!offering.isLoaded()) {
					SosCapabilities capabilities =
							SosUtil.getCapabilities(marker.getService().getServiceUrl());
					offering.loadSensorOffering(capabilities);
				}

				SensorOfferingItem offeringItem =
						new SensorOfferingItem(marker.getService(), offering);

				offerings.add(offeringItem);

			} else {
				log.warn("A marker in a SOS Offering Layer was not " +
						"an instance of " +
						SensorOfferingMarker.class.getName());
			}
		}

		return offerings;
	}

	/**
	 * Returns true if the offering is in the
	 *
	 * @param offering
	 * @param sector Geographical sector restriction, may be null
	 * @return
	 */
	private boolean offeringInSector(SensorOffering offering, Sector sector) {
		// if we have defined a sector, ensure that we are dealing with
		// offerings within this sector
		if (sector != null) {
			if (!WorldWindUtils.offeringInSection(offering, sector))
				return false;
		}
		return true;
	}

	/**
	 * Returns the World Window object
	 *
	 * @return
	 */
	public WorldWindow getWwd() {
		return world;
	}

	/**
	 * Returns the sector to use for geographical constraints
	 *
	 * @return
	 */
	public Sector getSector() {
		// update the layer displaying offerings
//		if (locationSearchSector != null)
//			return locationSearchSector;
		return selector.getSector();
	}

	/**
	 * Hides the geographical sector restriction layer
	 *
	 */
	public void hideSector() {
		selector.hide();
	}

	/**
	 * Shows the geographical sector restriction layer
	 *
	 */
	public void showSector() {
		selector.show();
	}

	/**
	 * Enables the sector tool for this layer
	 *
	 */
	public void enableSector() {
		selector.enable();
	}

	/**
	 * Enables the sector tool for this layer
	 *
	 */
	public void disableSector() {
		selector.disable();
	}

	/**
	 * Returns the map layer model object
	 *
	 * Implements {@link QuerySetMap}
	 *
	 * @return
	 */
	@Override
	public MapId getMapId() {
		return mapId;
	}

	/**
	 * Clears the sector
	 *
	 */
	public void clearSector() {

		// trigger display of everything
		updateOfferingLayer(getSector());
	}

	/**
	 * Clears all the facet restrictions
	 *
	 */
	public void clearFacets() {

		facets.clear();

		// trigger display of everything
		updateOfferingLayer(getSector());
	}


	/**
	 * Returns the correctly active facet constraints
	 *
	 * @return
	 */
	public Set<FacetChangeToggle> getFacets() {
		return facets;
	}

	/**
	 * The first time the markers are set for this layer, save them.
	 * Otherwise, simply call the super method.
	 *
	 * @param renderables
	 */
	@Override
	public void setRenderables(Iterable<Renderable> renderables) {

		if (!markersInitialized) {
			allRenderables = renderables;
			log.trace("SET renderables");
			markersInitialized = true;

			QuerySetEventNotifier.getInstance().fireEvent(this,
					QuerySetEventType.QUERYSET_OFFERING_LAYER_CONTRIBUTION_CHANGED);

			log.trace("Updating contributions from setRenderables()");
			log.trace("Initialized markers for layer: " + getName());
		}

		// hides selection
		hideSelection();

		super.setRenderables(renderables);
		log.trace("Layer " + getName() + " setting " +
				getNumRenderables() + " renderables.");
	}

	/**
	 * The first time the markers are set for this layer, save them.
	 * Otherwise, simply call the super method.
	 *
	 * @param renderables
	 */
	public void resetRenderables(Iterable<Renderable> renderables) {
		log.trace("RESETnig renderables (was: " +
				countRenderables(allRenderables) + ", new: " +
				countRenderables(renderables) + ")");
		allRenderables = renderables;
		markersInitialized = true;
		log.trace("Initialized markers for layer: " + getName());
		super.setRenderables(renderables);
		log.trace("Layer " + getName() + " setting " +
				getNumRenderables() + " renderables.");

		//  hides selection
		hideSelection();

		QuerySetEventNotifier.getInstance().fireEvent(this,
				QuerySetEventType.QUERYSET_OFFERING_LAYER_CONTRIBUTION_CHANGED);
		log.trace("Updating contributions from resetRenderables()");
	}

	/**
	 * Track the visibility status of this layer
	 *
	 */
	@Override
	public void setEnabled(boolean status) {

		super.setEnabled(status);

		// add or remove this object as a listener for the selector service
		// that constraints a given region
		if (status) {
//			selector.enable();
			selector.addPropertyChangeListener(this);

			// update the layer with the sector in case there is already
			// a sector enabled
			updateOfferingLayer(getSector());

			// show selection layer
			layerSelection.setEnabled(true);

		} else {

//			selector.disable();
			selector.removePropertyChangeListener(this);

			getWwd().redrawNow();

			// show selection layer
			layerSelection.setEnabled(false);
		}

		// notify facet viewer that this layer was enabled/disabled
		EventNotifier.getInstance().fireEvent(this,
				EventType.DISCOVERY_FACET_DATA_CHANGE, this);
	}

	private int countRenderables(Iterable<Renderable> renderables) {
		int count = 0;
		for (Renderable r : renderables) {
			count++;
		}
		return count;
	}

	/**
	 * Event listener for changes in the sector selector
	 *
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();

		if (oldValue instanceof Sector) {

			Sector oldSector = (Sector) oldValue;
			Sector newSector = (Sector) newValue;

			if (newSector == null) {

				if (latestUserSelection == null ||
						!latestUserSelection.equals(oldSector))	{

					// remove all OBSERVED FACETS since we moved the sector
					// (i.e. we "start over")
//					removeFacets(Facet.OBSERVED_PROPERTY);

					// update discovery layer
					updateOfferingLayer(oldSector);

					// notify facet viewer that the underlying facet data may
					// have changed
//					EventNotifier.getInstance().fireEvent(this,
//							EventType.DISCOVERY_FACET_DATA_CHANGE, this);

//					QuerySetEventNotifier.getInstance().fireEvent(this,
//							QuerySetEventType.QUERYSET_OFFERING_LAYER_CONTRIBUTION_CHANGED,
//							this);
					log.trace("Updating contributions from propertyChange()");
				}

				latestUserSelection = oldSector;

				// clear the location sector
				//		locationSearchSector = null;

			}
		}
	}

	/**
	 * Highlights the given sensor offerings
	 *
	 * @param sensorOfferings
	 */
	public void showSelection(List<SensorOffering> sensorOfferings) {
		layerSelection.showSelection(sensorOfferings);
	}

	/**
	 * Hides selection
	 *
	 */
	public void hideSelection() {
		layerSelection.hideSelection();
	}

	/**
	 * Prepares deletion of layer
	 *
	 */
	public void prepareLayerDelete() {
		// disable and remove sector selection
		disableSector();
		// remove selection layer
		getWwd().getModel().getLayers().remove(layerSelection);
	}

}

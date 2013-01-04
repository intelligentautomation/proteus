package com.iai.proteus.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.iai.proteus.common.sos.model.SensorOffering;


/**
 * Layer highlighting selections of offerings
 *
 * @author Jakob Henriksson
 *
 */
public class SelectionLayer extends RenderableLayer {

	private static final Logger log =
		Logger.getLogger(SelectionLayer.class);

	private WorldWindow world;

	// attributes
	protected static ShapeAttributes regionAttributes;
	protected static PointPlacemarkAttributes placemarkAttributes;

	// create attributes
	static {

		Material material =	new Material(new Color(0, 255, 0));

		/* region attributes */
		regionAttributes = new BasicShapeAttributes();
		regionAttributes.setDrawOutline(false);
		regionAttributes.setDrawInterior(true);
		regionAttributes.setInteriorMaterial(material);

		/* marker attributes */
		placemarkAttributes = new PointPlacemarkAttributes();
		placemarkAttributes.setUsePointAsDefaultImage(true);
		placemarkAttributes.setScale(20d);
		placemarkAttributes.setLineMaterial(material);
	}

	/**
	 * Constructor
	 *
	 * @param worldWindow
	 */
	public SelectionLayer(WorldWindow worldWindow) {

		if (worldWindow == null) {
			String msg = "World window object was null";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		this.world = worldWindow;

		// general layer settings
		setName("Sensor offering selection");
		setPickEnabled(false);
	}

	/**
	 * Hides the selection
	 */
	public void hideSelection() {
		removeAllRenderables();
	}

	/**
	 * Shows the selection according to the given sensor offering
	 *
	 * @param sensorOffering
	 */
	public void showSelection(SensorOffering sensorOffering) {
		showSelection(Arrays.asList(new SensorOffering[] { sensorOffering }));
	}

	/**
	 * Shows the selection according to the given sensor offerings
	 *
	 * @param sensorOfferings
	 */
	public void showSelection(List<SensorOffering> sensorOfferings) {
		// hide old selection
		hideSelection();

		// add offerings selections
		for (SensorOffering sensorOffering : sensorOfferings) {

			Renderable renderable = null;

			if (WorldWindUtils.isRegion(sensorOffering)) {

				// construct the offering region
				renderable = new SensorOfferingRegion(sensorOffering,
						WorldWindUtils.getBoundingBoxSector(sensorOffering),
						regionAttributes);

			} else {

				renderable = new SensorOfferingPlacemark(sensorOffering,
						WorldWindUtils.getCentralPosition(sensorOffering),
						placemarkAttributes);
			}

			// add the new selection renderable
			if (renderable != null)
				addRenderable(renderable);
		}

		redraw();
	}

	/**
	 * Updates the map
	 */
	protected void redraw() {
		// update
		world.redrawNow();
	}

	protected WorldWindow getWwd() {
		return world;
	}
}

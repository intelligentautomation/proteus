/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.UserFacingIcon;

import java.awt.Color;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.iai.proteus.Activator;
import com.iai.proteus.common.sos.model.SensorOffering;


/**
 * A layer to display where plotted data comes from
 *
 * @author Jakob Henriksson
 *
 */
public class DataPlotProvenanceLayer extends SelectionLayer {

	private IconLayer layerIcon;

	/**
	 * Constructor
	 *
	 * @param worldWindow
	 */
	public DataPlotProvenanceLayer(WorldWindow worldWindow) {
		super(worldWindow);
		setName("Data plot provenance layer");

		layerIcon = new IconLayer();
		getWwd().getModel().getLayers().add(layerIcon);
	}

	/**
	 * Shows the selection according to the given sensor offerings
	 *
	 * @param sensorOfferings
	 */
	@Override
	public void showSelection(List<SensorOffering> sensorOfferings) {
		// hide old selection
		hideSelection();
		layerIcon.removeAllIcons();

		Color color = new Color(0, 255, 0);

		// add offerings selections
		for (SensorOffering sensorOffering : sensorOfferings) {

			Renderable renderable = null;
			Position position = null;

			if (WorldWindUtils.isRegion(sensorOffering)) {

				// copy attributes
				ShapeAttributes attrs =
						new BasicShapeAttributes(regionAttributes);
				attrs.setInteriorMaterial(new Material(color));

				// construct the offering region
				renderable = new SensorOfferingRegion(sensorOffering,
						WorldWindUtils.getBoundingBoxSector(sensorOffering),
						attrs);

				// calculate position
//				position = WorldWindUtils.getUpperPosition(sensorOffering);

				LatLon[] corners =
						WorldWindUtils.getBoundingBoxSector(sensorOffering).getCorners();
				position = new Position(corners[3], 0.0);

				// move the icon position slightly
//				position =
//						position.subtract(new Position(Angle.fromDegrees(-0.5),
//								Angle.fromDegrees(0.5),
//								position.getElevation()));

			} else {

				// copy attributes
				PointPlacemarkAttributes attrs =
						new PointPlacemarkAttributes(placemarkAttributes);
				attrs.setLineMaterial(new Material(color));

				renderable = new SensorOfferingPlacemark(sensorOffering,
						WorldWindUtils.getCentralPosition(sensorOffering),
						attrs);

				// calculate position
				position = WorldWindUtils.getUpperPosition(sensorOffering);

				// move the icon position slightly
				position =
						position.subtract(new Position(Angle.fromDegrees(-0.5),
								Angle.fromDegrees(0.5),
								position.getElevation()));
			}

			if (position != null) {
				UserFacingIcon icon =
						new UserFacingIcon(getIconSource("icons/fugue/chart.png"),
								position);
				if (icon != null) {
					icon.setAlwaysOnTop(true);
					layerIcon.addIcon(icon);
				}
			}

			// add the new selection renderable
			if (renderable != null)
				addRenderable(renderable);
		}

		// TODO: ensure the z-index of icon layer

		// update
		redraw();
	}

	private URL getIconSource(String iconPath) {
		return FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID),
				new Path(iconPath), null);
	}

}

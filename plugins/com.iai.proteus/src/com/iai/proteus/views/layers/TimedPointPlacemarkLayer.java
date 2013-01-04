package com.iai.proteus.views.layers;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

/**
 * A lot of the code here was taken from the World Wind MarkersOrder example
 * 
 * 
 * @author Jakob Henriksson
 *
 */
public class TimedPointPlacemarkLayer extends RenderableLayer {
	
	/**
	 * Constructor 
	 * 
	 */
	public TimedPointPlacemarkLayer() {
		super();
	}
	
	/**
	 * Constructor 
	 * 
	 * @param renderables
	 */
	public TimedPointPlacemarkLayer(Iterable<Renderable> renderables) {
		super();
		setRenderables(renderables);
	}

}



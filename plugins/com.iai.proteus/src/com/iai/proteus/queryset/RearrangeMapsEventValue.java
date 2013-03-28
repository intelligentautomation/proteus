/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import java.util.ArrayList;
import java.util.List;

import com.iai.proteus.model.map.IMapLayer;

/**
 * Container class for the event value of the re-arranging map layers event 
 * 
 * @author Jakob Henriksson
 *
 */
public class RearrangeMapsEventValue {
	
	private List<IMapLayer> maps;
	private IMapLayer target;
	
	/**
	 * Constructor 
	 * 
	 * @param maps
	 * @param target
	 */
	public RearrangeMapsEventValue(List<? extends IMapLayer> maps, IMapLayer target) {
		this.maps = new ArrayList<IMapLayer>();
		for (Object o : maps) {
			if (o instanceof IMapLayer)
				this.maps.add((IMapLayer) o);
		}
		this.target = target;
	}

	/**
	 * @return the maps
	 */
	public List<IMapLayer> getMaps() {
		return maps;
	}

	/**
	 * @param maps the maps to set
	 */
	public void setMaps(List<IMapLayer> maps) {
		this.maps = maps;
	}

	/**
	 * @return the target
	 */
	public IMapLayer getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(IMapLayer target) {
		this.target = target;
	}
	
}

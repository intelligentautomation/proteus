/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.queryset;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * Manages the state of open query sets 
 * 
 * @author Jakob Henriksson
 *
 */
public class QuerySetOpenState extends AbstractSourceProvider {

	public final static String STATE = 
			"com.iai.proteus.sourceprovider.queryset.open";
	
	public final static String ENABLED = "ENABLED";
	public final static String DISABLED = "DISABLED";
	
	enum State {
		ENABLED, DISABLED 
	};
	
	private State curState = State.ENABLED;

	@Override
	public Map<?, ?> getCurrentState() {
      Map<String, String> map = new HashMap<String, String>(1);
      switch (curState) {
      case ENABLED:
    	  map.put(STATE, ENABLED);
    	  break;
      case DISABLED:
    	  map.put(STATE, DISABLED);
    	  break;
      }
      return map;		
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { STATE };
	} 
	
	@Override
	public void dispose() {
		
	}
	
	/**
	 * Indicate that there are open query sets 
	 * 
	 */
	public void setQuerySetOpen() {
		curState = State.ENABLED;
		fireSourceChanged(ISources.WORKBENCH, STATE, ENABLED);
    }
	
	/**
	 * Indicate that there are no open query sets 
	 * 
	 */
	public void setNoQuerySetOpen() {
		curState = State.DISABLED;
        fireSourceChanged(ISources.WORKBENCH, STATE, DISABLED);
    }	
	
}

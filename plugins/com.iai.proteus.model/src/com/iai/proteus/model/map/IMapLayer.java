/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model.map;


/**
 * Interface for map layers
 * 
 * @author jhenriksson
 *
 */
public interface IMapLayer extends MapIdentifier {
	
	public boolean isActive();
	public void setActive(boolean status);

}

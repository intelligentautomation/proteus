/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model;

/**
 * Implementing classes have a name and can be re-named 
 * 
 * @author Jakob Henriksson
 *
 */
public interface Namable {
	
	/**
	 * Sets the name 
	 * 
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Returns the name 
	 * 
	 * @return
	 */
	public String getName();

}

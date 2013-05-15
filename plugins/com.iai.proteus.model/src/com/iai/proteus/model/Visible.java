/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model;

/**
 * Implementing classes can set visibility status 
 * 
 * @author Jakob Henriksson 
 *
 */
public interface Visible {
	
	public void show();
	public void hide(); 
	public boolean isVisible(); 

}

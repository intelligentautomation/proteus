/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model;

/**
 * Implementing classes are model objects that can be deleted
 * 
 * @author Jakob Henriksson 
 *
 */
public interface Deletable {
	
	/**
	 * Deletes the model object 
	 * 
	 * @return True if the object was deleted, false otherwise 
	 */
	public boolean delete(); 

}

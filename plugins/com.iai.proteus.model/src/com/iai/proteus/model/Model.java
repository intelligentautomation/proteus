/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.model;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents and abstract model object 
 * 
 * @author Jakob Henriksson 
 * 
 */
public abstract class Model implements Namable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected String name;
	protected Model parent;
		
	/**
	 * 
	 */
	public Model() {
		// default 
		name = "untitled";
	}
	
	public Model getParent() {
		return parent;
	}
	
	public void setParent(Model node) {
		this.parent = node;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * The reason this is deprecated is because we are now using 
	 * Java Bean serialization to persist models objects to disk 
	 * 
	 * @param document
	 * @return
	 */
	@Deprecated
	public abstract Element serialize(Document document);
	
}

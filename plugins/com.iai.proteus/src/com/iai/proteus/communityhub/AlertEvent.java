/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub;

import java.util.EventObject;

/**
 * Alert event object 
 * 
 * @author Jakob Henriksson
 *
 */
public class AlertEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor 
	 * 
	 * @param event
	 */
	public AlertEvent(Object event) {
		super(event);

	}

}

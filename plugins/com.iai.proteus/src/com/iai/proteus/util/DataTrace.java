/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.util;

import gov.nasa.worldwind.geom.Position;

import java.util.Date;

public class DataTrace {
	
	private Position position;
	private Date time; 
	private Double value; 
	
	/**
	 * Constructor 
	 * 
	 * @param position
	 * @param time
	 */
	public DataTrace(Position position, Date time) {
		this.position = position;
		this.time = time; 
		this.value = null;
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}
	
	

}

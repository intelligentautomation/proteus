/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

/**
 * Enumeration class for event topics 
 * 
 * @author Jakob Henriksson
 *
 */
public enum EventTopic {
	
	// catch all for query sets
	TOPIC_QUERYSET("proteus/queryset/*"),
	
	// load a query set
	QS_LOAD("proteus/queryset/load"),
	// initialize layers 
	QS_LAYERS_INIT("proteus/queryset/layers/init"),
	// delete layers 
	QS_LAYERS_DELETE("proteus/queryset/layers/delete"),
	// activate layers 
	QS_LAYERS_ACTIVATE("proteus/queryset/layers/activate"),
	// delete layers 
	QS_LAYERS_REARRANGE("proteus/queryset/layers/rearrange"),
	// bounding box region enabled
	QS_REGION_ENABLED("proteus/queryset/region/enabled"),
	// bounding box region disabled 
	QS_REGION_DISABLED("proteus/queryset/region/disabled"), 
	// setting the bounding box region restriction
	QS_REGION_SET("proteus/queryset/region/set"), 
	// 
	QS_REGION_UPDATED("proteus/queryset/region/updated"), 
	
	// toggle services 
	QS_TOGGLE_SERVICES("proteus/queryset/service/toggle"), 
	// sensor offering layer info change 
	QS_OFFERINGS_CHANGED("proteus/queryset/offering/change");
	
	private String topic;
	
	/**
	 * Constructor
	 * 
	 * @param topic
	 */
	private EventTopic(String topic) {
		this.topic = topic;
	}
	
	@Override
	public String toString() {
		return topic;
	}

}

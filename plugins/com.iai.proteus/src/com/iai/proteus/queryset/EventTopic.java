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
	
	// catch all topic for query sets
	TOPIC_QUERYSET("proteus/queryset/*"),
	
	
	// event topic tags 
	
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
	// used when a facet changed 
	QS_FACET_CHANGED("proteus/queryset/facet/changed"), 
	// when facets are cleared
	QS_FACET_CLEARED("proteus/queryset/facet/cleard"), 
	// sensor offering layer info change 
	QS_OFFERINGS_CHANGED("proteus/queryset/offering/changed"), 
	
	QS_PREVIEW_PLOT("proteus/queryset/preview/plot"),
	QS_PREVIEW_TABLE("proteus/queryset/preview/table"), 
	QS_PREVIEW_TABLE_REQUEST("proteus/queryset/preview/table/request"),
	QS_PREVIEW_TABLE_UPDATE("proteus/queryset/preview/table/update"),
	QS_PREVIEW_TABLE_CLEAR("proteus/queryset/preview/table/clear"),
	
	// toggle map layer
	QS_MAPS_LAYER_TOGGLE("proteus/queryset/map/layer/toggle"), 
	// remove all map layers from a particular service 
	QS_MAPS_DELETE_FROM_SERVICE("proteus/queryset/map/layer/remove_service"), 
	
	QS_FLY_TO_LATLON("proteus/queryset/fly"); 
	
	
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

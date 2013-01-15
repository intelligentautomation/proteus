/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

/**
 * Event Types
 *
 * @author Jakob Henriksson
 *
 */
public enum QuerySetEventType {

	// used to notify that a layer should be initialized
	QUERYSET_INITIALIZE_LAYER,
	// used to notify that a service has changed
	QUERYSET_SERVICE_TOGGLE,
	// used to notify that a query set has been closed/deleted
	QUERYSET_LAYERS_DELETE,
	// used to notify that a query set should be the active
	QUERYSET_LAYERS_ACTIVATE,

	// used to notify that a geographic selection was enabled
	QUERYSET_REGION_ENABLED,
	// used to notify that a geographic selection was disabled
	QUERYSET_REGION_DISABLE,

	// used to notify that the offering layer contributions have changed
	QUERYSET_OFFERING_LAYER_CONTRIBUTION_CHANGED,
	// used to notify that the facet specification has changed
	QUERYSET_FACET_CHANGE,
	// used to notify that the facet type should be cleared
	QUERYSET_FACET_CLEAR,


	// used to request moving to sensor offering
	QUERYSET_FLY_TO_OFFERING,

	// used to plot data
	QUERYSET_PREVIEW_PLOT,
	

	// used to notify filtered offering set changed
	OFFERING_SET_OFFERING_LAYER_CONTRIBUTION_CHANGED,
}

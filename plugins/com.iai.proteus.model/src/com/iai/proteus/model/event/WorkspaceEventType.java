package com.iai.proteus.model.event;

/**
 * Event Types 
 * 
 * @author Jakob Henriksson 
 *
 */
public enum WorkspaceEventType {
	
	// used to notify that a model object/resource has changed 
	WORKSPACE_MODEL_UPDATED, 
	
	// used to notify that a layer has been toggled  
	WORKSPACE_LAYER_TOGGLE,
	// used to notify that a layer has been deleted 
	WORKSPACE_LAYER_DELETE,
	// user to notify that a layer should be re-created 
	WORKSPACE_LAYER_RECREATE, 
	// used to notify that the color associated with a map layer has changed
	WORKSPACE_LAYER_COLOR_CHANGE,

	// used to notify that the map show move to a given position 
	WORKSPACE_FLY_TO_OFFERING, 
	
	// used to notify that a query is to be saved 
	WORKSPACE_SAVE_QUERY, 
	// used to notify that a saved query should be executed 
	WORKSPACE_EXEC_QUERY, 
	
	WORKSPACE_WMS_TOGGLE
	
}

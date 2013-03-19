/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub.apiv1;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


/**
 * Property provider for @{link Alert} objects 
 * 
 * @author Jakob Henriksson
 *
 */
public class AlertPropertySource implements IPropertySource {
	
	private final String CAT_GROUP = "Alert";
	private final String CAT_BBOX = "Bounding Box";
	private final String CAT_VALIDITY = "Validity";
	
	private final String PROP_ALERT_TYPE = "alert_type";
	private final String PROP_ALERT_DETAIL = "alert_detail";
	private final String PROP_ALERT_DATE_CREATED = "alert_date_created";
	private final String PROP_ALERT_VALID_FROM = "alert_valid_from";
	private final String PROP_ALERT_VALID_TO = "alert_valid_to";
	private final String PROP_ALERT_LAT_LOWER = "alert_lat_lower";
	private final String PROP_ALERT_LAT_UPPER = "alert_lat_upper";
	private final String PROP_ALERT_LON_LOWER = "alert_lon_lower";
	private final String PROP_ALERT_LON_UPPER = "alert_lon_upper";
	private final String PROP_ALERT_SERVICE_ENDPOINT = "alert_service_endpoint";
	private final String PROP_ALERT_SENSOR_OFFERING = "alert_sensor_offering";
	private final String PROP_ALERT_OBSERVED_PROPERTY = "alert_observed_property";
	
	private Alert alert; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public AlertPropertySource() {
		
	}

	/**
	 * Constructor 
	 * 
	 * @param alert
	 */
	public AlertPropertySource(Alert alert) {
		this.alert = alert;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		PropertyDescriptor groupType = 
				new TextPropertyDescriptor(PROP_ALERT_TYPE, "Type");
		groupType.setCategory(CAT_GROUP);
		
		PropertyDescriptor groupDetail = 
				new TextPropertyDescriptor(PROP_ALERT_DETAIL, "Detail");
		groupDetail.setCategory(CAT_GROUP);

		PropertyDescriptor groupDateCreated = 
				new TextPropertyDescriptor(PROP_ALERT_DATE_CREATED, "Created");
		groupDateCreated.setCategory(CAT_GROUP);
		
		PropertyDescriptor groupValidFrom = 
				new TextPropertyDescriptor(PROP_ALERT_VALID_FROM, "Valid from");
		groupValidFrom.setCategory(CAT_VALIDITY);

		PropertyDescriptor groupValidTo = 
				new TextPropertyDescriptor(PROP_ALERT_VALID_TO, "Valid To");
		groupValidTo.setCategory(CAT_VALIDITY);
		
		PropertyDescriptor groupLatLower = 
				new TextPropertyDescriptor(PROP_ALERT_LAT_LOWER, "Lat lower");
		groupLatLower.setCategory(CAT_BBOX);

		PropertyDescriptor groupLatUpper = 
				new TextPropertyDescriptor(PROP_ALERT_LAT_UPPER, "Lat upper");
		groupLatUpper.setCategory(CAT_BBOX);

		PropertyDescriptor groupLonLower = 
				new TextPropertyDescriptor(PROP_ALERT_LON_LOWER, "Lon lower");
		groupLonLower.setCategory(CAT_BBOX);

		PropertyDescriptor groupLonUpper = 
				new TextPropertyDescriptor(PROP_ALERT_LON_UPPER, "Lon upper");
		groupLonUpper.setCategory(CAT_BBOX);
		
		PropertyDescriptor groupServiceEndpoint = 
				new TextPropertyDescriptor(PROP_ALERT_SERVICE_ENDPOINT, "Service endpoint");
		groupServiceEndpoint.setCategory(CAT_GROUP);

		PropertyDescriptor groupSensorOffering = 
				new TextPropertyDescriptor(PROP_ALERT_SENSOR_OFFERING, "Sensor offering");
		groupSensorOffering.setCategory(CAT_GROUP);

		PropertyDescriptor groupObservedProperty = 
				new TextPropertyDescriptor(PROP_ALERT_OBSERVED_PROPERTY, "Observed property");
		groupObservedProperty.setCategory(CAT_GROUP);


		return new IPropertyDescriptor[] {
				groupType, 
				groupDetail, 
				groupDateCreated,
				groupValidFrom, 
				groupValidTo,
				groupLatLower, 
				groupLatUpper, 
				groupLonLower, 
				groupLonUpper, 
				groupServiceEndpoint, 
				groupSensorOffering, 
				groupObservedProperty
		};
		
	}
	
	@Override
	public Object getPropertyValue(Object id) {
		
		if (alert != null) {
			if (id.equals(PROP_ALERT_TYPE)) {
				return alert.getType();
			} else if (id.equals(PROP_ALERT_DETAIL)) {
				return alert.getDetail();
			} else if (id.equals(PROP_ALERT_DATE_CREATED)) {
				return alert.getDateCreated();
			} else if (id.equals(PROP_ALERT_VALID_FROM)) {
				return alert.getValidFrom();
			} else if (id.equals(PROP_ALERT_VALID_TO)) {
				return alert.getValidTo();
			} else if (id.equals(PROP_ALERT_LAT_LOWER)) {
				return alert.getLatLower();
			} else if (id.equals(PROP_ALERT_LAT_UPPER)) {
				return alert.getLatUpper();
			} else if (id.equals(PROP_ALERT_LON_LOWER)) {
				return alert.getLonLower();
			} else if (id.equals(PROP_ALERT_LON_UPPER)) {
				return alert.getLonUpper();
			} else if (id.equals(PROP_ALERT_SERVICE_ENDPOINT)) {
				return alert.getServiceEndpoint();
			} else if (id.equals(PROP_ALERT_SENSOR_OFFERING)) {
				return alert.getSensorOfferingId();
			} else if (id.equals(PROP_ALERT_OBSERVED_PROPERTY)) {
				return alert.getObservedProperty();
			}
		}
		
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		
	}

}

package com.iai.proteus.model.properties;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iai.proteus.common.sos.model.GetObservationRequest;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.model.TimeInterval;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.workspace.QueryGetObservation;

/**
 * Property provider for QuerySensorOffering objects objects 
 * 
 * @author Jakob Henriksson 
 *
 */
public class QueryGetObservationPropertySource implements IPropertySource {
	
	private QueryGetObservation queryGetObservation;
	
	private final String CAT_OFFERING= "Offering";
	private final String CAT_QUERY= "Query";
	
	private final String PROP_OFFERING_ID = "offering_id";
	private final String PROP_OFFERING_NAME = "offering_name";
	private final String PROP_OFFERING_DESC = "offering_description";
	
	private final String PROP_QUERY_OBSERVED_PROPERTY = "observed_property";
	private final String PROP_QUERY_RESPONSE_FORMAT = "response_formats";
	
	private final String PROP_QUERY_INTERVAL = "interval";
	
	/**
	 * Default constructor 
	 */
	public QueryGetObservationPropertySource() {
		
	}
	
	/**
	 * Constructor 
	 * 
	 * @param offering
	 */
	public QueryGetObservationPropertySource(QueryGetObservation offering) {
		this.queryGetObservation = offering;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		PropertyDescriptor offeringId = 
				new TextPropertyDescriptor(PROP_OFFERING_ID, "ID");
		offeringId.setCategory(CAT_OFFERING);
		
		PropertyDescriptor offeringName = 
				new TextPropertyDescriptor(PROP_OFFERING_NAME, "Name");
		offeringName.setCategory(CAT_OFFERING);
		
		PropertyDescriptor offeringDesc = 
				new TextPropertyDescriptor(PROP_OFFERING_DESC, "Description");
		offeringDesc.setCategory(CAT_OFFERING);
		
		PropertyDescriptor queryObservedProperty = 
				new TextPropertyDescriptor(PROP_QUERY_OBSERVED_PROPERTY, 
						"Observed property");
		queryObservedProperty.setCategory(CAT_QUERY);	
		
		PropertyDescriptor queryResponesFormat = 
				new TextPropertyDescriptor(PROP_QUERY_RESPONSE_FORMAT, 
						"Response format");
		queryResponesFormat.setCategory(CAT_QUERY);	
		
		PropertyDescriptor queryInterval = 
				new TextPropertyDescriptor(PROP_QUERY_INTERVAL, 
						"Time interval");
		queryInterval.setCategory(CAT_QUERY);			
		
		return new IPropertyDescriptor[] {
				
				offeringId, 
				offeringName, 
				offeringDesc, 
				
				queryObservedProperty,
				queryResponesFormat,
				queryInterval
		};
	}
	
	@Override
	public Object getPropertyValue(Object id) {
		
		SensorOffering sensorOffering = queryGetObservation.getSensorOffering();
		GetObservationRequest request = 
				queryGetObservation.getGetObservationRequest();
		
		// populate the sensor offering if needed 
		if (!sensorOffering.isLoaded()) {
			Service service = queryGetObservation.getProvenance().getService();
			SosCapabilities capabilities = 
					SosUtil.getCapabilities(service.getServiceUrl());
			sensorOffering.loadSensorOffering(capabilities);
		}
		
		SimpleDateFormat format = 
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				
		if (id.equals(PROP_OFFERING_ID)) {
			return sensorOffering.getGmlId();
		} else if (id.equals(PROP_OFFERING_NAME)) {
			return sensorOffering.getName();
		} else if (id.equals(PROP_OFFERING_DESC)) {
			return sensorOffering.getDescription();
		} else if (id.equals(PROP_QUERY_OBSERVED_PROPERTY)) {
			return request.getObservedProperty();
		} else if (id.equals(PROP_QUERY_RESPONSE_FORMAT)) {
			return request.getResponseFormat();
		} else if (id.equals(PROP_QUERY_INTERVAL)) {
			Collection<TimeInterval> intervals = request.getTimeIntervals();
			if (intervals.size() > 0) {
				for (TimeInterval interval : intervals) {
					String start = 
							format.format(interval.getStart());
					String end = 
							format.format(interval.getEnd());
					// TODO: for now just show the first one
					return start + " - " + end;
				}
			}
			Date start = sensorOffering.getStartTime();
			if (start != null)
				return format.format(start);
			return null;
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

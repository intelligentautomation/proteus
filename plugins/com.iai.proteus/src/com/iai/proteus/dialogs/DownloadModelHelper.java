/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.Period;

import com.iai.proteus.common.sos.SupportedResponseFormats;
import com.iai.proteus.common.sos.model.GetObservationRequest;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.exceptions.ResponseFormatNotSupportedException;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.queryset.DataFetcher;
import com.iai.proteus.ui.queryset.SensorOfferingItem;
import com.iai.proteus.views.TimeSeriesUtil;

public class DownloadModelHelper {
	
	
	public static Collection<DownloadModel> createDownloadModels() {
		
		Collection<DownloadModel> models = new ArrayList<DownloadModel>();

		Collection<SensorOfferingItem> offeringItems = 
				new ArrayList<SensorOfferingItem>();
		
		Service service = new Service(ServiceType.SOS);
		service.setEndpoint("http://noaa.gov/sos");
		
		SensorOffering sensorOffering = new SensorOffering("id1");
		sensorOffering.addObservedProperty("http://prop1");
		sensorOffering.addResponseFormat(SupportedResponseFormats.CSV.toString());
		
		SensorOfferingItem item1 = new SensorOfferingItem(service, sensorOffering);
		
		offeringItems.add(item1);
		
		Collection<String> observedProperties = new ArrayList<String>();
		observedProperties.add("http://prop1");
		
		models.addAll(createDownloadModels(offeringItems, observedProperties, 
				new Period(24, 0, 0, 0)));
		
		return models;
	}
	
	public static Collection<DownloadModel> 
		createDownloadModels(Collection<SensorOfferingItem> offeringItems, 
				Collection<String> observedProperties, Period timePeriod) 
	{
		
		Collection<DownloadModel> models = new ArrayList<DownloadModel>();
		
		for (SensorOfferingItem offeringItem : offeringItems) {
			Service service = offeringItem.getService();
			SensorOffering sensorOffering = 
					offeringItem.getSensorOffering();
			
			// make sure it is loaded
			loadSensorOffering(service, sensorOffering);
			
			for (String property : sensorOffering.getObservedProperties()) {
				if (observedProperties.contains(property)) {

					DownloadModel model = new DownloadModel();
					
					try {

						SosDataRequest dataRequest = 
								makeDataRequests(service, sensorOffering, 
										property, timePeriod);

						model.setDataRequest(dataRequest);
						
						// create a label 
						model.setLabel(GetObservationProgressDialog.
								createQueryLabel(sensorOffering, property));						

					} catch (ResponseFormatNotSupportedException e) {
						
						model.setStatus(DownloadModel.Status.ERROR);
						model.setStatusMsg("Response format not supported");
						
						// create a label since we do not have a data request 
						model.setLabel(GetObservationProgressDialog.
								createQueryLabel(sensorOffering, property));

					}
					
					models.add(model);
				}
			}

		}
		
		return models;
	}	
	

	/**
	 * Constructs @{link SosDataRequest} objects for the given service,
	 * offering and observed properties
	 *
	 * @param service
	 * @param sensorOffering
	 * @param observedProperty
	 * @param timePeriod
	 * @return
	 * @throws ResponseFormatNotSupportedException
	 */
	private static SosDataRequest makeDataRequests(Service service,
			SensorOffering sensorOffering, String observedProperty, 
			Period timePeriod)
		throws ResponseFormatNotSupportedException
	{

		// populate Sensor Offering object from Capabilities document
		// if needed
		if (!sensorOffering.isLoaded()) {
			SosCapabilities capabilities =
				SosUtil.getCapabilities(service.getEndpoint());
			sensorOffering.loadSensorOffering(capabilities);
		}

		// Ensure that we can handle any potential response
		List<String> commonFormats =
			SosUtil.commonResponseFormats(sensorOffering);

		if (commonFormats.isEmpty()) {
			String msg =
				"We do not support any of the response formats " +
					"from this sensor offering; aborting: " +
					sensorOffering.getResponseFormats();
			throw new ResponseFormatNotSupportedException(msg,
					sensorOffering.getResponseFormats());
		}

		String responseFormat = commonFormats.get(0);

		GetObservationRequest observationRequest =
				new GetObservationRequest(sensorOffering, observedProperty,
						responseFormat);
		
		// add the time period 
		DataFetcher.addTimeInterval(observationRequest, timePeriod);

		String label =
				TimeSeriesUtil.getSeriesLabel(sensorOffering, observedProperty);

		SosDataRequest dataRequest =
				new SosDataRequest(label, service.getEndpoint(), observationRequest);

		return dataRequest;
	}		
	
	
	private static void loadSensorOffering(Service service, SensorOffering sensorOffering) {
		// populate Sensor Offering object from Capabilities document
		// if needed
		if (!sensorOffering.isLoaded()) {
			SosCapabilities capabilities =
					SosUtil.getCapabilities(service.getEndpoint());
			sensorOffering.loadSensorOffering(capabilities);
		}					
	}	

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

//import gov.nasa.worldwind.ogc.ows.OWSException;
//import gov.nasa.worldwind.ogc.ows.OWSExceptionReport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;

import com.iai.proteus.Activator;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.common.sos.exception.ExceptionReportException;
import com.iai.proteus.common.sos.model.GetObservationRequest;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.model.TimeInterval;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.dialogs.UnsupportedResponseFormatsDialog;
import com.iai.proteus.exceptions.ResponseFormatNotSupportedException;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.plot.VariablesHolder;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.ui.queryset.SensorOfferingItem;
import com.iai.proteus.views.TimeSeriesUtil;

public class DataFetcher implements IRunnableWithProgress {

	private static final Logger log = Logger.getLogger(DataFetcher.class);

	/*
	 * This variable holds what the program considers to be "NOW".
	 * The idea is to only update it at regular intervals allowing
	 * data requests to hit the cache more frequently
	 *
	 */
	private static DateTime now = new DateTime();

	/*
	 * The duration that must have passed before we update NOW,
	 * see @{link #getNow()}.
	 */
	private static Duration duration = Duration.standardMinutes(5);

	/*
	 * Variables relating to the fetch (what offering and over what period)
	 */
	private SensorOfferingItem offeringItem;
	private Period period;

	/**
	 * Constructor
	 *
	 * @param offeringItem
	 * @param period Null means that there should be no time period specified
	 */
	public DataFetcher(SensorOfferingItem offeringItem, Period period) {
		this.offeringItem = offeringItem;
		this.period = period;
	}

	/**
	 * Implementation of @{link IRunnableWithProgress}
	 *
	 */
	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException
	{
		initiateDataPreview(offeringItem, monitor);
	}

	/**
	 * Retrieves the observations based on the marker selection
	 *
	 * @param offeringItem
	 * @param monitor
	 */
	private void initiateDataPreview(SensorOfferingItem offeringItem,
			IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException
	{

		SensorOffering sensorOffering =
				offeringItem.getSensorOffering();

		Service service = offeringItem.getService();

		if (service == null) {
			log.warn("We could not get the source " +
					"for this offering; skipping");
			return;
		}

		try {

			// construct data requests
			List<SosDataRequest> dataRequests =
					makeDataRequests(service, sensorOffering);

			// issue requests to service
			executeRequests(dataRequests, monitor);

			if (monitor.isCanceled())
				log.info("User cancelled donwloading of sensor data");

		} catch (final ResponseFormatNotSupportedException e) {
			log.warn(e.getMessage());

			/*
			 * Show an error dialog
			 */
			UIUtil.update(new Runnable() {
				public void run() {
					Shell shell = UIUtil.getShell();
					new UnsupportedResponseFormatsDialog(shell,
							e.getUnsupportedFormats()).open();
				}
			});

			throw new InvocationTargetException(e);
		}
	}


	/**
	 * Make data requests for all observed properties of the sensor offering
	 *
	 * @param service
	 * @param sensorOffering
	 * @return
	 * @throws ResponseFormatNotSupportedException
	 */
	private List<SosDataRequest> makeDataRequests(Service service,
			SensorOffering sensorOffering)
		throws ResponseFormatNotSupportedException
	{
		List<String> properties = new ArrayList<String>();
		for (String p : sensorOffering.getObservedProperties()) {
			properties.add(p);
		}

		return makeDataRequests(service, sensorOffering, properties);
	}

	/**
	 * Create a request for a single given property
	 *
	 * @param service
	 * @param sensorOffering
	 * @param observedProperty
	 * @return
	 * @throws ResponseFormatNotSupportedException
	 */
	public SosDataRequest makeDataRequests(Service service,
			SensorOffering sensorOffering, String observedProperty)
		throws ResponseFormatNotSupportedException
	{
		List<String> properties = new ArrayList<String>();
		properties.add(observedProperty);
		List<SosDataRequest> requests =
				makeDataRequests(service, sensorOffering, properties);

		// return the only request that should have been made
		if (requests.size() > 0)
			return requests.get(0);

		// if there was a problem
		return null;
	}


	/**
	 * Constructs @{link SosDataRequest} objects for the given service,
	 * offering and observed properties
	 *
	 * @param sensorOffering
	 * @param service
	 * @return
	 * @throws ResponseFormatNotSupportedException
	 */
	private List<SosDataRequest> makeDataRequests(Service service,
			SensorOffering sensorOffering, List<String> observedProperties)
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

		List<SosDataRequest> dataRequests =
			new ArrayList<SosDataRequest>();

		/*
		 * Create data request objects for these properties
		 */
		for (String property : sensorOffering.getObservedProperties()) {

			// only create requests for the specified observed properties
			if (observedProperties.contains(property)) {

				GetObservationRequest observationRequest =
						new GetObservationRequest(sensorOffering, property,
								responseFormat);

				String label =
						TimeSeriesUtil.getSeriesLabel(sensorOffering, property);

				SosDataRequest dataRequest =
						new SosDataRequest(label, service.getEndpoint(), observationRequest);

				dataRequests.add(dataRequest);
			}
		}

		return dataRequests;
	}


	/**
	 * Executes the current data requests
	 *
	 * - Updates all SOSDataRequest objects with appropriate time periods
	 *
	 * @param dataRequests
	 * @param monitor
	 * @throws InvocationTargetException, InterruptedException
	 */
	private void executeRequests(List<SosDataRequest> dataRequests,
			IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException
	{

		try {

			monitor.beginTask("Downloading sensor data preview...", dataRequests.size());

			for (SosDataRequest dataRequest : dataRequests) {

				try {

					/*
					 * Get data
					 */
					executeRequest(dataRequest);

				} catch (ExceptionReportException e) {
					log.warn("ExceptionReportException: " + e.getMessage());
				} catch (SocketTimeoutException e) {
					log.warn("Socket timeout: " + e.getMessage());
					throw new InvocationTargetException(e,
							"Socket timeout: " + e.getMessage());
				}

				if (monitor.isCanceled()) {
					throw new InterruptedException("The operation was cancelled");
				}

				monitor.worked(1);
			}

		} finally {
			monitor.done();
		}
	}

	/**
	 * Executes one request
	 *
	 * @param serviceUrl
	 * @param request
	 *
	 * @throws ExceptionReportException
	 */
	public SensorData executeRequest(SosDataRequest dataRequest)
			throws ExceptionReportException, SocketTimeoutException {

		GetObservationRequest request = dataRequest.getRequest();

		/*
		 * Add appropriate time intervals to request
		 */
		if (getPeriod() != null)
			addTimeInterval(request);
		
		System.out.println("Request: " + request.getGetQueryString());

		/*
		 * Get data
		 */
		SensorData sensorData =
				getObservationData(dataRequest.getServiceUrl(), request);

		/*
		 * Save variables
		 */

		VariablesHolder holder = VariablesHolder.getInstance();

		String offeringId = request.getSensorOffering().getGmlId();
		String property = request.getObservedProperty();

		holder.addVariables(offeringId, property, sensorData.getFields());

		return sensorData;
	}
	
	/**
	 * Add time interval to request
	 *
	 * @param request
	 */
	public static void addTimeInterval(GetObservationRequest request, Period period) {

		if (period != null) {

			SensorOffering sensorOffering = request.getSensorOffering();

			// Determine the earliest available time
			DateTime earliest =
					TimeSeriesUtil.getEarliestAvailableTime(sensorOffering);
			// Determine the latest available time
			DateTime latest =
					TimeSeriesUtil.getLatestAvailableTime(sensorOffering);

			// if there was no end time specified, we have to set one
			if (latest == null) {
				latest = getNow();
			}

			DateTime end = latest;

			/*
			 * Subtract the specified time period
			 */
			DateTime start = end.minus(period);

			/*
			 * If the start is before the earliest available time, just
			 * start from the earliest available data instead
			 */
			if (start.isBefore(earliest))
				start = earliest;

			/*
			 * NOTE: if period is null, we try and fetch all available data
			 */

			// remove all previous intervals
			request.clearIntervals();

			// finally, only add interval if there is no time issue
			if (end.isAfter(start)) {

				log.trace("Adding interval: start: " + earliest +
						"; end: " + latest);

				Interval interval = new Interval(start, end);
				request.addTimeInterval(TimeInterval.fromJoda(interval));
			}

		}
	}	

	/**
	 * Add time interval to request
	 *
	 * @param request
	 */
	private void addTimeInterval(GetObservationRequest request) {
		addTimeInterval(request, getPeriod());
	}

	/**
	 * Retrieves observations
	 *
	 * @param serviceUrl
	 * @param sensorOffering
	 * @param request
	 */
	private SensorData getObservationData(String serviceUrl,
			GetObservationRequest request) throws ExceptionReportException, SocketTimeoutException
	{

		try {

			SensorData sensorData =
					GetObservationCache.getInstance().getObservationData(serviceUrl, request);

			return sensorData;

		} catch (ExceptionReportException e) {

			String result = e.getMessage();

			StringWriter sw = new StringWriter();

//			try {
//
//				InputStream is = new ByteArrayInputStream(result.getBytes());
//				OWSExceptionReport exceptionReport =
//						new OWSExceptionReport(is).parse();
//
//				if (exceptionReport != null) {
//
//					for (OWSException exception : exceptionReport.getExceptions()) {
//						for (String text : exception.getExceptionText()) {
//
//							UIUtil.log(Activator.PLUGIN_ID, text);
//
//							sw.write(text);
//						}
//					}
//
//					throw new ExceptionReportException(sw.toString());
//				}
//
//				throw new ExceptionReportException(result);
//
//			} catch (XMLStreamException xmlException) {
//				log.warn("XML stream exception: " + xmlException.getMessage());
//			}

		}

		return null;
	}

	/**
	 * Returns the specified period (may be null)
	 *
	 * @return
	 */
	private Period getPeriod() {
		return period;
	}

	/**
	 * Returns what is considered to be the current time, or NOW
	 *
	 * If the NOW + duration is before the system time NOW, update NOW,
	 * otherwise return old value of NOW
	 *
	 * @return
	 */
	private static DateTime getNow() {
		if (now.plus(duration).isBefore(new DateTime())) {
			now = new DateTime();
		}
		return now;
	}

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.joda.time.DateTime;

import au.com.bytecode.opencsv.CSVWriter;

import com.iai.proteus.common.LatLon;
import com.iai.proteus.common.Util;
import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.FieldType;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.map.WorldWindUtils;
import com.iai.proteus.ui.UIUtil;


/**
 * Utility methods for data plotting and visualization
 *
 * @author Jakob Henriksson
 *
 */
public class TimeSeriesUtil {

	private static final Logger log = Logger.getLogger(TimeSeriesUtil.class);

	/**
	 * Returns a label for a series based on the given offering and property
	 *
	 * @param offering
	 * @param observedProperty
	 * @return
	 */
	public static String getSeriesLabel(SensorOffering offering,
			String observedProperty)
	{
		return offering.getGmlId() + " - " +
			Util.readableLocalURL(observedProperty);
	}

	/**
	 * Returns the latest time for which data is stated to be available from
	 * the offering that is queried by the given data request
	 *
	 * @param dataRequest
	 * @return
	 */
	public static DateTime getLatestAvailableTime(SosDataRequest dataRequest) {
		SensorOffering offering =
			dataRequest.getRequest().getSensorOffering();
		// if we have a known latest data point, use that
		if (offering.hasLatestDataPoint()) {
			return new DateTime(offering.getLatestDataPoint());
		} else {
			if (!offering.noEndTime())
				// return the latest time of the offering if available
				return new DateTime(offering.getEndTime());
		}
		// default; return the current system time by default
		return new DateTime();
	}

	/**
	 * Returns the latest time for which data is stated to be available from
	 * the offering that is queried by the given data request
	 *
	 * @param sensorOffering
	 * @return
	 */
	public static DateTime getLatestAvailableTime(SensorOffering sensorOffering) {
		// if we have a known latest data point, use that
		if (sensorOffering.hasLatestDataPoint()) {
			return new DateTime(sensorOffering.getLatestDataPoint());
		} else {
			// if there is an end time, use that
			if (!sensorOffering.noEndTime())
				// return the latest time of the offering if available
				return new DateTime(sensorOffering.getEndTime());
		}
		// default: there was no end time specified
		return null;
	}

	/**
	 * Returns the earliest time for which data is stated to be available from
	 * the offering that is queried by the given data request
	 *
	 * @param dataRequest
	 * @return
	 */
	public static DateTime getEarliestAvailableTime(SosDataRequest dataRequest) {
		SensorOffering offering =
			dataRequest.getRequest().getSensorOffering();
		Date start = offering.getStartTime();
		if (start != null)
			return new DateTime(start);
		return null;
	}

	/**
	 * Returns the earliest time for which data is stated to be available from
	 * the offering that is queried by the given data request
	 *
	 * @param sensorOffering
	 * @return
	 */
	public static DateTime getEarliestAvailableTime(SensorOffering sensorOffering) {
		Date start = sensorOffering.getStartTime();
		if (start != null)
			return new DateTime(start);
		return null;
	}


	/**
	 * Returns the best default variable to use for domain axis
	 *
	 * @param sensorData
	 * @return
	 */
	public static Field guessDefaultDomainVariable(SensorData sensorData) {

		List<Field> dates = sensorData.getFields(FieldType.TIMESTAMP);
		if (dates.size() > 0)
			return dates.get(0);

		// default
		return null;
	}


	/**
	 * Returns true if the field is NOT a candidate for a good range variable
	 *
	 * @param field
	 * @return
	 */
	public static boolean notRangeCandidate(Field field) {
		// TODO: put in a properties file
		String[] bad =
			new String[] { "sensor_id", "station_id",
				"latitude", "longitude", "date_time",
				"date", "time", "degree" };
		String name = field.getName();
		for (String b : bad) {
			if (name.contains(b))
				return true;
		}
		// default
		return false;
	}

	/**
	 * Attempts to find the field containing longitude information
	 *
	 * @param sensorData
	 * @return
	 */
	public static Field findLongitudeField(SensorData sensorData) {
		for (Field field : sensorData.getFields()) {
			if (field.getName().contains("longitude"))
				return field;
		}
		// default
		return null;
	}

	/**
	 * Attempts to find the field containing latitude information
	 *
	 * @param sensorData
	 * @return
	 */
	public static Field findLatitudeField(SensorData sensorData) {
		for (Field field : sensorData.getFields()) {
			if (field.getName().contains("latitude"))
				return field;
		}
		// default
		return null;
	}

	/**
	 * Attempts to find the field containing depth/elevation information
	 *
	 * @param sensorData
	 * @return
	 */
	public static Field findElevationField(SensorData sensorData) {
		for (Field field : sensorData.getFields()) {
			if (field.getName().contains("depth"))
				return field;
			if (field.getName().contains("elevation"))
				return field;
		}
		// default
		return null;
	}

	/**
	 * Create the makers that will populate the geo-location layer
	 *
	 * @param data
	 */
	public static Set<Position> findLatLongs(List<String[]> data) {

		Set<Position> locations = new HashSet<Position>();

		for (String[] row : data) {
			if (row.length == 4) {

				try {

					double lat = Double.parseDouble(row[1]);
					double lon = Double.parseDouble(row[2]);

					LatLon location = new LatLon(lat, lon);

					// only create markers for unique LAT-LONs
					if (!locations.contains(location)) {

						Position position = WorldWindUtils.getPosition(location);
						locations.add(position);
					}

				} catch (NumberFormatException e) {
					log.error("Number format exception: " + e.getMessage());
				}
			}
		}

		return locations;
	}
	
	/**
	 * Exports the data to a specified CSV file 
	 *
	 */	
	public static void exportDataToCSV(File file, SensorData sensorData) {
		try {

			FileWriter fw = new FileWriter(file);
			CSVWriter csvWriter = new CSVWriter(fw);

			// write headers
			List<Field> fields = sensorData.getFields();
			List<String> strFields = new ArrayList<String>();
			for (Field field : fields) {
				strFields.add(field.getName());
			}
			String[] arrFields =
					strFields.toArray(new String[strFields.size()]);
			csvWriter.writeNext(arrFields);

			// write data
			List<String[]> data = sensorData.getData();
			csvWriter.writeAll(data);

			csvWriter.flush();
			csvWriter.close();

			fw.close();

		} catch (IOException e) {
			log.error("IOException when exporting sensor data: " +
					e.getMessage());
		}
		
	}

	/**
	 * Exports the data to a CSV file selected by the user
	 *
	 */
	public static void exportDataToCSV(Shell shell, SensorData sensorData) {

		if (sensorData == null) {
			UIUtil.showInfoMessage("There is no data available to export");
			return;
		}

		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setText("Select File");
		fileDialog.setFilterExtensions(new String[] {
				"*.csv", "*.txt", "*.*" });
		fileDialog.setFilterNames(new String[] {
				"Comma-separated values files (*.csv)",
				"Text files (*.txt)",
				"All files (*.*)"});
		String selectedFile = fileDialog.open();

		if (selectedFile != null) {

			File file = new File(selectedFile);

			if (file.exists()) {

				MessageDialog dialog = UIUtil.getConfirmDialog(shell,
						"The selected file already exists",
						"Do you want to overwrite the existing file?");
				int result = dialog.open();
				// return and do nothing if the user cancels the action
				if (result != MessageDialog.OK) {
					return;
				}
			}

			exportDataToCSV(file, sensorData);
			
			MessageDialog.openInformation(shell,
					"Data exported", "The data was successfully exported.");
		}
	}
}

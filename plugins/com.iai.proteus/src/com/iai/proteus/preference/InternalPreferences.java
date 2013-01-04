package com.iai.proteus.preference;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/*
 * Preferences
 *
 * TODO: this object will be replaced by Bean objects that hold preferences
 */
public class InternalPreferences {

	public static boolean prefLayerGetObservationUnderOffering = true;

	public static boolean prefTimeSeriesCapabilitiesEndTime = false;

	public static Integer maxDefaultRangeVariables = 5;

	/**
	 * Constructor
	 */
	public InternalPreferences() {
	}

	/**
	 * Serializes preferences bean object to file
	 *
	 * @param file
	 * @throws FileNotFoundException
	 */
	public void encode(File file) throws FileNotFoundException {
		XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
				new FileOutputStream(file)));
		PreferencesTimeSeries bean = new PreferencesTimeSeries();
		bean.setMaxDefaultRangeVariables(10);
		e.writeObject(bean);
		e.close();
	}

	public static void main(String[] args) {
		try {
			new InternalPreferences().encode(new File("c:/Users/jhenriksson/test.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

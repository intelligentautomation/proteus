/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class BundleUtils {

	private static final Logger log = Logger.getLogger(BundleUtils.class);

	/**
	 * Reads contents from a bundle 
	 * 
	 * @param pluginId
	 * @param location
	 * @return
	 */
	public static String readBundleContents(String pluginId, String location) {

		try {

			URL url = new URL("platform:/plugin/" + 
					pluginId + "/" + location);
			InputStream inputStream = 
					url.openConnection().getInputStream();
			BufferedReader in = 
					new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;

			StringWriter sw = new StringWriter();
			while ((inputLine = in.readLine()) != null) {
				sw.write(inputLine);
			}

			in.close();

			return sw.toString();

		} catch (MalformedURLException e) {
			log.error("Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}

		return null;
	}

	/**
	 * Reads contents from a bundle 
	 * 
	 * @param pluginId
	 * @param location
	 * @return
	 */
	public static InputStream getInputStream(String pluginId, String location) {

		try {

			URL url = new URL("platform:/plugin/" + 
					pluginId + "/" + location);
			return url.openConnection().getInputStream();

		} catch (MalformedURLException e) {
			log.error("Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}

		return null;
	}       

}

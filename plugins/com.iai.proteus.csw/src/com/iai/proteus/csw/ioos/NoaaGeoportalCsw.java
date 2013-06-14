/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.csw.ioos;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.iai.proteus.common.BundleUtils;
import com.iai.proteus.common.Util;
import com.iai.proteus.csw.Activator;
import com.iai.proteus.model.services.Service;

/**
 * Utility class for searching for SOSs in a NOAA CSW 
 * 
 * @author Jakob Henriksson
 *
 */
public class NoaaGeoportalCsw {
	
	/**
	 * Returns the response from 
	 * 
	 * @return
	 * @throws IOException
	 */
	public Collection<Service> searchForSos() throws IOException {

		// load query template 
		String template =
				BundleUtils.readBundleContents(Activator.PLUGIN_ID,
						"resources/queries/cswQuery1.xml");
		System.out.println("TEMP: " + template);
		String query = template;
//		File file = new File("resources/templates/queryTemplate6.stg");
//		File file = new File("resources/templates/cswQuery1.stg");
//		String template = FileUtils.readFileToString(file);
		
//		STGroup group = new STGroupString(template);
//		ST st = group.getInstanceOf("query");
		
		// render the request 
//		String request = st.render();

		// service to query 
		// ?service=CSW&request=GetRecords&version=2.0.2&outputFormat=application/xml
		String service = "http://www.ngdc.noaa.gov/geoportal/csw";
		String params = "?service=CSW&request=GetRecords&version=2.0.2&outputFormat=application/xml";
//		String service = "http://geodiscover.cgdi.ca/wes/serviceManagerCSW/csw";

		String queryUrl = service + params;
		
		System.out.println("Query: " + query);
		// get the HTTP response 
		String response = Util.post(queryUrl, query);
//		String response = FileUtils.readFileToString(new File("/Users/b0kaj/Downloads/csw-response1.xml"));
		
		System.out.println("Response..." + response);
		
		// collect services 
		Collection<Service> services = 
				new EndpointParser().findEndpoints(response);
		
		return services;
	}
	

	public static void main(String[] args) throws IOException {
		new NoaaGeoportalCsw().searchForSos();
	}
	
}

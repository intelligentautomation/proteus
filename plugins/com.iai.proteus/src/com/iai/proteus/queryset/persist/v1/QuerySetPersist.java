/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset.persist.v1;

import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iai.proteus.Activator;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.map.WmsSavedMap;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.ui.queryset.Category;
import com.iai.proteus.ui.queryset.ObservedPropertiesHolder;
import com.iai.proteus.ui.queryset.ObservedProperty;
import com.iai.proteus.ui.queryset.QuerySetTab;

/**
 * Support for reading and writing query sets to/from disk 
 * 
 * @author Jakob Henriksson
 *
 */
public class QuerySetPersist {

	private static final Logger log = Logger.getLogger(QuerySetPersist.class);

	// name of folder where query sets are stored 
	public static String folderQuerySets = "querysets";

	// extension for serialized query set files  
	public static String querySetExtension = "json";
	
	// format for storing time stamps
	public static SimpleDateFormat format = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	
	// version of serialization 
	public static String version = "1.0";
	
	/**
	 * Returns the folder where the query sets are stored 
	 * 
	 * @return
	 */
	public static File getStorageLocation() {
		
		// load stored query sets 
		File parent = Activator.stateLocation.toFile();
		File folder = new File(parent, folderQuerySets);
		
		// ensure the folder exists
		if (!folder.exists())
			folder.mkdir();
		
		return folder;
	}
	
	/**
	 * Writes a query set
	 * 
	 * @param querySet
	 */
	public static QuerySet write(QuerySetTab querySet) {

		try {
			
			JSONObject root = new JSONObject();
			
			// metadata
			root.put(Keys.META_VERSION.toString(), version);
			root.put(Keys.META_UUID.toString(), querySet.getUuid());
			root.put(Keys.META_TITLE.toString(), querySet.getQuerySetName());
			root.put(Keys.META_DATE_CREATED.toString(), format.format(new Date()));
			
			// SOS
			JSONObject sos = new JSONObject();

			// SOS - services 
			JSONArray sosServices = new JSONArray();
			for (Service service : querySet.getServices(ServiceType.SOS)) {
				JSONObject sosService = new JSONObject();
				sosService.put(Keys.SERVICE_ENDPOINT.toString(), service.getEndpoint());
				sosService.put(Keys.SERVICE_TITLE.toString(), service.getName());
				sosService.put(Keys.SERVICE_ACTIVE.toString(), service.isActive());
				sosServices.put(sosService);
			}
			sos.put(Keys.SOS_SERVICES.toString(), sosServices);
			
			// SOS - bounding box 
			JSONArray boundingBox = new JSONArray();		
			Sector sector = querySet.getSector();
			if (sector != null) {
				for (double d : querySet.getSector().asDegreesArray()) {
					boundingBox.put(d);
				}
			} 
			sos.put(Keys.SOS_BBOX.toString(), boundingBox);
			
			// SOS - observed properties
			JSONArray observedProperties = new JSONArray();
			ObservedPropertiesHolder opHolder = 
					querySet.getObservedPropertiesHolder();
			for (Category cat : opHolder.getCategories()) {
				for (ObservedProperty op : cat.getObservedProperties()) {
					if (op.isChecked()) {
						observedProperties.put(op.getObservedProperty());
					}
				}
			}
			sos.put("observedProperties", observedProperties);
			
			root.put(Keys.SECTION_SOS.toString(), sos);
			
			// WMS
			
			// WMS - maps
			JSONArray wmsMaps = new JSONArray();
			for (MapLayer map : querySet.getSavedMaps()) {
				if (map instanceof WmsSavedMap) {
					JSONObject wmsMap = new JSONObject();
					WmsSavedMap savedMap = (WmsSavedMap) map;
					wmsMap.put(Keys.SERVICE_ENDPOINT.toString(), savedMap.getServiceEndpoint());
					wmsMap.put(Keys.SERVICE_ACTIVE.toString(), savedMap.isActive());
					wmsMap.put(Keys.MAP_NAME.toString(), savedMap.getName());
					wmsMap.put(Keys.MAP_TITLE.toString(), savedMap.getWmsLayerTitle());
					wmsMap.put(Keys.MAP_NOTES.toString(), savedMap.getNotes());
					wmsMaps.put(wmsMap);
				}
			}
			
			root.put(Keys.SECTION_WMS.toString(), wmsMaps);
			
			
			// write JSON 

			try {

				// get the location
				File folder = getStorageLocation();
				// create the file 
				// TODO: make sure this file does not exist 
				File file = new File(folder,
						querySet.getUuid() + 
//						querySet.getQuerySetName() + 
						"." + querySetExtension);
				FileWriter fw = new FileWriter(file);
				
				// pretty format with tab-size of 2
				String json = root.toString(2);
				if (json != null) {
					fw.write(json);
					fw.flush();
					fw.close();
					
					// do not need to parse entry model object, 
					// just need: 1) UUID, 2) File location
					QuerySet qs = new QuerySet();
					qs.setUuid(querySet.getUuid());
					qs.setFile(file);
					
					// return the query set model object that contains 
					// necessary information
					return qs;
					
				} else {
					log.error("Could not generate JSON");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
		}

		// error 
		return null;
	}
	
	/**
	 * Reads a query set
	 * 
	 * @param querySet
	 */
	public static QuerySet read(File file) {

		QuerySet qs = new QuerySet();
		
		try {

			JSONObject jsonRoot = 
					new JSONObject(FileUtils.readFileToString(file));
			
			// check version 
			String strVersion = jsonRoot.getString(Keys.META_VERSION.toString());
			if (!(strVersion.equals(version))) {
				log.error("The version did not match, " + 
						"expected version " + version);
				return null;
			}
			
			// set version
			qs.version = version;
			// set UUID
			qs.uuid = jsonRoot.getString(Keys.META_UUID.toString());
			// set title
			qs.title = jsonRoot.getString(Keys.META_TITLE.toString());
			// set dateCreated 
			String strDateCreated = jsonRoot.getString(Keys.META_DATE_CREATED.toString());
			qs.dateCreated = format.parse(strDateCreated);
			
			// SOS
			JSONObject jsonSos = jsonRoot.getJSONObject(Keys.SECTION_SOS.toString());
			
			// services
			JSONArray sosServices = 
					jsonSos.getJSONArray(Keys.SOS_SERVICES.toString());
			for (int i = 0; i < sosServices.length(); i++) {
				JSONObject objSosService = sosServices.getJSONObject(i);
				
				// create and populate the object
				QuerySet.SosService sosService = qs.new SosService();
				sosService.endpoint = 
						objSosService.getString(Keys.SERVICE_ENDPOINT.toString());
				sosService.title = 
						objSosService.getString(Keys.SERVICE_TITLE.toString());
				sosService.active = 
						objSosService.getBoolean(Keys.SERVICE_ACTIVE.toString());
				
				// add to services
				qs.sectionSos.sosServices.add(sosService);
			}
			
			// bounding box
			JSONArray arrBbox = jsonSos.getJSONArray(Keys.SOS_BBOX.toString());
			if (arrBbox.length() == 4) {
				qs.sectionSos.boundingBox.latL = arrBbox.getDouble(0);
				qs.sectionSos.boundingBox.latU = arrBbox.getDouble(1);
				qs.sectionSos.boundingBox.lonL = arrBbox.getDouble(2);
				qs.sectionSos.boundingBox.lonU = arrBbox.getDouble(3);
				System.out.println("Yes...");
			}
			
			// observed properties 
			JSONArray arrOp = 
					jsonSos.getJSONArray(Keys.SOS_OBSERVED_PROPERTIES.toString());
			for (int i = 0; i < arrOp.length(); i++) {
				// create observed property object 
				QuerySet.SosObservedProperty op = qs.new SosObservedProperty();
				op.observedProperty = arrOp.getString(i);
				
				// add to observed properties 
				qs.sectionSos.observedProperties.add(op);
			}
			
			// WMS
			JSONArray jsonWms = jsonRoot.getJSONArray(Keys.SECTION_WMS.toString());
			for (int i = 0; i < jsonWms.length(); i++) {
				JSONObject obj = (JSONObject) jsonWms.get(i);
				
				// create and populate the map object
				QuerySet.WmsSavedMap map = qs.new WmsSavedMap(); 
				map.endpoint = obj.getString(Keys.SERVICE_ENDPOINT.toString());
				map.active = obj.getBoolean(Keys.SERVICE_ACTIVE.toString());
				map.name = obj.getString(Keys.MAP_NAME.toString());
				map.title = obj.getString(Keys.MAP_TITLE.toString());
				map.notes = obj.getString(Keys.MAP_NOTES.toString());
				
				// add the map
				qs.sectionWms.maps.add(map);
			}
			
			return qs;

		} catch (IOException e) {
			log.error("Error reading query set: " + e.getMessage());
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
		} catch (ParseException e) {
			log.error("Error parsing creation date: " + e.getMessage());
		}
		
		// error
		return null;

	}	
	
	/**
	 * Keys for writing/reading JSON
	 * 
	 * @author Jakob Henriksson
	 *
	 */
	enum Keys {
		
		META_VERSION("version"),
		META_UUID("uuid"),
		META_TITLE("title"),
		META_DATE_CREATED("dateCreated"),
		
		SERVICE_ENDPOINT("endpoint"),
		SERVICE_TITLE("title"),
		SERVICE_ACTIVE("active"),
		
		MAP_NAME("name"),
		MAP_TITLE("title"),
		MAP_NOTES("notes"), 
		
		SECTION_SOS("sos"),
		SECTION_WMS("wms"),
		
		SOS_SERVICES("services"),
		SOS_BBOX("bbox"),
		SOS_OBSERVED_PROPERTIES("observedProperties");
		
		String tag;
		
		/**
		 * Constructor 
		 * 
		 * @param tag
		 */
		private Keys(String tag) {
			this.tag = tag;
		}
		
		@Override
		public String toString() {
			return tag;
		}
	}
}

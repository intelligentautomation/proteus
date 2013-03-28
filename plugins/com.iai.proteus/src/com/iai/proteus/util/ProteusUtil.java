/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.iai.proteus.PreferenceConstants;
import com.iai.proteus.common.Util;
import com.iai.proteus.communityhub.apiv1.Alert;
import com.iai.proteus.communityhub.apiv1.AlertsResponse;
import com.iai.proteus.communityhub.apiv1.Endpoints;
import com.iai.proteus.communityhub.apiv1.Group;
import com.iai.proteus.communityhub.apiv1.GroupsResponse;
import com.iai.proteus.model.map.MapLayer;

/**
 * Utility methods 
 * 
 * @author Jakob Henriksson 
 *
 */
public class ProteusUtil {
	
	private static final Logger log = Logger.getLogger(ProteusUtil.class);
	
	// cache for feeds 
//	private static FeedCache feedCache = new FeedCache();
	
	/**
	 * Returns the Community Hub end point from preferences 
	 * 
	 * @return
	 */
	public static String getCommunityHubEndpoint(IPreferenceStore store) {
		
		String serviceAddress =
				store.getString(PreferenceConstants.prefCommunityHub);

		// construct appropriate service call end-point
		serviceAddress = serviceAddress.trim();

		if (serviceAddress.endsWith("/"))
			serviceAddress = serviceAddress.substring(0,
					serviceAddress.length() - 1);
		
		return serviceAddress;
	}
	
	/**
	 * Returns the groups from the Community Hub 
	 * 
	 * @param store
	 */
	public static Collection<Group> getCommunityGroups(IPreferenceStore store) 
		throws MalformedURLException, SocketTimeoutException, 
		JsonSyntaxException, IOException 
	{

		String serviceAddress = getCommunityHubEndpoint(store);

		int timeoutConnection =
				store.getInt(PreferenceConstants.prefConnectionTimeout);
		int timeoutRead =
				store.getInt(PreferenceConstants.prefConnectionTimeout);

		// construct service endpoint 
		serviceAddress += Endpoints.groups();

		// issue GET request to API
		String json = Util.get(serviceAddress, 
				timeoutConnection, timeoutRead);

		// get and convert response 
		GroupsResponse gson = 
				new Gson().fromJson(json, GroupsResponse.class);

		// verify result and then return the groups 
		if (gson.getStatus().equals("OK"))
			return gson.getGroups(); 

		// default 
		return new ArrayList<Group>();
	}
	
	/**
	 * Returns the groups from the Community Hub 
	 * 
	 * @param store
	 * @param group
	 */
	public static Collection<Alert> getAlerts(IPreferenceStore store, Group group) 
		throws MalformedURLException, SocketTimeoutException, 
		JsonSyntaxException, IOException 
	{
		
		if (group == null) 
			throw new IllegalArgumentException("Group cannot be null");

		String serviceAddress = getCommunityHubEndpoint(store);

		int timeoutConnection =
				store.getInt(PreferenceConstants.prefConnectionTimeout);
		int timeoutRead =
				store.getInt(PreferenceConstants.prefConnectionTimeout);

		// construct service endpoint
		serviceAddress += Endpoints.alerts(group);

		// issue GET request to API
		String json = Util.get(serviceAddress, 
				timeoutConnection, timeoutRead);

		// get and convert response 
		AlertsResponse gson = 
				new Gson().fromJson(json, AlertsResponse.class);

		// verify result and then return the groups 
		if (gson.getStatus().equals("OK"))
			return gson.getAlerts(); 

		// default 
		return new ArrayList<Alert>();
	}
	
	/**
	 * Moves a given map higher in the ordering of a list of maps 
	 * 
	 * @param maps
	 * @param map
	 * @return
	 */
	public static boolean moveMapLayerHigher(List<MapLayer> maps, MapLayer map) {
		int idx = maps.indexOf(map);
		if (idx != -1) {
			maps.remove(idx);
			int newIdx = idx - 1 < 0 ? 0 : idx - 1;
			maps.add(newIdx, map);
			log.info("Moved map to index " + newIdx);
			return true;
		}
		return false;
	}

}

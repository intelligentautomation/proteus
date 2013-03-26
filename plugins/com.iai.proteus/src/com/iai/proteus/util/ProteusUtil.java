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

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.joda.time.Period;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.iai.proteus.PreferenceConstants;
import com.iai.proteus.common.Util;
import com.iai.proteus.communityhub.apiv1.Alert;
import com.iai.proteus.communityhub.apiv1.AlertsResponse;
import com.iai.proteus.communityhub.apiv1.Group;
import com.iai.proteus.communityhub.apiv1.GroupsResponse;

public class ProteusUtil {
	
	private static final Logger log = Logger.getLogger(ProteusUtil.class);
	
	// cache for feeds 
	private static FeedCache feedCache = new FeedCache();
	
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

		// TODO: make this generic, don't hard code here 
		serviceAddress += "/apiv1/groups/";

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

		// TODO: make this generic, don't hard code here 
		serviceAddress += "/apiv1/alerts/" + group.getId() + "/";

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
	 * Fetches an alert feed for a given group with default cache threshold period
	 * 
	 * @param store
	 * @param groupId
	 * @return
	 */
	public static String getAlertFeed(IPreferenceStore store, int groupId) {
		// default: 5 minutes 
		return getAlertFeed(store, groupId, new Period(0, 5, 0, 0));
	}
	
	/**
	 * Fetches an alert feed for a given group and cache threshold period  
	 * 
	 * @param store
	 * @param groupId
	 * @param period 
	 * @return
	 */
	public static String getAlertFeed(IPreferenceStore store, int groupId, Period period) {
		String serviceAddress = getCommunityHubEndpoint(store);
		String feedAddress = serviceAddress += "/community/feed/" + groupId;
		return feedCache.get(feedAddress, period);
	}	
	
	public static void main(String[] args) {
		
		String serviceAddress = "http://localhost:8080/communityhub/apiv1/alerts/1/";
		
		try {
			
			int timeoutConnection = 1000;
			int timeoutRead = 1000;

			// issue GET request
			String json = Util.get(serviceAddress, 
					timeoutConnection, timeoutRead);
			
			AlertsResponse gson = 
					new Gson().fromJson(json, AlertsResponse.class);
			
			System.out.println("V: " + gson.getVersion());
			for (Alert g : gson.getAlerts()) {
				System.out.println("G: " + g.getId() + "; " + g.getType());
			}

//			HubAddServiceResponse data =
//					new Gson().fromJson(json, HubAddServiceResponse.class);
			
//			return data.getMessage();

		} catch (MalformedURLException e) {
			log.error("Malformed URL exception: " + e.getMessage());
		} catch (SocketTimeoutException e) {
			log.error("Socket timeout exception: " + e.getMessage());
		} catch (JsonSyntaxException e) {
			log.error("JSON syntax exception: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}						
	}

}

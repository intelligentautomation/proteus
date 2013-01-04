package com.iai.proteus.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.iai.proteus.common.Util;

/**
 * Simple cache for alert feeds 
 * 
 * @author Jakob Henriksson
 *
 */
public class FeedCache {
	
	private static final Logger log = Logger.getLogger(ProteusUtil.class);
	
	private Map<String, Date> cacheTimestamps;
	private Map<String, String> cacheFeeds;
	
	/**
	 * Constructor 
	 * 
	 */
	public FeedCache() {
		cacheTimestamps = new HashMap<String, Date>();
		cacheFeeds = new HashMap<String, String>();
	}
	
	/**
	 * Get feed with default caching threshold 
	 * 
	 * @param service
	 * @return
	 */
	public String get(String service) {
		// default period: 5 minutes
		return get(service, new Period(0, 5, 0, 0));
	}
	
	/**
	 * Get feed with given caching threshold period 
	 * 
	 * @param service
	 * @param period
	 * @return
	 */
	public String get(String service, Period period) {
		Date now = new Date();
		Date time = cacheTimestamps.get(service);
		if (time != null) {
			DateTime dt = new DateTime(time);
			// not enough time has elapsed, return cached result 
			if (dt.plus(period).isAfterNow()) {
				return cacheFeeds.get(service);
			} 
		} 
		
		// get from live service 
		try {
			
			String feed = Util.get(service);
			if (feed != null) {
				// add to cache
				cacheTimestamps.put(service, now);
				cacheFeeds.put(service, feed);
				return feed;
			}
			
		} catch (SocketTimeoutException e) {
			log.error("SocketTimeoutException: " + e.getMessage());
		} catch (MalformedURLException e) {
			log.error("MalformedURLException: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		} 		
		
		// default
		return null;
	}
	
}

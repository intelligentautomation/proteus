/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub.apiv1;

/**
 * Computes relevant endpoints 
 * 
 * @author Jakob Henriksson
 *
 */
public class Endpoints {
	
	/**
	 * Ping 
	 * 
	 * @return
	 */
	public static String ping() {
		return "/apiv1/ping/";
	}
	
	/**
	 * For retrieving groups 
	 * 
	 * @return
	 */
	public static String groups() {
		return "/apiv1/groups/";
	}
	
	/**
	 * For retrieving alerts for a given group 
	 * 
	 * @param group
	 * @return
	 */
	public static String alerts(Group group) {
		return String.format("/apiv1/alerts/%d/", group.getId());
	}

}

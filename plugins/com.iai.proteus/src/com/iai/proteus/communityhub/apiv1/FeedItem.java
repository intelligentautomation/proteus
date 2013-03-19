/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub.apiv1;

import javaxt.rss.Item;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Model holder for alert feed items 
 * 
 * @author Jakob Henriksson 
 *
 */
public class FeedItem implements IAdaptable {

	private Item item;
	
	private FeedItemPropertySource property;
	
	/**
	 * Constructor 
	 * 
	 * @param item
	 */
	public FeedItem(Item item) {
		this.item = item;
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(Item item) {
		this.item = item;
	}

	/**
	 * Adapter
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		
		if (adapter == IPropertySource.class) {
			if (property == null) {
				// cache the source 
				property = new FeedItemPropertySource(item);
			}
			return property;
		}
		// default 
		return null;
	}		
}

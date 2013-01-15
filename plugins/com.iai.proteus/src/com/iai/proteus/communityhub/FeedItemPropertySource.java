/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.communityhub;

import javaxt.rss.Item;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Property provider for Source objects 
 * 
 * @author Jakob Henriksson
 *
 */
public class FeedItemPropertySource implements IPropertySource {
	
	private final String CAT_ITEM = "Alert item";
	
	private final String PROP_ITEM_TITLE = "item_title";
	private final String PROP_ITEM_DATE = "item_date";
	private final String PROP_ITEM_LINK = "item_link";
	private final String PROP_ITEM_LOCATION = "item_location";
	
	private Item item; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public FeedItemPropertySource() {
		
	}

	/**
	 * Constructor 
	 * 
	 * @param item
	 */
	public FeedItemPropertySource(Item item) {
		this.item = item;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		PropertyDescriptor itemTitle = 
				new TextPropertyDescriptor(PROP_ITEM_TITLE, "Title");
		itemTitle.setCategory(CAT_ITEM);
		
		PropertyDescriptor itemDate = 
				new TextPropertyDescriptor(PROP_ITEM_DATE, "Date");
		itemDate.setCategory(CAT_ITEM);

		PropertyDescriptor itemlink = 
				new TextPropertyDescriptor(PROP_ITEM_LINK, "Link");
		itemlink.setCategory(CAT_ITEM);
		
		PropertyDescriptor itemLocation = 
				new TextPropertyDescriptor(PROP_ITEM_LOCATION, "Location");
		itemLocation.setCategory(CAT_ITEM);
		
		return new IPropertyDescriptor[] {
				itemTitle, 
				itemDate, 
				itemlink,
				itemLocation, 
		};
	}
	
	@Override
	public Object getPropertyValue(Object id) {
		
		if (item != null) {
			if (id.equals(PROP_ITEM_TITLE)) {
				return item.getTitle();
			} else if (id.equals(PROP_ITEM_DATE)) {
				return item.getDate();
			} else if (id.equals(PROP_ITEM_LINK)) {
				return item.getLink();
			} else if (id.equals(PROP_ITEM_LOCATION)) {
				return item.getLocation();
			}
		}
		
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		
	}

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.iai.proteus.common.Labeling;
import com.iai.proteus.ui.queryset.Category;
import com.iai.proteus.ui.queryset.ObservedProperty;

/**
 * Viewer filter for observed properties 
 * 
 * @author Jakob Henriksson 
 *
 */
public class ObservedPropertiesFilter extends ViewerFilter {
	
	private String searchString;
	
	/**
	 * Sets the search string 
	 * 
	 * @param s
	 */
	public void setSearchText(String s) {
		// search must be a substring of the existing value
		this.searchString = ".*" + s + ".*";
	}	

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		if (element instanceof Category) {
			return matchesCategory((Category) element);
		}
		else if (element instanceof ObservedProperty) {
			return matchesObservedProperty((ObservedProperty) element);
		}
		// default 
		return false;
	}
	
	/**
	 * Returns true if the observed property is matched, false otherwise 
	 * 
	 * @param op
	 * @return
	 */
	private boolean matchesObservedProperty(ObservedProperty op) {
		String property = op.getObservedProperty();
		if (property.matches(searchString) || 
				Labeling.labelProperty(property).matches(searchString))
			return true;
		// default
		return false;
	}
	
	/**
	 * Returns true if the category matches, or one it its children
	 * (observed properties), false otherwise  
	 * 
	 * @param category
	 * @return
	 */
	private boolean matchesCategory(Category category) {
		// return true if the category matches
		if (category.getName().matches(searchString))
			return true;
		// but also return true if the category contains a property that
		// matches
		boolean match = false;
		for (ObservedProperty op : category.getObservedProperties()) {
			match = matchesObservedProperty(op);
			if (match)
				break;
		}
		// default
		return match;
	}

}

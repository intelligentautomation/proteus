/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui.queryset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.iai.proteus.queryset.FacetData;

public class ObservedPropertiesHolder {

	/*
	 * Holds the actual structure of the observed properties as it will
	 * be displayed in the UI
	 */
	private List<Category> observedProperties;

	/*
	 * Specification of how to organize the observed properties. Each map
	 * key maps to a set of regular expressions. An observed property that
	 * matches a given regular expression will be categorized according
	 * to that matching category (key)
	 */
	private Map<String, List<String>> categories;

	/*
	 * Name of a miscellaneous category
	 */
	private final static String categoryOther = "Other";

	/**
	 * Constructor
	 *
	 */
	public ObservedPropertiesHolder() {

		observedProperties = new ArrayList<Category>();
		categories = new HashMap<String, List<String>>();

		/*
		 * Create categorization specification
		 */
		categories.put("Weather", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("(.)*Wind(.)*");
				add("(.)*wind(.)*");
				add("(.)*gust(.)*");
				add("(.)*Barometric(.)*");
				add("(.)*Precipitation(.)*");
				add("(.)*Rain(.)*");
				add("(.)*Solar(.)*");
			}
		});

		categories.put("Climate", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("(.)*Temp(.)*");
				add("(.)*temp(.)*");
				add("(.)*Air(.)*");
				add("(.)*air(.)*");
				add("(.)*atmosphere(.)*");
				add("(.)*humidity(.)*");
			}
		});

		categories.put("Water", new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("(.)*Water(.)*");
				add("(.)*water(.)*");
				add("(.)*Salinity(.)*");
				add("(.)*Sea(.)*");
				add("(.)*sea(.)*");
				add("(.)*Wave(.)*");
				add("(.)*wave(.)*");
				add("(.)*Swell(.)*");
				add("(.)*swell(.)*");
				add("(.)*Currents(.)*");
				add("(.)*currents(.)*");
			}
		});
	}

	/**
	 * Sets the observed properties
	 *
	 * @param facetData
	 */
	public void setObservedProperties(FacetData facetData) {
		observedProperties =
				categorize(new ArrayList<String>(
						facetData.getObservationProperties().keySet()));
	}

	/**
	 * Returns the observed properties, organized by categories
	 *
	 * @return
	 */
	public List<Category> getObserverPropertes() {
		return observedProperties;
	}

	/**
	 * Organizes the observed properties
	 *
	 * @param collection
	 * @return
	 */
	private List<Category> categorize(List<String> collection) {

		Map<String, List<ObservedProperty>> structure =
				new HashMap<String, List<ObservedProperty>>();

		// go through all the items in the collection
		for (String property : collection) {

			if (property == null)
				continue;

			property = property.trim();

			if (property.equals(""))
				continue;

			boolean matched = false;
			boolean next = false;

			// go though all the categories to find if we should
			// bin the property somewhere
			for (String category : categories.keySet()) {

				// each category has a list of regular expressions
				// that defines what goes into the category
				List<String> regexps = categories.get(category);
				for (String regexp : regexps) {

					// if there is a match, bin the property appropriately
					boolean match =
							Pattern.matches(regexp, property);
					if (match) {

						matched = true;

						if (structure.containsKey(category)) {
							// add property to an existing list for the
							// category
							structure.get(category).add(new ObservedProperty(property));

						} else {
							// start a new list for this category
							List<ObservedProperty> newList =
									new ArrayList<ObservedProperty>();
							newList.add(new ObservedProperty(property));
							structure.put(category, newList);
						}

						// indicate that we should go to the next property
						next = true;
						break;
					}
				}

				// stop looking through categories and go to the next
				// property
				if (next)
					break;
			}

			// if there was no match, add to the "Other" category
			if (!matched) {
				if (structure.containsKey(categoryOther)) {
					structure.get(categoryOther).add(new ObservedProperty(property));
				} else {
					List<ObservedProperty> newList =
							new ArrayList<ObservedProperty>();
					newList.add(new ObservedProperty(property));
					structure.put(categoryOther, newList);
				}
			}
		}

		// construct the hierarchy of categories with their values
		List<Category> categories = new ArrayList<Category>();

		for (String categoryName : structure.keySet()) {
			Category category = new Category(categoryName);
			category.setObservedProperties(structure.get(categoryName));
			categories.add(category);
		}

		// return the categories and their structure we came up with
		return categories;
	}

}

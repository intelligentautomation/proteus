/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import java.util.Collection;

import com.iai.proteus.queryset.ui.SensorOfferingItem;

public interface QuerySetContributor {

	public Collection<SensorOfferingItem> getSensorOfferingsContribution();
	public FacetData getObservedPropertiesContribution();

}

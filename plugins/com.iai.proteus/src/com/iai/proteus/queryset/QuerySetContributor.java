/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset;

import java.util.Collection;

import com.iai.proteus.ui.queryset.SensorOfferingItem;

public interface QuerySetContributor {

	public Collection<SensorOfferingItem> getSensorOfferingsContribution();
	public FacetData getObservedPropertiesContribution();

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset.ui;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProvider implements ITreeContentProvider {

	Object[] EMPTY_ARRAY = new Object[0];

	@Override
    public Object[] getChildren(Object parent) {

		// handles collections 
		if (parent instanceof Collection<?>) {
			return ((Collection<?>) parent).toArray();
		} 
		// sensor offerings
		else if (parent instanceof SensorOfferingsHolder) {
			return ((SensorOfferingsHolder) parent).getSensorOfferings().toArray();
		}
		// observed properties
		else if (parent instanceof ObservedPropertiesHolder) {
			ObservedPropertiesHolder holder =
					(ObservedPropertiesHolder) parent;
			return holder.getObserverPropertes().toArray();
		} else if (parent instanceof Category) {
			Category category = (Category) parent;
			return category.getObservedProperties().toArray();
		}
		// formats
		else if (parent instanceof AvailableFormatsHolder) {
			return ((AvailableFormatsHolder) parent).getAvailableFormats().toArray();
		}

        return EMPTY_ARRAY;
    }

	@Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

	@Override
    public Object getParent(Object element) {
        return null;
    }

	@Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

	@Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

	@Override
	public void dispose() {
	}
}

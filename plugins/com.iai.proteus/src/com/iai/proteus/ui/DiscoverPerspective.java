/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.iai.proteus.views.DataPreviewView;
import com.iai.proteus.views.DataTableView;
import com.iai.proteus.views.DiscoverView;
import com.iai.proteus.views.WorldWindView;

/**
 * Perspective for sensor discovery 
 * 
 * @author Jakob Henriksson
 *
 */
public class DiscoverPerspective implements IPerspectiveFactory {

	public static final String ID =
		"com.iai.proteus.ui.perspective.discover"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {

		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		/*
		 * Determines if the views can be moved around or not
		 */
		layout.setFixed(true);

        /*
         * Discover
         */
		layout.addStandaloneView(DiscoverView.ID, false,
				IPageLayout.LEFT, 0.4f, editorArea);

        /*
         * Map
         */
		layout.addStandaloneView(WorldWindView.ID, false,
				IPageLayout.RIGHT, 0.6f, editorArea);

        /*
         * Time series etc.
         */
        IFolderLayout bottomRight = layout.createFolder("bottom-right",
        		IPageLayout.BOTTOM, 0.6f, WorldWindView.ID);
        bottomRight.addView(IPageLayout.ID_PROP_SHEET);
        bottomRight.addView(DataPreviewView.ID);
        bottomRight.addView(DataTableView.ID);
        
	}
}

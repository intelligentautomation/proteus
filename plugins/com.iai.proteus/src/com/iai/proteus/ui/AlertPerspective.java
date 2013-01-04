package com.iai.proteus.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.iai.proteus.views.AlertFeedView;
import com.iai.proteus.views.CommunityGroupView;
import com.iai.proteus.views.WorldWindView;

public class AlertPerspective implements IPerspectiveFactory {

	public static final String ID =
		"com.iai.proteus.ui.perspective.alert"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {

		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		/*
		 * Determines if the views can be moved around or not
		 */
		layout.setFixed(true);

        /*
         * Groups
         */
		layout.addStandaloneView(CommunityGroupView.ID, false,
				IPageLayout.LEFT, 0.3f, editorArea);
		
		/*
		 * Feed (alerts)
		 */
		layout.addStandaloneView(AlertFeedView.ID, false, 
				IPageLayout.BOTTOM, 0.3f, CommunityGroupView.ID);

        /*
         * Map
         */
		layout.addStandaloneView(WorldWindView.ID, false,
				IPageLayout.RIGHT, 0.6f, editorArea);

        /*
         * Property sheet etc.
         */
        IFolderLayout bottomRight = layout.createFolder("bottom-right",
        		IPageLayout.BOTTOM, 0.8f, WorldWindView.ID);
        bottomRight.addView(IPageLayout.ID_PROP_SHEET);

	}
}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.iai.proteus.common.event.EventNotifier;
import com.iai.proteus.common.event.EventType;
import com.iai.proteus.ui.AlertPerspective;
import com.iai.proteus.ui.DiscoverPerspective;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
    	
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1200, 860));

        configurer.setShowCoolBar(false);
        // enable the status bar
        configurer.setShowStatusLine(true);
        // enable progress indicator in the status bar
        configurer.setShowProgressIndicator(true);
        configurer.setShowPerspectiveBar(true);

        configurer.setTitle("Proteus"); //$NON-NLS-1$
    }
    
    @Override
    public void postWindowOpen() {
    	
		final IWorkbenchWindow workbenchWindow = 
				PlatformUI.getWorkbench().getActiveWorkbenchWindow();	
		
		final EventNotifier notifier = EventNotifier.getInstance();
		
		workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspectiveDescriptor) {
				super.perspectiveActivated(page, perspectiveDescriptor);
				
				if (perspectiveDescriptor.getId().equals(DiscoverPerspective.ID)) {
					// fire event 
					notifier.fireEvent(null, EventType.MAP_TOGGLE_GLOBE_TYPE, "globe");
				} else if (perspectiveDescriptor.getId().equals(AlertPerspective.ID)) {
					// fire event 
					notifier.fireEvent(null, EventType.MAP_TOGGLE_GLOBE_TYPE, "flat");
				} 
			}
		});    	    	
    }
}

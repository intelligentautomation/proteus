/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.ui.DiscoverPerspective;
import com.iai.proteus.util.Startup;
import com.iai.proteus.views.DiscoverView;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    /**
     * Returns the perspective to start with
     */
	public String getInitialWindowPerspectiveId() {
		return DiscoverPerspective.ID;
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		// false = do not save the window configuration and restore on startup
		configurer.setSaveAndRestore(false);
	}
	
	@Override
	public void preStartup() {
		
		/*
		 * Create default setup
		 */
//		if (!Startup.workspaceFileExists()) {
//			Startup.createDefaultSetup();
//		}

		/*
		 * Create a default service if none exists
		 */
		if (!Startup.servicesFileExists()) {
			Service ndbc = new Service(ServiceType.SOS);
			ndbc.setEndpoint("http://sdf.ndbc.noaa.gov/sos/server.php");
			ndbc.setName("National Data Buoy Center");
			ServiceRoot.getInstance().addService(ndbc);
		}

		/*
		 * Load cached capabilities documents
		 */
		Startup.loadCapabilities();

		/*
		 * Load the services
		 */
		Startup.loadServices();
	}

	@Override
	public void postShutdown() {

		/*
		 * Saves the services
		 */
		Startup.saveServices();

		/*
		 * Write source (Capabilities) caches
		 */
		Startup.storeCapabilities();
//		Startup.writeSourcesCache();
	}

	@Override
	public void postStartup() {
		// manually remove pages from the preferences
		PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
		// help
		pm.remove("org.eclipse.help.ui.browsersPreferencePage");
		// security
		pm.remove("org.eclipse.equinox.security.ui.category");
		// install software
		pm.remove("org.eclipse.equinox.internal.p2.ui.sdk.ProvisioningPreferencePage");
		
		// set default focus on the discover view 
		IWorkbenchPage page = 
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IViewReference viewReference = 
				page.findViewReference(DiscoverView.ID);
		if (viewReference != null) {
			final IViewPart view = viewReference.getView(true);
			if (view != null)
				view.setFocus();
		}
	}

}

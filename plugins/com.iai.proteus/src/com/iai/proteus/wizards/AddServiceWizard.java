/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

import com.iai.proteus.Activator;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.ui.UIUtil;

/**
 * Wizard for adding a service 
 * 
 * @author Jakob Henriksson 
 *
 */
public class AddServiceWizard extends Wizard {

	AddServicePage pageService;
	AddServiceResultPage pageName;

	/**
	 * Constructor
	 *
	 */
	public AddServiceWizard() {
		setWindowTitle("Add Sensor Observation Service");
		ImageDescriptor id =
			UIUtil.getImageDescriptor(Activator.PLUGIN_ID,
					"icons/fugue/icons-32/database.png");
		setDefaultPageImageDescriptor(id);
		setNeedsProgressMonitor(true);
		setHelpAvailable(true);
	}

	/**
	 * Add pages
	 *
	 */
	@Override
	public void addPages()
	{
		pageService = new AddServicePage(AddServicePage.pageId);
		addPage(pageService);
		pageName = new AddServiceResultPage(AddServiceResultPage.pageId);
		addPage(pageName);
	}

	@Override
	public boolean canFinish() {
		return pageService.isSuccessful() &&
			!pageName.getServiceName().trim().equals("");
	}

	@Override
	public boolean performFinish() {
		// get and update source with details
		Service service = pageService.getService();
		String name = pageName.getServiceName();
		service.setName(name);
		// add source to list of known sources
		ServiceRoot.getInstance().addService(service);
		// send notification to the source list viewer to update
//		service.fireUpdated();
		return true;
	}

}

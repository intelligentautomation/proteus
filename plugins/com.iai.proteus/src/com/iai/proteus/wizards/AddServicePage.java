/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.wizards;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.model.services.ServiceType;

public class AddServicePage extends WizardPage implements Listener {
	
	private static final Logger log = Logger.getLogger(AddServicePage.class);
	
	public static String pageId = "pageService";

	// internal
	// service type combo box widget 
	private Combo comboServiceType;
	// service URL entry widget 
	private Text textServiceUrl;

	
	// the service created after successfully executing wizard
	private Service service;

	// true if we successfully got the Capabilities document, false otherwise
	private boolean success = false;

	/**
	 * Constructor 
	 *
	 * @param pageName
	 */
	public AddServicePage(String pageName) {
		super(pageName);
		setTitle("Add Service");
		setMessage("Provide servie type and service endpoint.");
		// default 
		setPageComplete(false);
	}
	
	/**
	 * Override and do not try and fetch the next page 
	 * (will only happen when clicking 'next')
	 * 
	 */
	@Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }	
	
	/**
	 * Returns true if this page was executed successfully, false otherwise
	 *
	 * @return
	 */
	public boolean isSuccessful() {
		return success;
	}

	/**
	 * Returns the service that was created after successfully executing wizard
	 *
	 * @return
	 */
	public Service getService() {
		return service;
	}

	/**
	 * Create contents of the wizard page 
	 *
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, true));

		setControl(container);

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		// service type 
		Label lblType = new Label(composite, SWT.NONE);
		lblType.setText("Type");
		
		comboServiceType = new Combo(composite, SWT.READ_ONLY);
		comboServiceType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		comboServiceType.setItems(new String[] {
			ServiceType.SOS.toStringLong(), 
			ServiceType.WMS.toStringLong()
		});
		// default
		comboServiceType.select(0);
		
		// service URL (endpoint) 
		Label lblUrl = new Label(composite, SWT.NONE);
		lblUrl.setText("URL");

		textServiceUrl = new Text(composite, SWT.BORDER);
		textServiceUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// add listener to handle error messages
		textServiceUrl.addListener(SWT.CHANGED, this);
	}

	/**
	 * Do whatever is needed to progress to the next page 
	 * 
	 */
	@Override
	public IWizardPage getNextPage() {

		success = false;
		service = null;

		try {

			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {

					try {
						
						monitor.beginTask("Trying to retrieve the Capabilities document...",
								IProgressMonitor.UNKNOWN);
						
						monitor.worked(1);
						
						String url = textServiceUrl.getText();
						
						switch (comboServiceType.getSelectionIndex()) {
						// SOS
						case 0: 
							// fetch capabilities 
							service = fetchSosCapabilities(url);
							break;
						// WMS 
						case 1:
							// fetch capabilities 
							service = fetchWmsCapabilities(url);
							break;
						}
						
					} finally {
						monitor.done();
					}
				}
			});

		} catch (InterruptedException e) {
			log.error("Interruption exception: " + e.getMessage());
		} catch (InvocationTargetException e) {
			log.error("Invocation target exception: " + e.getMessage());
			e.printStackTrace();
		}

		// return next page if we were successful
		if (service != null) {
			// mark this as a success
			success = true;
			// update next page
			AddServiceResultPage page = ((AddServiceWizard) getWizard()).pageResult;
			String name = service.getName();
			if (name != null)
				page.setName(name);
			// return the next page 
			return page;
		}

		setErrorMessage("Failed reading service Capabilities. " +
				"Please check the information or try again later.");

		// default
		return null;
	}
	
	/**
	 * Fetches the Capabilities document 
	 * 
	 * @param endpoint
	 * @return
	 */
	private Service fetchSosCapabilities(String endpoint) {
		
		SosCapabilities capabilities = SosUtil.getCapabilities(endpoint);

		if (capabilities != null) {
			// create service object 
			Service service = new Service(ServiceType.SOS);
			service.setServiceUrl(endpoint);
			// try and get the title
			String title = capabilities.getServiceIdentification().getTitle();
			if (title != null) {
				service.setName(title);
			}
			return service; 
		} 

		// default 
		return null; 
	}
	
	/**
	 * Fetches 
	 * 
	 * @param endpoint
	 * @return
	 */
	private Service fetchWmsCapabilities(String endpoint) {
		
		try {
			
            WMSCapabilities capabilities = 
            		WMSCapabilities.retrieve(new URI(endpoint));
                        
            if (capabilities != null) {
            	// parse 
            	capabilities.parse();
            	// create service object 
            	Service service = new Service(ServiceType.WMS);
            	service.setServiceUrl(endpoint);
            	// try and get the title
            	String title = 
            			capabilities.getServiceInformation().getServiceTitle();
            	if (title != null) {
            		service.setName(title);
            	}
            	return service; 
            }

		} catch (URISyntaxException e) {
			log.error("URI Exception: " + e.getMessage());
		} catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		}
		
		// default 
		return null;
	}
	
	/**
	 * Implements @{link Listener} 
	 * 
	 * @param event 
	 */
	@Override
	public void handleEvent(Event event) {
		
		boolean error = false;

		// check service URL when selecting to use a user-defined URL, or
		// when the URL is changed
		if (event.widget == textServiceUrl) {
			
			String url = textServiceUrl.getText();
			
			System.out.println("URL: " + url);

			// first check: validate URL 
			UrlValidator urlValidator = 
					new UrlValidator(new String[] { "http", "https" } );
			if (!urlValidator.isValid(url)) {
				// set error message
				setMessage("The provided URL is not valid", WizardPage.ERROR);
				// mark the error 
				error = true;
			} 

			// second check: check if the URL already exists
			if (!error && serviceUrlExists(url)) {
				setMessage("Service with the same URL already exists",
						WizardPage.ERROR);
				// mark the error 
				error = true;
			} 
			
			// no error
			if (!error) {
				setMessage("Click 'next' to try and fetch the Capabilities document", 
						WizardPage.INFORMATION);
			}
		}
		
		// update variable 
		setPageComplete(!error);

		getWizard().getContainer().updateButtons();
	}

	/**
	 * Returns true if service with the same URL already exists
	 *
	 * @param url
	 * @return
	 */
	private boolean serviceUrlExists(String url) {
		for (Service service : ServiceRoot.getInstance()) {
			if (service.getServiceUrl().equalsIgnoreCase(url))
				return true;
		}
		return false;
	}
}

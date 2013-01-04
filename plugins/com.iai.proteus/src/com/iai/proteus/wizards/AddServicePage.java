package com.iai.proteus.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	
	private String serviceUrl;
	private Text textServiceUrl;

	// the service created after successfully executing wizard
	private Service service;

	// true if we successfully got the Capabilities document, false otherwise
	private boolean success = false;

	/**
	 * Create the wizard.
	 *
	 * @param pageName
	 */
	public AddServicePage(String pageName) {
		super(pageName);
		setTitle("Add Sensor Observation Service");
		setMessage("Enter the end-point URL to the service that should be added");
	}

	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		container.setLayout(new GridLayout(1, true));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		Label lblUrl = new Label(composite, SWT.NONE);
		lblUrl.setText("URL");

		textServiceUrl = new Text(composite, SWT.BORDER);
		textServiceUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblResult = new Label(container, SWT.NONE);
		lblResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));	
		
		// add listener to handle error messages
		textServiceUrl.addListener(SWT.CHANGED, this);
	}


	public String getServiceUrl() {
		return serviceUrl;
	}

	@Override
	public boolean isPageComplete() {

		/*
		 * See if we have provided a URL in the field
		 */
		try {
			String url = textServiceUrl.getText();
			// try to create a URL
			new URL(url);
			// if there was no error, check if the URL already exists
			if (serviceUrlExists(url)) {
				return false;
			}
			// remember the URL
			serviceUrl = url;
			return true;
		} catch (MalformedURLException e) {

		}

		// default
		return false;
	}

	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	@Override
	public IWizardPage getNextPage() {

		success = false;
//		String message = null;

		try {

			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {

					try {
						
						monitor.beginTask("Retrieving Capabilities document",
								IProgressMonitor.UNKNOWN);
						
						monitor.worked(1);

						// fetch capabilities 
						Service service = fetchCapabilitiesDocument();
						
						// for now, do not do anything with the service
						// object, it will be stored
						
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

		// return next page if we were successful, otherwise null
		if (service != null) {
			// mark this as a success
			success = true; 
			// return the next page 
			return ((AddServiceWizard) getWizard()).pageName;
		}

		setErrorMessage("Could not retrieve the Capabilities " +
				"from the service. Please try again later.");

		// default
		return null;
	}
	
	/**
	 * Fetches the Capabilities document 
	 * 
	 * @return
	 */
	private Service fetchCapabilitiesDocument() {
		
		// default
		String name = "Untitled";

		service = new Service(ServiceType.SOS);
		service.setServiceUrl(getServiceUrl());

		SosCapabilities capabilities =
				SosUtil.getCapabilities(service.getServiceUrl());

		if (capabilities != null) {
			name = capabilities.getServiceIdentification().getTitle();
		} else {
			// was not successful 
			service = null; 
		}

		IWizardPage page =
				getWizard().getPage(AddServiceResultPage.pageId);
		// set the default name to use
		if (page instanceof AddServiceResultPage) {

			// set the name in the next page
			((AddServiceResultPage)page).setName(name);
		}
		
		return service; 
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

	@Override
	public void handleEvent(Event event) {

		// check service URL when selecting to use a user-defined URL, or
		// when the URL is changed
		if (event.widget == textServiceUrl) {
			try {
				String url = textServiceUrl.getText();
				// try to create a URL
				new URL(url);
				// if there was no error, check if the URL already exists
				if (serviceUrlExists(url)) {
					setMessage("Service with the same URL already exists",
							WizardPage.ERROR);
				} else {
					// no error
					setMessage("");
				}

			} catch (MalformedURLException e) {
				Status status = new Status(IStatus.ERROR, "not_used",
						"The URL is not valid");
				// set error message
				setMessage(status.getMessage(), WizardPage.ERROR);
			}
		} 

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

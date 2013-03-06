/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;

public class AddServiceResultPage extends WizardPage implements Listener {
	
	public static String pageId = "pageName"; 
	
	private Text serviceName;
	private String hubMessage;

	private Label lblResults; 

	/**
	 * Create the wizard
	 * 
	 * @param pageName
	 */
	public AddServiceResultPage(String pageName) {
		super(pageName);
		setTitle("Add Sensor Observation Service");
		setDescription("Provide a name for the service");
		
		// defaults
		hubMessage = "";
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		container.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		Label lblName = new Label(composite, SWT.NONE);
		lblName.setText("Name");
		
		serviceName = new Text(composite, SWT.BORDER);
		serviceName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// default
		serviceName.setText("Untitled");
		
		serviceName.addListener(SWT.CHANGED, this);
		
		lblResults = new Label(container, SWT.NONE);
		lblResults.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		lblResults.setText(hubMessage);
	}

	/**
	 * Sets the name of the source 
	 * 
	 * @param name
	 */
	public void setName(String name) {
		serviceName.setText(name.trim());
	}
	
	public String getServiceName() {
		return serviceName.getText();
	}
	
	/**
	 * Sets the message received from the hub 
	 * 
	 * @param message
	 */
	public void setHubMessage(String message) {
//		this.hubMessage = message; 
//		lblResults.redraw();
	}
	
	@Override
	public boolean isPageComplete() {
		return !(serviceName.getText().trim().equals("") ||
					serviceNameExists(serviceName.getText()));
	}
	
	@Override 
	public IWizardPage getNextPage() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == serviceName) {
			if (serviceName.getText().trim().equals("")) {
				Status status = new Status(IStatus.ERROR, "not_used", 
                	"Must provide a name for the source");
				// set error message 
				setMessage(status.getMessage(), WizardPage.ERROR);
			} else if (serviceNameExists(serviceName.getText())) {
				setMessage("A source with the same name already exists"); 
			} else {
				setMessage("");	
			}
		}
		
		getWizard().getContainer().updateButtons();
	}
	
	/**
	 * Returns true if source with the same name already exists 
	 * 
	 * @param name
	 * @return
	 */
	private boolean serviceNameExists(String name) {
		for (Service service : ServiceRoot.getInstance()) {
			if (service.getName().equalsIgnoreCase(name))
				return true;
		}
		return false; 
	}
}

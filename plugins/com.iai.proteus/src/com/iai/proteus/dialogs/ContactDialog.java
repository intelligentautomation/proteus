/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;


public class ContactDialog extends Dialog {
	
	private String provider; 
	private String contact;
	private String phone;
	private String email; 
	
	private Text textPhone;
	private Text textContact;
	private Text textProvider;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ContactDialog(Shell parentShell, SosCapabilities capabilities) {
		super(parentShell);
		
		provider = SosUtil.getServiceProviderName(capabilities);
		if (provider == null)
			provider = "-";
		
		contact = SosUtil.getServiceContactName(capabilities);
		if (contact == null)
			contact = "-";
		
		phone = SosUtil.getServiceContactPhone(capabilities);
		if (phone == null)
			phone = "-";

		email = SosUtil.getServiceContactEmail(capabilities);
		if (email == null)
			email = "-";
		
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		Label lblProvider = new Label(container, SWT.NONE);
		lblProvider.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProvider.setText("Provider:");
		
		textProvider = new Text(container, SWT.BORDER);
		textProvider.setText(provider);
		textProvider.setEditable(false);
		textProvider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblContact = new Label(container, SWT.NONE);
		lblContact.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblContact.setText("Contact:");
		
		textContact = new Text(container, SWT.BORDER);
		textContact.setText(contact);
		textContact.setEditable(false);
		textContact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPhone = new Label(container, SWT.NONE);
		lblPhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPhone.setText("Phone:");
		
		textPhone = new Text(container, SWT.BORDER);
		textPhone.setText(phone);
		textPhone.setEditable(false);
		textPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblEmail = new Label(container, SWT.NONE);
		lblEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEmail.setText("E-mail:");
		
		Link linkEmail = new Link(container, SWT.NONE);
		linkEmail.setText("<a href=\"mailto:" + email + "\">" + email + "</a>");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 184);
	}

}

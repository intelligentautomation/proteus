/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.wizards.AddServiceWizard;

/**
 * Dialog for managing services 
 * 
 * @author Jakob Henriksson 
 *
 */
public class ManageServicesDialog extends TitleAreaDialog {

	private TableViewer tableViewer;
	private Table table;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ManageServicesDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Manage Sensor Observation Services");
		setMessage("You can fetch the latest set of available services from the Community Hub, or add your own services. ");
		Composite area = (Composite) super.createDialogArea(parent);

		ToolBar toolBar = new ToolBar(area, SWT.FLAT | SWT.RIGHT);

		ToolItem tltmAddService = new ToolItem(toolBar, SWT.NONE);
		tltmAddService.setText("Add Service");
		tltmAddService.setImage(UIUtil.getImage("icons/fugue/database--plus.png"));
		tltmAddService.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				WizardDialog dialog = 
						new WizardDialog(getShell(), new AddServiceWizard());
				dialog.open(); 
				// refresh view
				tableViewer.refresh();
//				if (dialog.open() == IDialogConstants.OK_ID) {
//					
//				}
			}
		});

		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		tableViewer = new TableViewer(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
		colName.getColumn().setText("Name");
		colName.getColumn().setWidth(200);
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Service service = (Service) element;
				return service.getName();
			}
		});

		TableViewerColumn colUrl = new TableViewerColumn(tableViewer, SWT.NONE);
		colUrl.getColumn().setText("Service URL");
		colUrl.getColumn().setWidth(200);
		colUrl.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Service service = (Service) element;
				return service.getServiceUrl();
			}
		});

		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setContentProvider(new ServiceContentProvider());
		tableViewer.setInput(ServiceRoot.getInstance());

		table.setFocus();

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.CLOSE_LABEL,
				true);
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
//				true);
//		createButton(parent, IDialogConstants.CANCEL_ID,
//				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 *
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 529);
	}

}

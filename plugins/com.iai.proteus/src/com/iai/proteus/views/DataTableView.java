/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.iai.proteus.Activator;
import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.ui.UIUtil;

public class DataTableView extends ViewPart {

	public static final String ID = "com.iai.proteus.views.DataTableView"; //$NON-NLS-1$

	private Table table;

	private SensorData currentSensorData;

	private IAction actionExportToCSV =
			new Action("Export to CSV",
					UIUtil.getImageDescriptor("icons/fugue/database-export.png")) {
		@Override
		public void run() {
			TimeSeriesUtil.exportDataToCSV(getViewSite().getShell(),
					currentSensorData);
		}
	};


	/**
	 * Default constructor
	 */
	public DataTableView() {

	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@SuppressWarnings("serial")
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		{
			table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
		}

		initializeToolBar();
		initializeMenu();
		
		// get EventAdmin service 
		BundleContext ctx = Activator.getContext();
		ServiceReference<EventAdmin> ref = 
				ctx.getServiceReference(EventAdmin.class);
		EventAdmin eventAdminService = ctx.getService(ref);
		
		// create handler 
		EventHandler handler = new EventHandler() {
			public void handleEvent(final Event event) {
				
				Object value = event.getProperty("value");
				
				// preview plot 
				if (match(event, EventTopic.QS_PREVIEW_TABLE_UPDATE)) {
					
					if (value instanceof SensorData) {
						updateTable((SensorData) value);
					}					
				}
				
				else if (match(event, EventTopic.QS_PREVIEW_TABLE_CLEAR)) {

					clearTable();
				}
			}
		};
		
		// register service 
		Dictionary<String,String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, 
				EventTopic.TOPIC_QUERYSET.toString());
		// listen to query set topics 
		ctx.registerService(EventHandler.class.getName(), handler, properties);				

		// ask for data that may be available to display
		eventAdminService.sendEvent(new Event(EventTopic.QS_PREVIEW_TABLE_REQUEST.toString(), 
				new HashMap<String, Object>() { 
			{
				put("object", this);
			}
		}));		
	}
	
	/**
	 * Returns true if the event matches the event topic, false otherwise 
	 * 
	 * @param event
	 * @param topic
	 * @return
	 */
	private boolean match(Event event, EventTopic topic) {
		return event.getTopic().equals(topic.toString());
	}	

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();

		// disabled by default
		actionExportToCSV.setEnabled(false);
		toolbarManager.add(actionExportToCSV);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();

		menuManager.add(actionExportToCSV);
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	/**
	 * Update the table
	 *
	 * @param sensorData
	 */
	public void updateTable(final SensorData sensorData) {

		// remember the sensor data
		currentSensorData = sensorData;

		UIUtil.update(new Runnable() {
			@Override
			public void run() {

				table.setRedraw(false);

				while (table.getColumnCount() > 0) {
					table.getColumns()[0].dispose();
				}

				// clear the table
				table.removeAll();

				// columns
				List<Field> fields = sensorData.getFields();
				for (int i = 0; i < fields.size(); i++) {
					TableColumn column = new TableColumn (table, SWT.NONE);
					column.setText(fields.get(i).getName());
				}

				// rows
				List<String[]> rows = sensorData.getData(fields);
				for (String[] row : rows) {
					TableItem item = new TableItem(table, SWT.NONE);
					for (int i = 0; i < row.length; i++) {
						item.setText(i, row[i]);
					}
				}

				for (int i = 0; i < fields.size(); i++) {
					table.getColumn(i).pack();
				}

				table.setRedraw(true);

				// enable export action
				actionExportToCSV.setEnabled(true);
			}
		});
	}

	/**
	 * Clears the table
	 */
	private void clearTable() {

		// forget the sensor data
		currentSensorData = null;

		UIUtil.update(new Runnable() {
			@Override
			public void run() {

				table.setRedraw(false);

				while (table.getColumnCount() > 0) {
					table.getColumns()[0].dispose();
				}

				// clear the table
				table.removeAll();

				table.setRedraw(true);

				// disable export action
				actionExportToCSV.setEnabled(false);
			}
		});
	}
}

package com.iai.proteus.views;

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

import com.iai.proteus.common.event.Event;
import com.iai.proteus.common.event.EventListener;
import com.iai.proteus.common.event.EventNotifier;
import com.iai.proteus.common.event.EventType;
import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.ui.UIUtil;

public class DataTableView extends ViewPart implements EventListener {

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

		EventNotifier.getInstance().addListener(this);

		/*
		 * Ask for data that may be available to display
		 */
		EventNotifier.getInstance().fireEvent(this,
				EventType.DATA_TABLE_VIEWER_DATA_REQUEST);
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

	@Override
	public void update(Event event) {
		Object value = event.getValue();
		switch (event.getEventType()) {
		case DATA_TABLE_VIEWER_UPDATE:

			if (value instanceof SensorData) {
				updateTable((SensorData) value);
			}

			break;

		case DATA_TABLE_VIEWER_CLEAR:

			clearTable();

			break;
		}
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

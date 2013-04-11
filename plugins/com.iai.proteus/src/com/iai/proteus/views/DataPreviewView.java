/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.views;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.experimental.swt.SWTUtils;
import org.jfree.ui.HorizontalAlignment;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.iai.proteus.Activator;
import com.iai.proteus.common.TimeUtils;
import com.iai.proteus.common.event.EventNotifier;
import com.iai.proteus.common.event.EventType;
import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.queryset.EventTopic;
import com.iai.proteus.ui.UIUtil;

/**
 * View for visualizing sensor data 
 * 
 * @author Jakob Henriksson
 *
 */
public class DataPreviewView extends ViewPart {

	private static final Logger log = Logger.getLogger(DataPreviewView.class);

	public static final String ID = "com.iai.proteus.chart.DataPreviewView";
	
	// EventAdmin service for communicating with other views/modules
	private EventAdmin eventAdminService;	

	private StackLayout stackLayout;
	// the stack that holds the various plots
	private Composite compositePlotStack;
	// composite for each chart type
	private Composite compositeTimeSeries;

	// Time Series
	private JFreeChart chartTimeSeries;
	private TimeSeriesCollection datasetTimeSeries;

	/*
	 * Holds the currently plotted data
	 */
	private SensorData currentSensorData;

	/*
	 * Actions
	 */

	IAction actionExportPlot = new Action("&Export plot as PNG",
			UIUtil.getImageDescriptor("icons/fugue/chart.png")) {
		public void run() {
			try {
				((ChartComposite)compositeTimeSeries).doSaveAs();
			} catch (Exception e) {
				e.printStackTrace();
				UIUtil.showErrorMessage(e.getMessage());
			}
		}
	};

	/*
	 * Action for exporting sensor data to disk in CSV format
	 */
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
	 * Constructor
	 *
	 */
	public DataPreviewView() {

		// default 
		currentSensorData = null;
	}

	/**
	 * Create contents of the view part.
	 *
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		Composite compositePlot = new Composite(parent, SWT.NONE);
		compositePlot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositePlot.setLayout(new GridLayout());

		stackLayout = new StackLayout();
		compositePlotStack = new Composite(compositePlot, SWT.NONE);
		compositePlotStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositePlotStack.setLayout(stackLayout);

		compositeTimeSeries = createTimeSeriesChart(compositePlotStack);

		// default top stack
		stackLayout.topControl = compositeTimeSeries;

		compositeTimeSeries.setLayout(new FillLayout(SWT.VERTICAL));
		compositeTimeSeries.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));

		initializeActions();
		initializeToolBar();
		initializeMenu();
		initializeListeners();
		
		// get EventAdmin service 
		BundleContext ctx = Activator.getContext();
		ServiceReference<EventAdmin> ref = 
				ctx.getServiceReference(EventAdmin.class);
		eventAdminService = ctx.getService(ref);
		
		// create handler 
		EventHandler handler = new EventHandler() {
			@SuppressWarnings("serial")
			@Override
			public void handleEvent(final Event event) {
				
				Object value = event.getProperty("value");
				
				// preview plot 
				if (match(event, EventTopic.QS_PREVIEW_PLOT)) {
					
					if (value instanceof DataPreviewEvent) {
						plot((DataPreviewEvent) value);
					}
				}
				
				else if (match(event, EventTopic.QS_PREVIEW_TABLE_REQUEST)) {
					
					if (currentSensorData != null) {
						// send event to update the table view with the 
						// current set of data 
						eventAdminService.sendEvent(new Event(EventTopic.QS_PREVIEW_TABLE_UPDATE.toString(), 
								new HashMap<String, Object>() { 
							{
								put("object", this);
								put("value", currentSensorData);
							}
						}));
					}					

				}
			}
		};
		
		// register service 
		Dictionary<String,String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, 
				EventTopic.TOPIC_QUERYSET.toString());
		// listen to query set topics 
		ctx.registerService(EventHandler.class.getName(), handler, properties);		

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
	

	public void dispose() {
		super.dispose();
	}

	/**
	 * Initialize actions
	 */
	private void initializeActions() {

		// export to CSV action: disabled by default
		actionExportToCSV.setEnabled(false);
	}



	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {

		IToolBarManager toolbarManager =
			getViewSite().getActionBars().getToolBarManager();

		toolbarManager.add(actionExportToCSV);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		 IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		 menuManager.add(actionExportToCSV);
		 menuManager.add(actionExportPlot);
	}

	/**
	 * Add listeners
	 *
	 */
	private void initializeListeners() {

		/*
		 * Add as listener
		 */
//		EventNotifier.getInstance().addListener(this);
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	/**
	 * Create a Time Series chart
	 *
	 * @param parent
	 * @return
	 */
	private Composite createTimeSeriesChart(Composite parent) {

		/*
		 * Create empty data set
		 */
		datasetTimeSeries = new TimeSeriesCollection();

		/*
		 * Create chart
		 */
		chartTimeSeries =
			ChartFactory.createTimeSeriesChart("", "", "",
					datasetTimeSeries, true, true, false);


		Color colorBg = SWTUtils.toAwtColor(parent.getBackground());
		chartTimeSeries.setBackgroundPaint(colorBg);
//		chartTimeSeries.addProgressListener(this);
		// for performance
		chartTimeSeries.setAntiAlias(false);

		XYPlot plot = (XYPlot) chartTimeSeries.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
//		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);

		plot.setDomainCrosshairLockedOnData(true);
//		plot.setRangeCrosshairVisible(false);
		plot.setRangeCrosshairVisible(true);

		plot.setNoDataMessage("There is currently no data to plot.");

		// legend
		LegendTitle legend = chartTimeSeries.getLegend();
		legend.setFrame(BlockBorder.NONE);
		legend.setBackgroundPaint(colorBg);
		legend.setHorizontalAlignment(HorizontalAlignment.CENTER);

		// date/time axis
		DateAxis dateAxis = new DateAxis();
		dateAxis.setAutoTickUnitSelection(true);
//		dateAxis.setVerticalTickLabels(true);
		DateFormat chartFormatter = new SimpleDateFormat("MM/dd/yy Ka");
		dateAxis.setDateFormatOverride(chartFormatter);
		plot.setDomainAxis(dateAxis);

		// modify the renderer
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(true);
//			renderer.setBaseShapesFilled(true);

//			Shape shape = renderer.getBaseLegendShape();
//			shape.getBounds().width += 20;
//
		}

		Composite chartComposite =
			new ChartComposite(parent, SWT.NONE , chartTimeSeries,
				false, true, true, true, true);

		((ChartComposite) chartComposite).setDomainZoomable(true);
		((ChartComposite) chartComposite).setRangeZoomable(false);

		return chartComposite;
	}



	/**
	 * Plot the sensor data available
	 *
	 * @param plotData
	 */
	private void plot(DataPreviewEvent plotData) {

		currentSensorData = plotData.getSensorData();
		
		plot(currentSensorData,
				plotData.getDomainVariable(), plotData.getRangeVariasbles());
		
		// notify listeners, for just one data set
		EventNotifier.getInstance().fireEvent(this,
				EventType.DATA_TABLE_VIEWER_UPDATE, currentSensorData);	

	}

	/**
	 * Plots a time series
	 *
	 * @param sensorData
	 * @param domainVariable
	 * @param rangeVariables
	 */
	private void plot(SensorData sensorData, Field domainVariable,
			Collection<Field> rangeVariables)
	{

		String description = "";
		String label = "";

		// get the time series for the data of this request
		final List<TimeSeries> timeSeries =
				generateTimeSeries(sensorData, description, label, domainVariable,
						rangeVariables);

		clearPlot();

		if (timeSeries.size() > 0) {

			/*
			 * Update the data underlying the chart
			 */
			UIUtil.update(new Runnable() {
				public void run() {
					for (TimeSeries series : timeSeries) {
						datasetTimeSeries.addSeries(series);
					}
					chartTimeSeries.fireChartChanged();

					// make sure the time series is on top
					stackLayout.topControl = compositeTimeSeries;
					compositePlotStack.layout();

					// enable export of data
					actionExportToCSV.setEnabled(true);
				}
			});
		}

		autoZoom();
	}


	/**
	 * Generates a time series data set for the given data and variables
	 * (domain and range)
	 *
	 * @param sensorData
	 * @param description
	 * @param title
	 * @param domainVariable
	 * @param rangeVariables
	 * @return
	 */
	private List<TimeSeries>
		generateTimeSeries(SensorData sensorData,
				String description, String title,
				Field domainVariable, Collection<Field> rangeVariables)
	{

		if (rangeVariables.size() > 0) {

			System.out.println("Range variables: " + rangeVariables);

			List<Field> allVariables = new ArrayList<Field>();
			// domain
			allVariables.add(domainVariable);
			// ranges
			for (Field field : rangeVariables) {
				allVariables.add(field);
			}

			/*
			 * Get data
			 */
			List<String[]> data = sensorData.getData(allVariables);

			System.out.println("DATA ROWS: " + data.size());

			// create as many time series as we have variables
			TimeSeries[] allSeries = new TimeSeries[rangeVariables.size()];
			for (int i = 0; i < allSeries.length; i++) {
				// create label for time series
				String variable = allVariables.get(i + 1).getName();
				String label = title + " - " + variable;
				allSeries[i] = new TimeSeries(label);
				allSeries[i].setDescription(description + "##" + variable);
			}

			/*
			 * Update labels
			 *
			 * TODO: what should this be?
			 */
//			chartSettings.labelYAxis = "";
//				Util.readableLocalURL(rangeVariables[0]);

			for (String[] row : data) {

				Date timestamp = TimeUtils.parseDefault(row[0]);
				if (timestamp != null) {

					// add to each time series in order
					for (int i = 0; i < allSeries.length; i++) {
						try {
							double value = Double.parseDouble(row[i + 1]);
							allSeries[i].addOrUpdate(new Minute(timestamp), value);
						} catch (NumberFormatException e) {
							log.warn("Could not parse '" + row[1] +
									"' as Double");
						}
					}
				}
			}

			return Arrays.asList(allSeries);

		} else {
			log.warn("Could not find appropriate values to plot");
		}

		// default
		return null;
	}


	/**
	 * Removes all the series from the chart
	 *
	 */
	private void clearPlot() {

		log.trace("Clearing plot.");

		// forget the sensor data
//		currentSensorData = null;

		UIUtil.update(new Runnable() {
			public void run() {
				// clear all
				datasetTimeSeries.removeAllSeries();

				// disable export action
				actionExportToCSV.setEnabled(false);
			}
		});
	}

	/**
	 *
	 */
	private void autoZoom() {
		UIUtil.update(new Runnable() {
			public void run() {
				((ChartComposite)compositeTimeSeries).restoreAutoBounds();
			}
		});
	}

}

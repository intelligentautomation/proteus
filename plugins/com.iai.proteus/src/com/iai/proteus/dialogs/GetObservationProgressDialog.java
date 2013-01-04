package com.iai.proteus.dialogs;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.wb.swt.SWTResourceManager;

import com.iai.proteus.common.Labeling;
import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.common.sos.exception.ExceptionReportException;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.dialogs.DownloadModel.Status;
import com.iai.proteus.queryset.DataFetcher;
import com.iai.proteus.ui.UIUtil;
import com.iai.proteus.views.TimeSeriesUtil;

public class GetObservationProgressDialog extends Dialog {
	
	private static final Logger log = 
			Logger.getLogger(GetObservationProgressDialog.class);

	private TableViewer tableViewer;
	
	private Collection<DownloadModel> input; 
	
	private boolean statusDownloading = false;
	private boolean statusCancelled = false;

	private Label lblStatus;
	
	// foreground
	private Color colorTextPending;
	private Color colorTextProcessed;	
	
	// background 
	private Color colorNone; 
	private Color colorSuccess;
	private Color colorError;
	
	
	// folder where fetched data ends up 
	private File folder; 

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public GetObservationProgressDialog(Shell parentShell, File folder) {
		super(parentShell);
		
		this.folder = folder;
		
		// default 
		input = new ArrayList<DownloadModel>();
		
		colorTextPending = SWTResourceManager.getColor(SWT.COLOR_BLACK);
		colorTextProcessed = SWTResourceManager.getColor(SWT.COLOR_WHITE);
		
		colorNone = SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
		colorSuccess = SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN);
		colorError = SWTResourceManager.getColor(SWT.COLOR_DARK_RED);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		tableViewer = new TableViewer(container, SWT.BORDER);
		
		TableViewerColumn colQuery = new TableViewerColumn(tableViewer, SWT.NONE);
		colQuery.getColumn().setText("Query");
		colQuery.getColumn().setWidth(400);
		colQuery.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				DownloadModel model = (DownloadModel) element;
				return model.getLabel();
			}
			
			@Override
			public Color getForeground(Object element) {
				DownloadModel model = (DownloadModel) element;
				return getForegroundColor(model);
			}
			
			@Override
			public Color getBackground(Object element) {
				DownloadModel model = (DownloadModel) element;
				return getBackgroundColor(model);
			}			
			
		});		
		
		TableViewerColumn colStatus = new TableViewerColumn(tableViewer, SWT.NONE);
		colStatus.getColumn().setText("Status");
		colStatus.getColumn().setWidth(150);
		colStatus.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				DownloadModel model = (DownloadModel) element;
				return model.getStatusMsg();
			}
			
			@Override
			public Color getForeground(Object element) {
				DownloadModel model = (DownloadModel) element;
				return getForegroundColor(model);
			}			
			
			@Override
			public Color getBackground(Object element) {
				DownloadModel model = (DownloadModel) element;
				return getBackgroundColor(model);
			}
			
		});
		
		Table table = tableViewer.getTable();
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setUseHashlookup(true);
		// empty input 
		tableViewer.setInput(input);
		
		Composite compositeStatus = new Composite(container, SWT.NONE);
		compositeStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeStatus.setLayout(new GridLayout(2, false));
		
		Label lblStatusHeader = new Label(compositeStatus, SWT.NONE);
		lblStatusHeader.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
		lblStatusHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblStatusHeader.setSize(37, 14);
		lblStatusHeader.setText("Status: ");
		
		lblStatus = new Label(compositeStatus, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// start the download thread 
		startThread();
		
		return container;
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Exporting sensor data");
	}	
	
	/**
	 * Returns the color to use in viewer 
	 * 
	 * @param model
	 * @return
	 */
	private Color getForegroundColor(DownloadModel model) {
		switch (model.getStatus()) {
			case NONE:
				return colorTextPending; 
		}
		return colorTextProcessed;
	}	
	
	/**
	 * Returns the color to use in viewer 
	 * 
	 * @param model
	 * @return
	 */
	private Color getBackgroundColor(DownloadModel model) {
		switch (model.getStatus()) {
			case SUCCESS:
				return colorSuccess;
			case ERROR:
				return colorError;
		}
		return colorNone;
	}

	/**
	 * Create a label 
	 * 
	 * @param sensorOffering
	 * @param observedProperty
	 * @return
	 */
	public static String createQueryLabel(SensorOffering sensorOffering, 
			String observedProperty) 
	{
		return "Offering: " + sensorOffering.getGmlId() + 
				"; Observed property: " + 
				Labeling.labelProperty(observedProperty);		
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
		
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		
		// disable button by default 		
		Button button = getButton(IDialogConstants.OK_ID); 
		if (button != null)
			button.setEnabled(false);		
	}
	
	/**
	 * Internal thread where sensor data downloading takes place  
	 * 
	 * @author Jakob Henriksson
	 *
	 */
	class ProcessThread extends Thread {
		@Override 
		public void run() {
			startDataDownload(); 
		}
	}
	
	
	private void startThread() {
		new ProcessThread().start(); 
	}
	
	/**
	 * Handles the sensor data download 
	 */
	private void startDataDownload() {
		
		// set status flag 
		statusDownloading = true;
		
		DataFetcher fetcher = new DataFetcher(null, null);
		
		int success = 0, failed = 0, total = input.size();
		
		for (DownloadModel model : input) {
			
			// update the dialog UI
			updateUI(model, success, failed, total); 
			
			if (statusCancelled)
				break;
			
			SosDataRequest dataRequest = model.getDataRequest();

			if (dataRequest != null) {
				try {

					SensorData sensorData = 
							fetcher.executeRequest(dataRequest);

					model.setStatus(Status.SUCCESS);
					model.setStatusMsg("Success");

					File file = new File(folder, "data" + success + ".txt");

					TimeSeriesUtil.exportDataToCSV(file, sensorData);

					success++;

				} catch (SocketTimeoutException e) {
					
					model.setStatus(Status.ERROR);
					model.setStatusMsg("Request timed out");
					
					failed++;
					
					log.error("Sensor data download request timed out: " + 
							e.getMessage());
					
					
				} catch (ExceptionReportException e) {
					
					model.setStatus(Status.ERROR);
					model.setStatusMsg("Exception prevented download");	
					
					failed++;
					
					log.error("Sensor data download request timed out: " + 
							e.getMessage());
				}
				
			} else {
				
				// if there is no data request, it will be a failure...
				failed++;
			}
			
			// update the dialog UI
			updateUI(model, success, failed, total); 
		}
		
		// update buttons
		UIUtil.update(new Runnable() {
			@Override
			public void run() {
				
				// enable 
				Button button = getButton(IDialogConstants.OK_ID); 
				if (button != null)
					button.setEnabled(true);
				
				// disable 
				button = getButton(IDialogConstants.CANCEL_ID); 
				if (button != null)
					button.setEnabled(false);
				
			}
		});
		
		if (statusCancelled) {
			UIUtil.update(new Runnable() {
				@Override
				public void run() {
					propagateCancel();
				}
			});
		}
		
		// set status flag 
		statusDownloading = false;
	}
	
	/**
	 * Propagate cancel button press
	 * 
	 */
	private void propagateCancel() {
		super.cancelPressed();		
	}
	
	/**
	 * Updates the UI
	 * 
	 * @param success
	 * @param failed
	 * @param total
	 */
	private void updateUI(final DownloadModel model, final int success, 
			final int failed, final int total) 
	{
		final StructuredSelection selection = new StructuredSelection(model);
		
		UIUtil.update(new Runnable() {
			@Override
			public void run() {
				
				lblStatus.setText("Total requests: " + total + "; " + 
						"Successful: " + success + "; Failed: " + failed);
				
				tableViewer.setSelection(selection, true);
				tableViewer.refresh();
			}
		});
	}
	
	@Override
	protected void cancelPressed() {
		// set status flag
		statusCancelled = true;
		// propagate if we are not downloading 
		if (!statusDownloading)
			super.cancelPressed();
	}
	
	/**
	 * Set the viewer input 
	 * 
	 * @param models
	 */
	public void setInput(Collection<DownloadModel> models) {
		input = models;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}

}

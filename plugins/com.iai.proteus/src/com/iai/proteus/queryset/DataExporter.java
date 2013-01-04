package com.iai.proteus.queryset;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.iai.proteus.common.sos.data.SensorData;
import com.iai.proteus.common.sos.exception.ExceptionReportException;
import com.iai.proteus.common.sos.util.SosDataRequest;
import com.iai.proteus.dialogs.DownloadModel;
import com.iai.proteus.views.TimeSeriesUtil;

public class DataExporter implements IRunnableWithProgress {

	private static final Logger log = Logger.getLogger(DataExporter.class);

	/*
	 * 
	 */
	private Collection<DownloadModel> models;
	private File folder;

	/**
	 * Constructor
	 *
	 * @param models
	 * @param folder 
	 */
	public DataExporter(Collection<DownloadModel> models, File folder) {
		this.models = models;
		this.folder = folder;
	}

	/**
	 * Implementation of @{link IRunnableWithProgress}
	 *
	 */
	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException
	{
		
		DataFetcher fetcher = new DataFetcher(null, null); 
		
		int counter = 0; 
		
		monitor.beginTask("Exporting sensor data", models.size());
		
		for (DownloadModel model : models) {
			
			if (monitor.isCanceled())
				break;
			
			SosDataRequest dataRequest = model.getDataRequest();

			try {

				SensorData sensorData = 
						fetcher.executeRequest(dataRequest);

				File file = new File(this.folder, "data" + counter++ + ".txt");

				TimeSeriesUtil.exportDataToCSV(file, sensorData);
				
				log.info("Dowloaded sensor data");
				
				monitor.worked(1);

			} catch (SocketTimeoutException e) {
				System.err.println("Error: " + e.getMessage());
			} catch (ExceptionReportException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
	}		

}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.iai.proteus.Activator;
import com.iai.proteus.BundleUtils;
import com.iai.proteus.common.sos.GetCapabilities;
import com.iai.proteus.common.sos.SosCapabilitiesCache;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.model.map.MapLayer;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;

public class Startup {

	private static final Logger log = Logger.getLogger(Startup.class);

	/*
	 * Folder names
	 *
	 */
	public static String folderCapabilities = "capabilities";

	/*
	 * File names
	 */
	public static String fileServices = "services.xml";

    /**
     * Loads the layer configuration from a configuration file
     *
     */
    public static void loadSetup() {

    }

    /**
     * Returns true if the services file exists, false otherwise
     *
     * @return
     */
    public static boolean servicesFileExists() {
		File parent = Activator.stateLocation.toFile();
		File services = new File(parent, fileServices);
		return services.exists();
    }

    /**
     * Writes the given contents as the default project file
     *
     * @param contents
     */
    public static void createDefaultSetup() {

//    	// get default file
//    	String contents =
//    		BundleUtils.readBundleContents(Activator.PLUGIN_ID,
//    				"resources/" + "file.extension");
//
//		File parent = Activator.stateLocation.toFile();
//		File projects = new File(parent, "file.extension");
//
//		try {
//
//			FileUtils.write(projects, contents);
//
//		} catch (IOException e) {
//			log.error("Error writing default project: " + e.getMessage());
//		}

		/*
		 * Second, capabilities documents
		 */
		copyDefaultCapabilities();

    }

    /**
     * Copies the default capabilities documents
     */
    private static void copyDefaultCapabilities() {

    	String[] defaultCapabilities =
    		new String[] {
    			"noaa-sos-capabilities.xml"
    	};

    	File parent = Activator.stateLocation.toFile();
    	File folder = new File(parent, folderCapabilities);

    	for (String filename : defaultCapabilities) {

    		final String contents =
    			BundleUtils.readBundleContents(Activator.PLUGIN_ID,
    					"resources/capabilities/" + filename);

    		if (contents == null || contents.trim().equals(""))
    			continue;

    		try {

				// write file
				File file = new File(folder, filename);
				// write capabilities contents to file
				FileUtils.write(file, contents);

				Job job = new Job("Load Capabilities into cache") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// load into cache
						SosCapabilitiesCache cache =
							SosCapabilitiesCache.getInstance();

						SosCapabilities capabilities =
							GetCapabilities.parseCapabilitiesDocument(contents);

						if (capabilities != null) {
							cache.commit(contents, capabilities);
						}

						return Status.OK_STATUS;
					}
				};
				job.schedule();

    		} catch (IOException e) {
    			continue;
    		}
    	}

		log.info("Restored "  +
				" Capabilities documents for discovery");
    }

    /**
     * Persists the services to disk
     *
     */
    public static void saveServices() {

    	File parent = Activator.stateLocation.toFile();
    	File services = new File(parent, fileServices);

    	try {

    		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
    				new FileOutputStream(services)));
    		encoder.writeObject(ServiceRoot.getInstance());
    		encoder.close();

    		log.trace("Persisted services to disk");

    	} catch (FileNotFoundException e) {
    		log.error("Persisted services file: " +
    				services.getAbsolutePath() + " not found");
    	}

    }

    /**
     * Loads the workspace from persisted state
     *
     */
    public static void loadServices() {

    	File parent = Activator.stateLocation.toFile();
    	File services = new File(parent, fileServices);

    	try {

    		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
    				new FileInputStream(services)));
    		Object object = decoder.readObject();
    		if (object instanceof ServiceRoot) {
    			ServiceRoot root = (ServiceRoot) object;
    			for (Service service : root) {
    				ServiceRoot.getInstance().addService(service);
    			}
    		}

    		log.trace("Loaded persisted services from disk");

    	} catch (FileNotFoundException e) {
    		log.error("Persisted services file: " +
    				services.getAbsolutePath() + " not found");
    	} catch (NoSuchElementException e) {
    		log.error("NoSuchElementException when loading services: " +
    				e.getMessage());
    	}
    }

	public static void storeCapabilities() {

		File parent = Activator.stateLocation.toFile();
		File folder = new File(parent, folderCapabilities);
		if (!folder.exists())
			folder.mkdir();

		// TODO: can we optimize this so that we only delete and write
		//       Capabilities documents that were modified?

		/*
		 * Delete old cache files
		 */
		String[] ext = new String[] { "xml" };
		Iterator<File> it =
			FileUtils.iterateFiles(folder, ext, false);
		while (it.hasNext()) {
			it.next().delete();
		}

		/*
		 * Write new cache files
		 */
		SosCapabilitiesCache cache = SosCapabilitiesCache.getInstance();
		Iterator<String> urls = cache.iterator();
		int counter = 0;
		while (urls.hasNext()) {
			String url = urls.next();
			String contents = cache.getDocument(url);
			if (contents != null) {
				File file = new File(folder,
						"capabilities" + counter++ + ".xml");
				try {
					FileUtils.writeStringToFile(file, contents);
				} catch (IOException e) {
					log.warn("IOException: " + e.getMessage());
				}
			}
		}

	}

	/**
	 * Loads capabilities documents from cache
	 *
	 */
	public static void loadCapabilities() {

		File parent = Activator.stateLocation.toFile();
		final File folder = new File(parent, folderCapabilities);

		if (!folder.exists()) {

			/*
			 * Initialize the Capabilities cache
			 */
//			Collection<String> documents = Discovery.getCapabilitiesDocuments();
//
//			CapabilitiesCache cache = CapabilitiesCache.getInstance();
//
//			for (String document : documents) {
//				SosCapabilities capabilities =
//					GetCapabilities.parseCapabilitiesDocument(document);
//
//				if (capabilities != null) {
//					cache.commit(document, capabilities);
//				}
//			}

			log.info("There were no cached capabilities to load.");
			return;
		}

		Job job = new Job("Retrieving Capabilities document") {
			protected IStatus run(IProgressMonitor monitor) {

				log.trace("Starting to load cached Capabilities documents");

				File[] files = folder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File arg0, String filename) {
						return filename.endsWith(".xml");
					}
				});

				try {

					monitor.beginTask("Loading cached Capabilities documents",
							files.length);

					// delete old files
					for (File file : files) {

						try {

							String contents = FileUtils.readFileToString(file);

							SosCapabilities capabilities =
								GetCapabilities.parseCapabilitiesDocument(contents);

							SosCapabilitiesCache.getInstance().commit(contents,
									capabilities);

							monitor.worked(1);

						} catch (IOException e) {
							log.warn("IOException reading cached capabilities " +
									"document: " + e.getMessage());
						}
					}

					log.trace("Done loading cached Capabilities documents");

				} finally {
					monitor.done();
				}

				return Status.OK_STATUS;
			}
		};
		job.schedule();

	}

	/**
	 * Detailed bean serialization specifications
	 *
	 */
	private static void controlBeanSerialization() {

    	// make the 'active' field in MapLayer transient
    	try {
    		BeanInfo info = Introspector.getBeanInfo(MapLayer.class);
    		PropertyDescriptor[] propertyDescriptors =
    				info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; i++) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("active")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {

    	}
	}

}

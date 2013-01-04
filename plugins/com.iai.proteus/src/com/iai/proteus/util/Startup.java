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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.Activator;
import com.iai.proteus.BundleUtils;
import com.iai.proteus.common.sos.GetCapabilities;
import com.iai.proteus.common.sos.SosCapabilitiesCache;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.model.parser.LayerConfigParser;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.model.workspace.LayerRoot;
import com.iai.proteus.model.workspace.MapLayer;
import com.iai.proteus.model.workspace.Project;
import com.iai.proteus.model.workspace.WorkspaceRoot;

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
	public static String fileWorkspace = "workspace.xml";
	public static String fileServices = "services.xml";
	private static String fileFields = "fields.xml";

    /**
     * Loads the workspace from persisted state
     *
     */
    public static void loadWorkspace() {

    	File parent = Activator.stateLocation.toFile();
    	File workspace = new File(parent, fileWorkspace);

    	try {

    		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
    				new FileInputStream(workspace)));
    		Object object = decoder.readObject();
    		if (object instanceof WorkspaceRoot) {
    			WorkspaceRoot root = (WorkspaceRoot) object;
    			for (Project project : root) {
    				WorkspaceRoot.getInstance().addProject(project);
    				// deactivate all layers by default
//    				for (QueryLayer layer : project) {
//    					layer.deactivate();
//    				}

    			}
    		}

    	} catch (FileNotFoundException e) {
    		log.error("Persisted workspace file: " +
    				workspace.getAbsolutePath() + " not found");
    	}
    }


    /**
     * Loads the layer configuration from a configuration file
     *
     */
    public static void loadSetup() {

		File parent = Activator.stateLocation.toFile();
		File workspace = new File(parent, fileWorkspace);

		/*
		 * Create a parser and parse the configuration file
		 */
		new LayerConfigParser().createProjectsModel(workspace);
    }

    /**
     * Returns true if the projects file exists, false otherwise
     *
     * @return
     */
    public static boolean workspaceFileExists() {
		File parent = Activator.stateLocation.toFile();
		File workspace = new File(parent, fileWorkspace);
		return workspace.exists();
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

    	/*
    	 * First, project file
    	 */
    	String contents =
    		BundleUtils.readBundleContents(Activator.PLUGIN_ID,
    				"resources/" + Startup.fileWorkspace);

		File parent = Activator.stateLocation.toFile();
		File projects = new File(parent, fileWorkspace);

		try {

			FileUtils.write(projects, contents);

		} catch (IOException e) {
			log.error("Error writing default project: " + e.getMessage());
		}

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
     * Persists the workspace to disk
     *
     */
    public static void saveWorkspace() {

    	File parent = Activator.stateLocation.toFile();
    	File workspace = new File(parent, fileWorkspace);

    	// set specific bean serialization options
    	controlBeanSerialization();

    	try {

    		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
    				new FileOutputStream(workspace)));
    		encoder.writeObject(WorkspaceRoot.getInstance());
    		encoder.close();

    	} catch (FileNotFoundException e) {
    		log.error("Persisted workspace file: " +
    				workspace.getAbsolutePath() + " not found");
    	}

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

    /**
     * Saves the projects and sources setup to disk (serialized as XML)
     *
     */
    public static void saveSetup() {

		try {

	        DocumentBuilderFactory factory =
	        	DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = builder.newDocument();

	        Element root = LayerRoot.getInstance().serialize(document);
	        document.appendChild(root);

			// set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            trans.setOutputProperty(OutputKeys.VERSION, "1.0");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
            		"2");

			// write XML tree to disk
			File parent = Activator.stateLocation.toFile();
			File projects = new File(parent, fileWorkspace);

			FileWriter fw = new FileWriter(projects);

			StreamResult result = new StreamResult(fw);
			DOMSource source = new DOMSource(document);
			trans.transform(source, result);

			fw.close();

			log.trace("Serialized layers to disk");

		} catch (ParserConfigurationException e) {
			log.error("Parser configuration error while serializing layers: " +
					e.getMessage());
		} catch (TransformerException e) {
			log.error("Error while serializing layers: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException while serializing layers: " +
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

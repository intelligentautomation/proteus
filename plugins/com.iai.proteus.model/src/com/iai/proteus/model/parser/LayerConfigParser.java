package com.iai.proteus.model.parser;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.model.services.ServiceType;
import com.iai.proteus.model.workspace.LayerRoot;
import com.iai.proteus.model.workspace.Project;
import com.iai.proteus.model.workspace.Query;
import com.iai.proteus.model.workspace.QueryLayer;
import com.iai.proteus.model.workspace.QuerySensorOffering;
import com.iai.proteus.model.workspace.WorkspaceRoot;

@Deprecated
public class LayerConfigParser extends DefaultHandler {
	
	private static final Logger log = 
		Logger.getLogger(LayerConfigParser.class);
	
	private String nsGml = "http://www.opengis.net/gml/3.2";
	
	/*
	 * ALL the layers
	 */
	private LayerRoot layerRoot; 
	
	/*
	 * Holds the services
	 */
	private ServiceRoot serviceRoot;
	
	/*
	 * Holds the projects with its layers etc. 
	 */
	private WorkspaceRoot projectRoot;
	
	private Project project; 
	private QueryLayer layer;
	private Query query;
	private Service service; 
	private String url; 
	
	private String name; 
	private Stack<String> names = new Stack<String>();
	
	private StringWriter sw;
	private String str;
	
	/*
	 * Boolean helpers
	 */
	private boolean inServices = false;
	private boolean inService = false; 
	private boolean inLayer = false;
	private boolean inQuery = false; 	
	
	/**
	 * Returns a projects model created from the read in XML file  
	 * 
	 */
	@Deprecated 
	public void createProjectsModel(File file) {

		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
				
		try {
			
			spf.setFeature("http://xml.org/sax/features/namespaces", true);
			
			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			
			InputStream is = new FileInputStream(file);
			
			//parse the file and also register this class for call backs
			sp.parse(is, this);
			
			log.info("Loaded configuration file " + file.toString());
			
			layerRoot = LayerRoot.getInstance();
			
			layerRoot.setServiceRoot(serviceRoot);
			layerRoot.setProjectRoot(projectRoot);
			
		} catch(SAXException e) {
			log.error("Error parsing configuration file: " + e.getMessage());
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			log.warn("Projects file: " + file +	" could not be found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Start element
	 * 
	 */
	@Override
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {
		
		sw = new StringWriter();
	
		if (qName.equalsIgnoreCase("smt")) {
			
		} else if (qName.equalsIgnoreCase("services")) {
			serviceRoot = ServiceRoot.getInstance();
			inServices = true; 
		} else if (qName.equalsIgnoreCase("projects")) {
			projectRoot = WorkspaceRoot.getInstance();
		} else if (qName.equalsIgnoreCase("project")) {
			
			/*
			 * Create project
			 */
			project = new Project();
		} else if (qName.equalsIgnoreCase("layer")) {
			/*
			 * Create layer
			 */
			layer = new QueryLayer(project);
			inLayer = true; 
		} else if (qName.equalsIgnoreCase("query")) {
			inQuery = true; 
			/*
			 * Create query
			 */
			String type = attributes.getValue("type");
			if (type.equalsIgnoreCase("SensorOffering")) {
				String gmlId = attributes.getValue(nsGml, "id");
				if (gmlId != null) {
					query = new QuerySensorOffering(gmlId);
					
				} else {
					log.warn("There was no GmlID given for the SensorOffering query");
				}
			} else if (type.equalsIgnoreCase("Observation")) {
				// TODO: handle observations 
			}
		} else if (qName.equalsIgnoreCase("service")) {
			inService = true; 
			String strService = attributes.getValue("type");
			if (strService != null) {
				try {
					service = new Service(ServiceType.parse(strService));
				} catch (IllegalArgumentException e) {
					log.warn("Could not create an enum for the service: " + service);
				}
			} else {
				log.warn("There was not service attribute for the service element");
			}
		} 
	}

	/**
	 * End element
	 */
	@Override
	public void endElement(String uri, String localName,
		String qName) throws SAXException {
		
		str = sw.toString();
		
		String fullUri = !uri.equals("") ? uri + "#" + localName : localName;
	
		if (qName.equalsIgnoreCase("smt")) {
			
		} else if (qName.equalsIgnoreCase("services")) {
			inServices = false;
		} else if (qName.equalsIgnoreCase("project")) {
			/*
			 * Set project name (takes it from the stack)
			 */
			project.setName(names.pop());
			/*
			 * Add project to list of projects
			 */
			projectRoot.addProject(project);
		} else if (qName.equalsIgnoreCase("layer")) {
			/*
			 * Set layer name (takes it from the stack)
			 */
			layer.setName(names.pop());
			/*
			 * Add layer to project (and automatically set the project 
			 * as the parent of the layer)  
			 */
			project.addLayer(layer);
			inLayer = false;
		} else if (qName.equalsIgnoreCase("query")) {
			inQuery = false;
			/*
			 * Add query to layer  
			 */
			layer.addQuery(query);
		} else if (qName.equalsIgnoreCase("service")) {
			/*
			 * Set service name (takes it from the stack)
			 */
			service.setName(names.pop());
			/*
			 * Set the URL of the service 
			 */
			service.setServiceUrl(url);
			/*
			 * Add to known services if we are listing services 
			 */
			if (inServices) {
				serviceRoot.addService(service);
			} 
			/*
			 * If we are in a query, set the service of the query 
			 */
			else if (inQuery) {
//				query.setProvenance(service);
				/* mark this service model object as a query service */ 
//				service.provenanceService(); 
			}
			
			inService = false;
		} else if (qName.equalsIgnoreCase("url")) {
			url = new String(str); 
		} else if (qName.equalsIgnoreCase("name")) {
			name = new String(str); 
			// add name to stack
			names.add(name);
		} else if (fullUri.equalsIgnoreCase(nsGml + "#lowerCorner")) {
			if (inQuery && query instanceof QuerySensorOffering) {
				QuerySensorOffering qso = (QuerySensorOffering)query;
				float latlong[] = getLatLong(str); 
				qso.getSensorOffering().setLowerCornerLat(latlong[0]);
				qso.getSensorOffering().setLowerCornerLong(latlong[1]);
			}
		} else if (fullUri.equalsIgnoreCase(nsGml + "#upperCorner")) {
			if (inQuery && query instanceof QuerySensorOffering) {
				QuerySensorOffering qso = (QuerySensorOffering)query;
				float latlong[] = getLatLong(str); 
				qso.getSensorOffering().setUpperCornerLat(latlong[0]);
				qso.getSensorOffering().setUpperCornerLong(latlong[1]);
			}
		} else if (qName.equalsIgnoreCase("color")) {
			String hex = new String(str); 
			Color color = Color.decode(hex);
			if (inServices && inService) {
				// add name to stack
				service.setColor(color);
			} else if (inLayer) {
				layer.setColor(color);
			}
		}
		
	}

	/**
	 * Reading characters 
	 */
	@Override
	public void characters(char ch[], int start, int length) 
		throws SAXException 
	{
		str = new String(ch, start, length);
		sw.write(str);
	}
	
	/**
	 * Returns an array of the lat and long given a string representation, 
	 * e.g. "16.03 -107"
	 * 
	 * @param str
	 * @return
	 */
	private float[] getLatLong(String str) {
		float[] res = new float[2];
		String[] latlongData = str.split(" ");
		res[0] = Float.parseFloat(latlongData[0]);
		res[1] = Float.parseFloat(latlongData[1]);
		return res; 
	}
	
	public static void main(String[] args) {
		new LayerConfigParser().createProjectsModel(new File("projects.xml"));
	}
	
}

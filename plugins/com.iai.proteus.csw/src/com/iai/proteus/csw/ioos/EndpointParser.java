/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.csw.ioos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.ServiceType;

/**
 * Simple XPath-based parser for extracting end-points for a CSW response
 * 
 * @author Jakob Henriksson
 *
 */
public class EndpointParser {

	/**
	 * Constructor
	 * 
	 */
	public EndpointParser() {
	
	}
	
	/**
	 * Finds end points in an XML response from a CSW 
	 * 
	 * @param xmlResponse
	 * @return
	 */
	public Collection<Service> findEndpoints(String xmlResponse) {
		
		Collection<Service> services = new HashSet<Service>();
		
		// basic check 
		if (xmlResponse == null || xmlResponse.equals(""))
			return services; 

		// XPath query 
		String xPath = "//connectPoint//URL/node()";
		
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			
			builder = factory.newDocumentBuilder();

			// create input stream from XML response 
			InputStream is = new ByteArrayInputStream(xmlResponse.getBytes("UTF-8"));

			Document doc = builder.parse(is);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			xpath.setNamespaceContext(new NamespaceResolver());
			
			XPathExpression expr = xpath.compile(xPath);
			
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				String text = node.getTextContent();
				if (text != null) {
					// trim
					text = text.trim();
					// cut 
					if (text.contains("?"))
						text = text.substring(0, text.indexOf("?"));
					if (text.equals(""))
						continue;
					// create service
					Service service = new Service(ServiceType.SOS);
					service.setEndpoint(text);
					// automatically activate
					service.activate();
					// automatically hide the services from service managers 
					service.hide();
					// add service 
					services.add(service);
				}
			}
			
//			final long startTime = System.currentTimeMillis();
//
//			int count = 0; 
//			for (Service s : services) {
////				System.out.println("Service: " + s.getEndpoint());
//				SosCapabilities caps = SosUtil.getCapabilities(s.getEndpoint());
//				if (caps != null) {
//					count++;
//				}
//			}
//			System.out.println("Number of services found: " + services.size());
//			System.out.println("Number of responsive SOS services: " + count);
//			
//			final long endTime = System.currentTimeMillis();
//			System.out.println("Total execution time: " + (endTime - startTime) );
			
			System.out.println("Returning with no error...");
			
			return services;
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// default
		return new HashSet<Service>();
	}
	
	private class NamespaceResolver implements NamespaceContext {

		/**
		 * This method returns the uri for all prefixes needed. Wherever possible
		 * it uses XMLConstants.
		 * 
		 * @param prefix
		 * @return uri
		 */
		public String getNamespaceURI(String prefix) {
			if (prefix == null) {
				throw new IllegalArgumentException("No prefix provided!");
			} 
			//			else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			//				return "http://univNaSpResolver/book";
			//			} 
			else if (prefix.equals("srv")) {
				return "http://www.isotc211.org/2005/srv";
			} else if (prefix.equals("csw")) {
				return "http://www.opengis.net/cat/csw/2.0.2";
			} else if (prefix.equals("gmd")) {
				return "http://www.isotc211.org/2005/gmd";
			} else {
				return XMLConstants.NULL_NS_URI;
			}
		}

		public String getPrefix(String namespaceURI) {
			// Not needed in this context.
			return null;
		}

		public Iterator<?> getPrefixes(String namespaceURI) {
			// Not needed in this context.
			return null;
		}

	}


}

/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.wss;

import gov.nasa.worldwind.util.xml.StringSetXMLEventParser;

import javax.xml.namespace.QName;

/**
 * Parses the World Wind Web Shape Service (WSS) NamedFeatures and Feature elements and provides access to their as a
 * set of feature names.
 *
 * @author dcollins
 * @version $Id: WSSNamedFeatures.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class WSSNamedFeatures extends StringSetXMLEventParser
{
    public WSSNamedFeatures(String namespaceURI)
    {
        super(namespaceURI, new QName(namespaceURI, "Feature"));
    }
}

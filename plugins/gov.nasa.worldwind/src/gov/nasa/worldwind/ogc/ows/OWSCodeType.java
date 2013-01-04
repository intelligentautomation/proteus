/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * Parses an OGC Web Service Common (OWS) CodeType element and provides access to its contents. See
 * http://schemas.opengis.net/ows/2.0/ows19115subset.xsd.
 *
 * @author dcollins
 * @version $Id: OWSCodeType.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class OWSCodeType extends AbstractXMLEventParser
{
    public OWSCodeType(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getCode()
    {
        return this.getCharacters();
    }

    public String getCodeSpace()
    {
        return (String) this.getField("codeSpace");
    }
}

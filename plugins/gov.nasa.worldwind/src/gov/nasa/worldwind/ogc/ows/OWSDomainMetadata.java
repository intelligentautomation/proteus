/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * Parses an OGC Web Service Common (OWS) DomainMetadataType element and provides access to its contents. See
 * http://schemas.opengis.net/ows/2.0/owsDomainType.xsd.
 *
 * @author dcollins
 * @version $Id: OWSDomainMetadata.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class OWSDomainMetadata extends AbstractXMLEventParser
{
    public OWSDomainMetadata(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getText()
    {
        return this.getCharacters();
    }

    public String getReference()
    {
        return (String) this.getField("reference");
    }
}

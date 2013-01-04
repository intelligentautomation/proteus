/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.ows;

/**
 * Parses an OGC Web Service Common (OWS) NoValues element and provides access to its contents. See
 * http://schemas.opengis.net/ows/2.0/owsDomainType.xsd.
 *
 * @author dcollins
 * @version $Id: OWSNoValues.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class OWSNoValues extends OWSPossibleValues
{
    public OWSNoValues(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    public boolean isNoValues()
    {
        return true;
    }

    @Override
    public OWSNoValues asNoValues()
    {
        return this;
    }
}

/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml.xal;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: XALAbstractObject.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class XALAbstractObject extends AbstractXMLEventParser
{
    public XALAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getType()
    {
        return (String) this.getField("Type");
    }

    public String getCode()
    {
        return (String) this.getField("Code");
    }
}

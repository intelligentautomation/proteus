/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml.xal;

/**
 * @author tag
 * @version $Id: XALCountryNameCode.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class XALCountryNameCode extends XALAbstractObject
{
    public XALCountryNameCode(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getScheme()
    {
        return (String) this.getField("Scheme");
    }
}

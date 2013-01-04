/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.gx;

/**
 * @author tag
 * @version $Id: GXTourControl.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class GXTourControl extends GXAbstractTourPrimitive
{
    public GXTourControl(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getPlayMode()
    {
        return (String) this.getField("playMode");
    }
}

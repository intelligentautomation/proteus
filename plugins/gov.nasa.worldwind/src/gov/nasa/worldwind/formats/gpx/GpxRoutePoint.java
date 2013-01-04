/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.gpx;

/**
 * @author tag
 * @version $Id: GpxRoutePoint.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class GpxRoutePoint extends GpxTrackPoint
{
    public GpxRoutePoint(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        super("rtept", uri, lname, qname, attributes);
    }
}

/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;

/**
 * @author tag
 * @version $Id: GXTour.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class GXTour extends KMLAbstractFeature
{
    public GXTour(String namespaceURI)
    {
        super(namespaceURI);
    }

    public GXPlaylist getPlaylist()
    {
        return (GXPlaylist) this.getField("Playlist");
    }
}

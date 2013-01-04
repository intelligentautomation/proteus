/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.*;

/**
 * @author tag
 * @version $Id: PointOfInterest.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface PointOfInterest extends WWObject
{
    LatLon getLatlon();
}

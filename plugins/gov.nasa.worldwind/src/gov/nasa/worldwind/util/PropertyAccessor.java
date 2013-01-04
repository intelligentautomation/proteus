/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;

/**
 * @author jym
 * @version $Id: PropertyAccessor.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class PropertyAccessor
{

    public PropertyAccessor()
    {
    }

    public static interface AngleAccessor
    {
        Angle getAngle();

        boolean setAngle(Angle value);
    }

    public static interface DoubleAccessor
    {
        Double getDouble();

        boolean setDouble(Double value);
    }

    public static interface PositionAccessor
    {
        Position getPosition();

        boolean setPosition(Position value);
    }
}

/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindx.examples.elevations;

import gov.nasa.worldwind.geom.Position;

/**
 * @author Lado Garakanidze
 * @version $Id: GetElevationsPostProcessor.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface GetElevationsPostProcessor
{
    public void onSuccess( Position[] positions );

    public void onError( String error );
}

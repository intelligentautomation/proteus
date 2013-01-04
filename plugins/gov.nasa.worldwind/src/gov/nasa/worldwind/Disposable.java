/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind;

/**
 * @author tag
 * @version $Id: Disposable.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface Disposable
{
    /** Disposes of any internal resources allocated by the object. */
    public void dispose();
}

/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind;

import gov.nasa.worldwind.cache.*;

import javax.media.opengl.GLAutoDrawable;

/**
 * @author tag
 * @version $Id: WorldWindowGLDrawable.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface WorldWindowGLDrawable extends WorldWindow
{
    void initDrawable(GLAutoDrawable glAutoDrawable);

    void initGpuResourceCache(GpuResourceCache cache);

    void endInitialization();
}

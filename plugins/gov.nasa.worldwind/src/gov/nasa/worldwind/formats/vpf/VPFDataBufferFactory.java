/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFDataBufferFactory.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface VPFDataBufferFactory
{
    VPFDataBuffer newDataBuffer(int numRows, int elementsPerRow);
}

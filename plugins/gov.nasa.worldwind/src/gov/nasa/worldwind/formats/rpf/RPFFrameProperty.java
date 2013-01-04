/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

/**
 * @author dcollins
 * @version $Id: RPFFrameProperty.java 1 2011-07-16 23:22:47Z dcollins $
 */
public enum RPFFrameProperty
{
    DATA_SERIES
    {
        public Object getValue(RPFFrameFilename frameFilename)
        {
            if (frameFilename == null)
                return null;
            return frameFilename.getDataSeriesCode();
        }
    },
    FRAME_NUMBER
    {
        public Object getValue(RPFFrameFilename frameFilename)
        {
            if (frameFilename == null)
                return null;
            return frameFilename.getFrameNumber();
        }
    },
    PRODUCER
    {
        public Object getValue(RPFFrameFilename frameFilename)
        {
            if (frameFilename == null)
                return null;
            return frameFilename.getProducerId();
        }
    },
    VERSION
    {
        public Object getValue(RPFFrameFilename frameFilename)
        {
            if (frameFilename == null)
                return null;
            return frameFilename.getVersion();
        }
    },
    ZONE
    {
        public Object getValue(RPFFrameFilename frameFilename)
        {
            if (frameFilename == null)
                return null;
            return frameFilename.getZoneCode();
        }
    };

    public abstract Object getValue(RPFFrameFilename frameFilename);
}

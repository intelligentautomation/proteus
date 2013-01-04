/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFTripletId.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class VPFTripletId
{
    private int id;
    private int tileId;
    private int extId;

    public VPFTripletId(int id, int tileId, int extId)
    {
        this.id = id;
        this.tileId = tileId;
        this.extId = extId;
    }

    public int getId()
    {
        return this.id;
    }

    public int getTileId()
    {
        return this.tileId;
    }

    public int getExtId()
    {
        return this.extId;
    }
}

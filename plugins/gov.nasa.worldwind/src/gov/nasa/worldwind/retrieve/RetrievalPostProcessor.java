/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

/**
 * @author Tom Gaskins
 * @version $Id: RetrievalPostProcessor.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface RetrievalPostProcessor
{
    public java.nio.ByteBuffer run(Retriever retriever);
}

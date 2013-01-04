/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author tag
 * @version $Id: RetrieverFactory.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface RetrieverFactory
{
    Retriever createRetriever(AVList params, RetrievalPostProcessor postProcessor);
}

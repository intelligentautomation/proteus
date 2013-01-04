/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: DataStoreProducer.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface DataStoreProducer extends WWObject
{
    AVList getStoreParameters();

    void setStoreParameters(AVList parameters);

    String getDataSourceDescription();

    Iterable<Object> getDataSources();

    boolean acceptsDataSource(Object source, AVList params);

    boolean containsDataSource(Object source);

    void offerDataSource(Object source, AVList params);

    void offerAllDataSources(Iterable<?> sources);

    void removeDataSource(Object source);

    void removeAllDataSources();

    void startProduction() throws Exception;

    void stopProduction();

    Iterable<?> getProductionResults();

    AVList getProductionParameters();

    void removeProductionState();
}

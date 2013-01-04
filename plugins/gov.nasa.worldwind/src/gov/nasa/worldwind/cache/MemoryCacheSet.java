/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.PerformanceStatistic;

import java.util.*;

/**
 * @author tag
 * @version $Id: MemoryCacheSet.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface MemoryCacheSet
{
    boolean containsCache(String key);

    MemoryCache getCache(String cacheKey);

    MemoryCache addCache(String key, MemoryCache cache);

    Collection<PerformanceStatistic> getPerformanceStatistics();

    void clear();

    Map<String, MemoryCache> getAllCaches();
}

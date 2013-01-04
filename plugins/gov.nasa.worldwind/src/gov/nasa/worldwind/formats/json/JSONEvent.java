/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

/**
 * @author dcollins
 * @version $Id: JSONEvent.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface JSONEvent
{
    boolean isStartObject();

    boolean isEndObject();

    boolean isStartArray();

    boolean isEndArray();

    boolean isFieldName();

    boolean isScalarValue();

    boolean isNumericValue();

    String getFieldName();

    Object asScalarValue();

    double asNumericValue();
}

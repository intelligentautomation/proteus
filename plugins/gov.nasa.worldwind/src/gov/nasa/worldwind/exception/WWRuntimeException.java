/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.exception;

/**
 * @author Tom Gaskins
 * @version $Id: WWRuntimeException.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class WWRuntimeException extends RuntimeException
{
    public WWRuntimeException()
    {
    }

    public WWRuntimeException(String s)
    {
        super(s);
    }

    public WWRuntimeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public WWRuntimeException(Throwable throwable)
    {
        super(throwable);
    }
}

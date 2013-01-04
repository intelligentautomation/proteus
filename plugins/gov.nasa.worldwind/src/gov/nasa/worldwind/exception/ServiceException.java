/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.exception;

/**
 * Thrown to indicate a service has failed.
 *
 * @author tag
 * @version $Id: ServiceException.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class ServiceException extends WWRuntimeException
{
    public ServiceException(String message)
    {
        super(message);
    }
}

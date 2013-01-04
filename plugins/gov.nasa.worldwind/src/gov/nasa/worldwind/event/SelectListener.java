/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.event;

import java.util.EventListener;

/**
 * @author tag
 * @version $Id: SelectListener.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface SelectListener extends EventListener
{
    public void selected(SelectEvent event);
}

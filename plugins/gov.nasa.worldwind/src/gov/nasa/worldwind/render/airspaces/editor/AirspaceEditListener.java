/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import java.util.EventListener;

/**
 * @author dcollins
 * @version $Id: AirspaceEditListener.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface AirspaceEditListener extends EventListener
{
    void airspaceMoved(AirspaceEditEvent e);

    void airspaceResized(AirspaceEditEvent e);

    void controlPointAdded(AirspaceEditEvent e);

    void controlPointRemoved(AirspaceEditEvent e);

    void controlPointChanged(AirspaceEditEvent e);
}

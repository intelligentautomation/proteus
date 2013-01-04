/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.awt;

/**
 * @author jym
 * @version $Id: KeyInputActionHandler.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface KeyInputActionHandler
{
    public boolean inputActionPerformed(AbstractViewInputHandler inputHandler, KeyEventState keys, String target,
        ViewInputAttributes.ActionAttributes viewAction);
    public boolean inputActionPerformed(AbstractViewInputHandler inputHandler, java.awt.event.KeyEvent event,
        ViewInputAttributes.ActionAttributes viewAction);

}

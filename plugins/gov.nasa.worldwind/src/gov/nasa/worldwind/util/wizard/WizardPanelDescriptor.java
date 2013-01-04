/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.wizard;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: WizardPanelDescriptor.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface WizardPanelDescriptor
{
    Component getPanelComponent();

    Object getBackPanelDescriptor();

    Object getNextPanelDescriptor();

    void registerPanel(Wizard wizard);

    void aboutToDisplayPanel();

    void displayingPanel();

    void aboutToHidePanel();
}

/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindx.applications.worldwindow.core;

/**
 * @author tag
 * @version $Id: WWMenu.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface WWMenu
{
    void addMenu(String featureID);

    void addMenus(String[] featureIDs);
}

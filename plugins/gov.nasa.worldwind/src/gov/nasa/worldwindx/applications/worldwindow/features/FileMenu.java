/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: FileMenu.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class FileMenu extends AbstractMenu
{
    public FileMenu(Registry registry)
    {
        super("File", Constants.FILE_MENU, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToMenuBar();
    }
}

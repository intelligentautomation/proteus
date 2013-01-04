/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.WWObject;

/**
 * Contents of a {@link Tree}.
 *
 * @author pabercrombie
 * @version $Id: TreeModel.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface TreeModel extends WWObject
{
    /**
     * Get the root node of the tree.
     *
     * @return The root node.
     */
    TreeNode getRoot();
}

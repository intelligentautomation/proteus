/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

/**
 * An animation that can be played in series of steps.
 *
 * @author pabercrombie
 * @version $Id: Animation.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface Animation
{
    /**
     * Reset the animation to the starting state.
     */
    void reset();

    /**
     * Step the animation.
     */
    void step();

    /**
     * Indicates whether or not there more steps left in the animation.
     *
     * @return {@code true} if there are more steps.
     */
    boolean hasNext();
}

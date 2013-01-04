/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.webview;

import java.net.URL;

/**
 * @author pabercrombie
 * @version $Id: WebResourceResolver.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface WebResourceResolver
{
    URL resolve(String address);
}

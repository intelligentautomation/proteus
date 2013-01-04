/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: JSONEventParser.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface JSONEventParser
{
    Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException;
}

/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import org.codehaus.jackson.JsonParser;
import gov.nasa.worldwind.formats.json.*;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: GeoJSONEventParserContext.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class GeoJSONEventParserContext extends BasicJSONEventParserContext
{
    public GeoJSONEventParserContext(JsonParser parser) throws IOException
    {
        super(parser);
        this.registerEventParsers();
    }

    @Override
    public JSONEventParser getUnrecognizedParser()
    {
        return new GeoJSONEventParser();
    }

    protected void registerEventParsers()
    {
        this.registerParser(GeoJSONConstants.FIELD_COORDINATES, new GeoJSONCoordinateParser());
    }
}

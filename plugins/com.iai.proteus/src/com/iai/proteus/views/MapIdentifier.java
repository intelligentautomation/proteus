package com.iai.proteus.views;

import java.util.List;

import com.iai.proteus.model.MapId;

public interface MapIdentifier {

	public List<MapId> getMapIds();
	public MapId getDefaultMapId();
}

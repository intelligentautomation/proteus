/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.plot;

import java.awt.Color;

public enum Spectrum {
	
	FullSpectrum(1, 240, 1, 1, 1, 1),
	RedToGreen(10,60, 1, 1, 1, 1),
	GrayScale(0, 0, 0, 0, 0, 1); 

	private double _min_hue, _hue_span;
	private double _min_saturation, _saturation_span;
	private double _min_brightness, _brightness_span;

	
	private Spectrum(double min_hue, double max_hue, double min_saturation, double max_saturation, double min_brightness, double max_brightness)
	{
		_min_hue = min_hue / 240;
		_hue_span = (max_hue - min_hue) / 240;

		_min_saturation = min_saturation;
		_saturation_span = (max_saturation - min_saturation);
		
		_min_brightness = min_brightness;
		_brightness_span = (max_brightness - min_brightness);
	}

	public int getColor(double fraction) {
		float hue = (float) (_min_hue + (fraction * _hue_span));
		float saturation = (float) (_min_saturation + (fraction * _saturation_span));
		float brightness = (float) (_min_brightness + (fraction * _brightness_span));
		return Color.HSBtoRGB(hue, saturation, brightness);
	}
}

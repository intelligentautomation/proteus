package com.iai.proteus.exceptions;

import java.util.List;

/**
 * Thrown if we do not support a particular response format from a SOS 
 * GetObservation request 
 * 
 * @author Jakob Henriksson
 *
 */
public class ResponseFormatNotSupportedException extends Exception {
	
	private static final long serialVersionUID = 1L;

	private List<String> formats;
	
	/**
	 * Constructor 
	 * 
	 * @param msg
	 * @param unsupportedFormats
	 */
	public ResponseFormatNotSupportedException(String msg, 
			List<String> unsupportedFormats) 
	{
		super(msg);
		this.formats = unsupportedFormats;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getUnsupportedFormats() {
		return formats; 
	}

}

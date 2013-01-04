package com.iai.proteus.model.workspace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Model;

public class Folder extends Model {

	/**
	 * Constructor 
	 * 
	 * @param name
	 */
	public Folder(String name) {
		this.name = name;
	}
	
	@Override 
	public Element serialize(Document document) {
		// TODO: implement serialization of model object 
		return null;
	}	
}

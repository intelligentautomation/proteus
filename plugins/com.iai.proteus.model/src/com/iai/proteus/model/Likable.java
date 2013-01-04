package com.iai.proteus.model;

/**
 * Implementing classes can be favorites 
 * 
 * @author Jakob Henriksson 
 *
 */
public interface Likable {
	
	public void like();
	public void dislike(); 
	public boolean isLiked(); 

}

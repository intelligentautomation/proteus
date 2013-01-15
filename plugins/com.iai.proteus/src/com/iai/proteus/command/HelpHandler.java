/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

/**
 * Handler for executing help commands
 * 
 * @author Jakob Henriksson
 *
 */
public class HelpHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
        if (!java.awt.Desktop.isDesktopSupported()) {
            System.err.println( "Desktop is not supported (fatal)" );
            return null;
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported( java.awt.Desktop.Action.BROWSE )) {
            System.err.println( "Desktop doesn't support the browse action (fatal)" );
            return null;
        }
        
		String url_to_display = event.getParameter("url");

        try {
            java.net.URI uri = new java.net.URI( url_to_display );
            desktop.browse( uri );
        } catch (Exception e) {
            System.err.println( e.getMessage() );
        }
		
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

}

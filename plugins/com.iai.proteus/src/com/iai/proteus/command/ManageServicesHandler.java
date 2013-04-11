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

import com.iai.proteus.dialogs.ManageAllServicesDialog;
import com.iai.proteus.model.services.ServiceRoot;
import com.iai.proteus.ui.UIUtil;

/**
 * Handler for managing services 
 * 
 * @author Jakob Henriksson
 *
 */
public class ManageServicesHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// create and open the dialog to manage services 
		ManageAllServicesDialog dialog =
				new ManageAllServicesDialog(UIUtil.getShell(), 
						ServiceRoot.getInstance());
		dialog.open();
		dialog.close();
		
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

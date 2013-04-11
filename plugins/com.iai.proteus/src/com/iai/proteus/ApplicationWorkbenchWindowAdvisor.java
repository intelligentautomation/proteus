/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	/**
	 * Constructor 
	 * 
	 * @param configurer
	 */
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }
    
//    @Override
//    public void createWindowContents(Shell shell) {
//    	// call standard eclipse method creating window contents
//    	super.createWindowContents(shell);
//    	// find cbanner
//    	CBanner banner = findTopBanner(shell);
//
//    	// create custom perspective switcher
//    	MyPerspectiveSwitcher perspectiveSwitcher = 
//    			new MyPerspectiveSwitcher(getWindowConfigurer().getWindow(), banner);
//    	perspectiveSwitcher.createControl( );
//    }    

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
    	
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1200, 860));
        
        // enable the status bar
        configurer.setShowStatusLine(true);
        // enable progress indicator in the status bar
        configurer.setShowProgressIndicator(true);
        configurer.setShowPerspectiveBar(false);
        configurer.setShowCoolBar(true);

        configurer.setTitle("Proteus"); //$NON-NLS-1$
    }
    
    @Override
    public void postWindowOpen() {
    	
    }
    
    /**
     * Find CBanner among children of the given shell.
     */
//    private CBanner findTopBanner( Shell shell ) {
//    	Control[] children = shell.getChildren();
//    	CBanner result = null;
//    	for (Control child : children) {
//    		if (child instanceof CBanner) {
//    			if (result != null) {
//    				throw new IllegalStateException("More than one CBanner.");
//    			}
//    			result = (CBanner) child;
//    		}
//    	}
//    	if (result == null) {
//    		throw new IllegalStateException("No CBanner.");
//    	}
//    	return result;
//    }    
}

/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private final IWorkbenchWindow window;

//	private IContributionItem viewsShortList;
//	private IContributionItem perspectivesShortList;

    private MenuManager showViewMenuMgr;
    private IContributionItem showViewItem;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();
    }

    protected void makeActions(IWorkbenchWindow window) {

		showViewMenuMgr = new MenuManager("Show View", "showView");
		showViewItem =
			ContributionItemFactory.VIEWS_SHORTLIST.create(window);

//    	register(ActionFactory.ABOUT.create(window));
//    	register(ActionFactory.HELP_SEARCH.create(window));
//		register(ActionFactory.DYNAMIC_HELP.create(window));
    }

    protected void fillMenuBar(IMenuManager menuBar) {

//    	MenuManager helpMenu =
//    			new MenuManager("&Help",
//    					IWorkbenchActionConstants.M_HELP);
//
//        menuBar.add(helpMenu);
//        helpMenu.add(aboutAction);

//    	super.makeActions(window);
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
    }

}

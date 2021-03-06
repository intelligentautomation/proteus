/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iai.proteus.Activator;
import com.iai.proteus.PreferenceConstants;

/**
 * Preference page for the Community Hub 
 * 
 * @author Jakob Henriksson
 *
 */
public class PrefPageCommunityHub extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage
{
	// page ID 
	public static final String ID = "com.iai.proteus.preferences.community_hub.page";

	private StringFieldEditor stringFieldEditor;

	/**
	 * Constructor
	 * 
	 */
	public PrefPageCommunityHub() {

	}

	@Override
	public void createFieldEditors() {
		{
			stringFieldEditor =
					new StringFieldEditor(PreferenceConstants.prefCommunityHub,
							"Community Hub URL", -1,
							StringFieldEditor.VALIDATE_ON_KEY_STROKE,
							getFieldEditorParent());
			addField(stringFieldEditor);
		}
	}

	/**
	 * Set defaults
	 *
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		stringFieldEditor.setStringValue(store.getDefaultString(PreferenceConstants.prefCommunityHub));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("The Community Hub is a service used for retrieving service listings, " +
				"quert for alerts and for collaborating with peers.");
	}

}


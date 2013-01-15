/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.iai.proteus.Activator;
import com.iai.proteus.PreferenceConstants;

public class PrefPageMain extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public PrefPageMain() {
		super(GRID);
	}

	public void createFieldEditors() {

		addField(new IntegerFieldEditor(PreferenceConstants.prefConnectionTimeout,
				"Connection timeout (seconds)",
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.prefConnectionReadTimeout,
				"Connection read timeout (seconds)",
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
//		setDescription("General preferences.");
	}
}


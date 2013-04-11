/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.iai.proteus.p2.CloudPolicy;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.iai.proteus"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	// Context
	private static BundleContext context;

	// Shared state location
	public static IPath stateLocation;

	ServiceRegistration<?> policyRegistration;
	CloudPolicy policy;
	IPropertyChangeListener preferenceListener;

	/**
	 * The constructor
	 */
	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Activator.plugin = this;
		Activator.context = context;
		stateLocation = getStateLocation();

		registerP2Policy(context);
		getPreferenceStore().addPropertyChangeListener(getPreferenceListener());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Activator.plugin = null;
		Activator.context = null;
		// unregister the UI policy
		policyRegistration.unregister();
		policyRegistration = null;
		getPreferenceStore().removePropertyChangeListener(preferenceListener);
		preferenceListener = null;
		super.stop(context);
	}

	/**
	 *
	 * @param context
	 */
	private void registerP2Policy(BundleContext context) {
		policy = new CloudPolicy();
		policy.updateForPreferences();
		Dictionary<String, Integer> props = new Hashtable<String, Integer>();
		props.put(org.osgi.framework.Constants.SERVICE_RANKING,
				new Integer(99));
		policyRegistration = context.registerService(Policy.class.getName(),
				policy, props);
	}

	/**
	 *
	 * @return
	 */
	private IPropertyChangeListener getPreferenceListener() {
		if (preferenceListener == null) {
			preferenceListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					policy.updateForPreferences();
				}
			};
		}
		return preferenceListener;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns context
	 *
	 * @return
	 */
	public static BundleContext getContext() {
		return context;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}

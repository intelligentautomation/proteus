/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * User Interface utilities
 *
 * @author Jakob Henriksson
 *
 */
public class UIUtil {

	public static List<Color> colors = new ArrayList<Color>() {
		private static final long serialVersionUID = 1L;
		{
			add(Color.decode("#6A4B36"));
			add(Color.decode("#BC967C"));
			add(Color.decode("#3C703C"));
			add(Color.decode("#9EE7A8"));
			add(Color.decode("#7F3F6C"));
			add(Color.decode("#CCAA04"));
			add(Color.decode("#FCE46E"));
			add(Color.decode("#B5B5B5"));
			add(Color.decode("#BC967C"));
			add(Color.decode("#0699CC"));
			add(Color.decode("#012937"));
			add(Color.decode("#384985"));
			add(Color.decode("#F9645B"));
			add(Color.decode("#3275E4"));
			add(Color.decode("#B9D0F5"));
			add(Color.decode("#8B8032"));
			add(Color.decode("#9AFF7F"));
			add(Color.decode("#8CA5AC"));
		}
	};

	/**
	 * Execute the runnable in the UI thread
	 *
	 * @param r
	 */
	public static void update(Runnable r) {
		if (r != null) {
			/* make sure the runnable is executed on the current thread */
			if (Display.getCurrent() != null) {
				r.run();
			} else {
				Display.getDefault().asyncExec(r);
			}
		}
	}

	/**
	 * Updates the status bar with the given message
	 *
	 * @param viewSite
	 * @param msg
	 */
	public static void updateStatus(final IViewSite viewSite,
			final String msg)
	{

		/* construct runnable object to update the status bar */
		Runnable r = new Runnable() {
			public void run() {
				viewSite.getActionBars().getStatusLineManager().
	        		setMessage(msg);
			}
		};

		/* make sure the runnable is executed on the current thread */
		if (Display.getCurrent() != null) {
			r.run();
		} else {
			Display.getDefault().asyncExec(r);
		}

	}

	/**
	 * Returns a confirmation dialog box with "YES" and "NO" buttons
	 * 
	 * The dialog returns MessageDialog.OK or MessageDialog.CANCEL
	 *
	 * @param shell
	 * @param title
	 * @param msg
	 * @return
	 */
	public static MessageDialog getConfirmDialog(Shell shell, String title, String msg) {
		MessageDialog dialog =
			new MessageDialog(shell,
					title, null, msg, MessageDialog.CONFIRM,
					new String[] { "Yes", "No" }, 0);
		return dialog;
	}

	/**
	 * Displays an error message
	 *
	 * @param msg
	 */
	public static void showErrorMessage(final String msg) {
		update(new Runnable() {
			public void run() {
				Shell shell =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//		Shell shell = new Shell(Display.getCurrent(), SWT.NONE);
				MessageBox messageBox =
					new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				messageBox.setMessage(msg);
				messageBox.setText("Sorry, but an error occured...");
				messageBox.open();
			}
		});
	}

	/**
	 * Displays an information dialog box
	 *
	 * @param msg
	 */
	public static void showInfoMessage(String msg) {
		showInfoMessage("Information", msg);
	}
	
	/**
	 * Displays an information dialog box
	 *
	 * @param msg
	 */
	public static void showInfoMessage(String title, String msg) {
		Shell shell =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openInformation(shell, title, msg);
	}	

	/**
	 *
	 * @return
	 */
	public static Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Returns an image from the specified location
	 *
	 * @param location e.g. "icons/source.png"
	 * @return
	 */
	public static Image getImage(String location) {
		return getImageDescriptor(location).createImage();
	}

	/**
	 * Returns an image from the specified location
	 *
	 * @param bundleId The bundle ID
	 * @param location e.g. "icons/source.png"
	 * @return
	 */
	public static Image getImage(String bundleId, String location) {
		return getImageDescriptor(bundleId, location).createImage();
	}

	/**
	 * Returns an image descriptor from the specified location
	 *
	 * @param location e.g. "icons/source.png"
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(String location) {
		// uses the main bundle as default
		return getImageDescriptor("com.iai.proteus", location);
	}

	/**
	 * Returns an image descriptor from the specified location
	 *
	 * @param bundleId
	 * @param location e.g. "icons/source.png"
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(String bundleId, String location) {
		return ImageDescriptor.createFromURL(
				FileLocator.find(Platform.getBundle(bundleId),
						new Path(location), null));
	}

	/**
	 * Converts RGB object to a Color object
	 *
	 * @param rgb
	 * @return
	 */
	public static Color colorFromRGB(RGB rgb) {
		return new Color(rgb.red, rgb.green, rgb.blue);
	}

	/**
	 * Logs a message at ERROR level
	 *
	 * @param pluginId
	 * @param msg
	 */
	public static void log(String pluginId, String msg) {
		log(pluginId, IStatus.ERROR, msg);
	}


	/**
	 * Logs a message
	 *
	 * @param pluginId
	 * @param level log level
	 * @param msg
	 */
	public static void log(String pluginId, int level, String msg) {
		Status status = new Status(level, pluginId, msg);
		StatusManager.getManager().handle(status,
				StatusManager.LOG);
	}

	/**
	 * Makes the label bold (assumed to be executed on the UI thread)
	 *
	 * @param lbl
	 */
	public static void bolden(Label lbl) {
		FontData[] fontData = lbl.getFont().getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setStyle(SWT.BOLD);
		}
		final Font font = new Font(lbl.getDisplay(), fontData);
		lbl.setFont(font);

		// Since you created the font, you must dispose it
		lbl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				font.dispose();
			}
		});
	}

	/**
	 * Makes the label bold (assumed to be executed on the UI thread)
	 *
	 * @param lbl
	 */
	public static void unbolden(Label lbl) {
		FontData[] fontData = lbl.getFont().getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setStyle(SWT.NORMAL);
		}

		final Font font = new Font(lbl.getDisplay(), fontData);
		lbl.setFont(font);

		// Since you created the font, you must dispose it
		lbl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				font.dispose();
			}
		});
	}

	/**
	 * Helper method to implement standard selection behavior for a Tree
	 *
	 * @param item
	 * @param checked
	 * @param grayed
	 */
	public static void checkPath(TreeItem item, boolean checked, boolean grayed) {
	    if (item == null) return;
	    if (grayed) {
	        checked = true;
	    } else {
	        int index = 0;
	        TreeItem[] items = item.getItems();
	        while (index < items.length) {
	            TreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) {
	                checked = grayed = true;
	                break;
	            }
	            index++;
	        }
	    }
	    item.setChecked(checked);
	    item.setGrayed(grayed);
	    checkPath(item.getParentItem(), checked, grayed);
	}

	/**
	 * Helper method to implement standard selection behavior for a Tree
	 *
	 * @param item
	 * @param checked
	 */
	public static void checkItems(TreeItem item, boolean checked) {
	    item.setGrayed(false);
	    item.setChecked(checked);
	    TreeItem[] items = item.getItems();
	    for (int i = 0; i < items.length; i++) {
	        checkItems(items[i], checked);
	    }
	}

}

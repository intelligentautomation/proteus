package com.iai.proteus.util;

import java.net.URL;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import com.iai.proteus.Activator;

public class Resources {
	
	static Logger log = Logger.getLogger(Resources.class);

	/**
	 * Loads the image specified by the resource path, returns null
	 * if there was an error 
	 * 
	 * @param resource
	 * @return
	 */
	public static Image loadImage(String resource) {
		
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path(resource); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);		

		if (url != null) 
			return ImageDescriptor.createFromURL(url).createImage();
		
		log.warn("Error loading bundled resource: " + resource);
		
		// default
		return null;
	}
	
}

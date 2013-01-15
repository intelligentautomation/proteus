/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.map.wms;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.util.WWUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.iai.proteus.model.event.WorkspaceEventNotifier;
import com.iai.proteus.model.event.WorkspaceEventType;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.services.WmsMapLayer;

public class WMSUtil {
	
//	private static final Logger log = Logger.getLogger(WMSUtil.class);
	
	public static void test2(final Service service) {
		Job job = new Job("Finding layers") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {

					monitor.beginTask("Finding layers", 
							IProgressMonitor.UNKNOWN);

//					test3(service);

				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();		
	}
	
	
//	public static Collection<LayerInfo> test3(Service service) {
//		
//		String serviceUrl = service.getServiceUrl();
//
//		
//		WMSCache cache = WMSCache.getInstance();
//
//		if (cache.contains(serviceUrl)) {
//
//			return cache.get(serviceUrl);
//
//		} else {
//
//			try {
//
//				String capabilities = Util.get(serviceUrl);
//				File tmp = File.createTempFile("smt", "wms");
//				FileUtils.write(tmp, capabilities);
//
//				Collection<LayerInfo> layerInfos = WMSUtil.getLayers(tmp);
//
//				cache.commit(serviceUrl, layerInfos);
//				
//				return layerInfos;
//
//			} catch (MalformedURLException e) {
//				UIUtil.showErrorMessage("There was an error fetching the data");
//				log.error("Malformed URL: " + e.getMessage());
//			} catch (IOException e) {
//				UIUtil.showErrorMessage("There was an error fetching the data");
//				log.error("IOException: " + e.getMessage());
//			}
//
//		}
//
//		if (layerInfos != null) {
//
//			treeLayers.removeAll();
//
//			for (LayerInfo layer : layerInfos) {
//				TreeItem resultItem = new TreeItem(treeLayers, SWT.NONE);
//				resultItem.setText(layer.getTitle());
//				Service service = new Service(ServiceType.WMS);
//				service.setServiceUrl(serviceUrl);
//				resultItem.setData("service", service);
//				resultItem.setData("layer", layer);
//			}
//		}
//	}					

	
	/**
	 * Loads layers from a WMS 
	 * 
	 * @param service
	 * @return
	 */
	public static Object[] loadLayersFromWMS(Service service) {
		
		Collection<LayerInfo> layerInfos = 
				WMSUtil.getLayers(service.getServiceUrl());
		
		if (layerInfos == null || layerInfos.size() == 0)
			// empty 
			return new Object[0];
		
		Collection<WmsMapLayer> mapLayers = new ArrayList<WmsMapLayer>();
		for (LayerInfo layerInfo : layerInfos) {
			WmsMapLayer mapLayer = new WmsMapLayer();
			// set name of the model object 
			mapLayer.setName(layerInfo.getTitle());
			// set name of the WMS layer 
			mapLayer.setWmsLayerName(layerInfo.getName());
			mapLayer.setParent(service);
			
			mapLayers.add(mapLayer);
		}
		
		return mapLayers.toArray(new WmsMapLayer[mapLayers.size()]);
	}	

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static Collection<LayerInfo> getLayers(File file) {
		try {
			WMSCapabilities caps = new WMSCapabilities(file);
			caps.parse();
			return getLayers(caps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static String getTitleFromCache(String serviceUrl) {
		WMSCache cache = WMSCache.getInstance();
		if (cache.contains(serviceUrl)) {
			WMSCapabilities capabilities = cache.get(serviceUrl);
			return capabilities.getServiceInformation().getServiceTitle();
		}
		return null;
	}

	/**
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static Collection<LayerInfo> getLayers(String serviceUrl) {

		// check cache 
		WMSCache cache = WMSCache.getInstance();
		if (cache.containsLayers(serviceUrl))
			return cache.getLayers(serviceUrl);
		
        try
        {
            WMSCapabilities caps = WMSCapabilities.retrieve(new URI(serviceUrl));
            caps.parse(); 
            
            Collection<LayerInfo> layerInfos = getLayers(caps);

            // commit Capabilities document 
            cache.commit(serviceUrl, caps);
            // commit layers 
            cache.commitLayers(serviceUrl, layerInfos);
        
            return layerInfos;
        }
        catch (URISyntaxException e) 
        {
        	e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
	}
	
	
	/**
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static Collection<LayerInfo> getLayers(WMSCapabilities caps) {
		
		Collection<LayerInfo> layerInfos = new ArrayList<LayerInfo>();
		
        // Gather up all the named layers and make a world wind layer for each.
        final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();
        if (namedLayerCaps == null)
            return null;

        try {
            for (WMSLayerCapabilities lc : namedLayerCaps) {
            	
                Set<WMSLayerStyle> styles = lc.getStyles();
                if (styles == null || styles.size() == 0)
                {
                    LayerInfo layerInfo = createLayerInfo(caps, lc, null);
                    layerInfos.add(layerInfo);
                }
                else
                {
                    for (WMSLayerStyle style : styles)
                    {
                        LayerInfo layerInfo = createLayerInfo(caps, lc, style);
                        layerInfos.add(layerInfo);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        
        for (LayerInfo info : layerInfos) {
        	System.out.println("NAME:" + info.getName()); 
        }
        
        return layerInfos;
	} 
	
	/**
	 * 
	 * @param serviceUrl
	 * @param layerName
	 * @return
	 */
	public static LayerInfo getLayer(String serviceUrl, String layerName) 
	{
		
		Collection<LayerInfo> layerInfos = getLayers(serviceUrl);
		for (LayerInfo layerInfo : layerInfos) {
			if (layerInfo.getName().equals(layerName))
				return layerInfo; 
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @param service
	 * @param name
	 */
	public static void toggleLayer(Service service, LayerInfo layerInfo, boolean checked) {

		WorkspaceEventNotifier.getInstance().fireEvent(layerInfo, 
				WorkspaceEventType.WORKSPACE_WMS_TOGGLE, checked);

	}

	private void test() {
		
		String server = "http://firefly.geog.umd.edu/wms/wms";
		
		Collection<LayerInfo> layerInfos = new ArrayList<LayerInfo>();
		
        WMSCapabilities caps;

        try
        {
            caps = WMSCapabilities.retrieve(new URI(server));
            caps.parse();
        }
        catch (URISyntaxException e) 
        {
        	e.printStackTrace();
        	return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        
        
        // Gather up all the named layers and make a world wind layer for each.
        final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();
        if (namedLayerCaps == null)
            return;

        try
        {
            for (WMSLayerCapabilities lc : namedLayerCaps)
            {
                Set<WMSLayerStyle> styles = lc.getStyles();
                if (styles == null || styles.size() == 0)
                {
                    LayerInfo layerInfo = createLayerInfo(caps, lc, null);
                    layerInfos.add(layerInfo);
                }
                else
                {
                    for (WMSLayerStyle style : styles)
                    {
                        LayerInfo layerInfo = createLayerInfo(caps, lc, style);
                        layerInfos.add(layerInfo);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        
        
        for (LayerInfo info : layerInfos) {
        	System.out.println("NAME:" + info.getName()); 
        }
	}
	
    protected static LayerInfo createLayerInfo(WMSCapabilities caps, 
    		WMSLayerCapabilities layerCaps, WMSLayerStyle style)
    {
        // Create the layer info specified by the layer's capabilities 
    	// entry and the selected style.

        LayerInfo linfo = new LayerInfo();
        linfo.caps = caps;
        linfo.params = new AVListImpl();
        linfo.params.setValue(AVKey.LAYER_NAMES, layerCaps.getName());
        if (style != null)
            linfo.params.setValue(AVKey.STYLE_NAMES, style.getName());
        String abs = layerCaps.getLayerAbstract();
        if (!WWUtil.isEmpty(abs))
            linfo.params.setValue(AVKey.LAYER_ABSTRACT, abs);

        linfo.params.setValue(AVKey.DISPLAY_NAME, makeTitle(caps, linfo));

        return linfo;
    }

    protected static String makeTitle(WMSCapabilities caps, LayerInfo layerInfo)
    {
        String layerNames = layerInfo.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = layerInfo.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            WMSLayerCapabilities lc = caps.getLayerByName(layerName);
            String layerTitle = lc.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;

            String styleName = sNames[i];
            WMSLayerStyle style = lc.getStyleByName(styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }
    
	/**
	 * 
	 * 
	 * @param layerInfo
	 * @return
	 */
	public static Object getWMSLayer(LayerInfo layerInfo) {
		
		AVList params = layerInfo.getParams();
        AVList configParams = params.copy(); // Copy to insulate changes from the caller.

        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        try {
        	
        	WMSCapabilities caps = layerInfo.getCapabilities();
        	
            String factoryKey = getFactoryKeyForCapabilities(caps);
            Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey);
            
            System.out.println("Contacting service...");
            
            return factory.createFromConfigSource(caps, configParams);
        }
        catch (Exception e)
        {
            // Ignore the exception, and just return null.
        }

        return null;
		
	}   
	
	/**
	 * 
	 * @param caps
	 * @return
	 */
    protected static String getFactoryKeyForCapabilities(WMSCapabilities caps)
    {
        boolean hasApplicationBilFormat = false;

        Set<String> formats = caps.getImageFormats();
        for (String s : formats)
        {
            if (s.contains("application/bil"))
            {
                hasApplicationBilFormat = true;
                break;
            }
        }

        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }	
	
	
	public static void main(String[] args) {
		new WMSUtil().test();
	}

}

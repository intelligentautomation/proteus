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

import org.apache.log4j.Logger;

import com.iai.proteus.model.event.WorkspaceEventNotifier;
import com.iai.proteus.model.event.WorkspaceEventType;
import com.iai.proteus.model.map.WmsMapLayer;
import com.iai.proteus.model.services.Service;

public class WmsUtil {
	
	private static final Logger log = Logger.getLogger(WmsUtil.class);
	
	/**
	 * Loads layers from a WMS 
	 * 
	 * @param service
	 * @return
	 */
	public static Object[] loadLayersFromWMS(Service service) {
		
		Collection<WmsLayerInfo> layerInfos = 
				WmsUtil.getLayers(service.getEndpoint());
		
		if (layerInfos == null || layerInfos.size() == 0)
			// empty 
			return new Object[0];
		
		Collection<WmsMapLayer> mapLayers = new ArrayList<WmsMapLayer>();
		for (WmsLayerInfo layerInfo : layerInfos) {
			WmsMapLayer mapLayer = new WmsMapLayer();
			// set name of the model object 
			mapLayer.setName(layerInfo.getTitle());
			// set name of the WMS layer 
			mapLayer.setWmsLayerTitle(layerInfo.getName());
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
	public static Collection<WmsLayerInfo> getLayers(File file) {
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
		WmsCache cache = WmsCache.getInstance();
		if (cache.contains(serviceUrl)) {
			WMSCapabilities capabilities = cache.get(serviceUrl);
			return capabilities.getServiceInformation().getServiceTitle();
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static Collection<WmsLayerInfo> getLayers(String serviceUrl) {

		// check cache 
		WmsCache cache = WmsCache.getInstance();
		if (cache.containsLayers(serviceUrl))
			return cache.getLayers(serviceUrl);
		
        try {
            WMSCapabilities caps = WMSCapabilities.retrieve(new URI(serviceUrl));
            caps.parse(); 
            
            Collection<WmsLayerInfo> layerInfos = getLayers(caps);

            // commit Capabilities document 
            cache.commit(serviceUrl, caps);
            // commit layers 
            cache.commitLayers(serviceUrl, layerInfos);
        
            return layerInfos;
            
        } catch (URISyntaxException e) {
        	e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
	}
	
	
	/**
	 * From NASA World Wind example 
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static Collection<WmsLayerInfo> getLayers(WMSCapabilities caps) {
		
		Collection<WmsLayerInfo> layerInfos = new ArrayList<WmsLayerInfo>();
		
        // Gather up all the named layers and make a world wind layer for each.
        final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();
        if (namedLayerCaps == null)
            return null;

        try {
            for (WMSLayerCapabilities lc : namedLayerCaps) {
            	
                Set<WMSLayerStyle> styles = lc.getStyles();
                if (styles == null || styles.size() == 0) {
                    WmsLayerInfo layerInfo = createLayerInfo(caps, lc, null);
                    layerInfos.add(layerInfo);
                } else {
                    for (WMSLayerStyle style : styles) {
                        WmsLayerInfo layerInfo = createLayerInfo(caps, lc, style);
                        layerInfos.add(layerInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return layerInfos;
	} 
	
	/**
	 * 
	 * @param serviceUrl
	 * @param layerName
	 * @return
	 */
	public static WmsLayerInfo getLayer(String serviceUrl, String layerName) 
	{
		
		Collection<WmsLayerInfo> layerInfos = getLayers(serviceUrl);
		for (WmsLayerInfo layerInfo : layerInfos) {
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
	public static void toggleLayer(Service service, WmsLayerInfo layerInfo, boolean checked) {

		WorkspaceEventNotifier.getInstance().fireEvent(layerInfo, 
				WorkspaceEventType.WORKSPACE_WMS_TOGGLE, checked);

	}

	/**
	 * From NASA World Wind example
	 * 
	 * @param caps
	 * @param layerCaps
	 * @param style
	 * @return
	 */
    protected static WmsLayerInfo createLayerInfo(WMSCapabilities caps, 
    		WMSLayerCapabilities layerCaps, WMSLayerStyle style)
    {
        // Create the layer info specified by the layer's capabilities 
    	// entry and the selected style.

        WmsLayerInfo linfo = new WmsLayerInfo();
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

    /**
     * From NASA World Wind example 
     * 
     * @param caps
     * @param layerInfo
     * @return
     */
    protected static String makeTitle(WMSCapabilities caps, WmsLayerInfo layerInfo)
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
	 * From NASA World Wind example 
	 * 
	 * @param layerInfo
	 * @return
	 */
	public static Object getWMSLayer(WmsLayerInfo layerInfo) {
		
		AVList params = layerInfo.getParams();
		// copy to insulate changes from the caller
        AVList configParams = params.copy(); 

        // Some wms servers are slow, so increase the timeouts and limits 
        // used by world wind's retrievers.
        configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        try {
        	
        	WMSCapabilities caps = layerInfo.getCapabilities();
        	
            String factoryKey = getFactoryKeyForCapabilities(caps);
            Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey);
            
            log.trace("Contacting WMS...");
            
            return factory.createFromConfigSource(caps, configParams);
            
        } catch (Exception e) {
            // ignore the exception, and just return null.
        }

        return null;
		
	}   
	
	/**
	 * From NASA World Wind example 
	 * 
	 * @param caps
	 * @return
	 */
    protected static String getFactoryKeyForCapabilities(WMSCapabilities caps)
    {
        boolean hasApplicationBilFormat = false;

        Set<String> formats = caps.getImageFormats();
        for (String s : formats) {
            if (s.contains("application/bil")) {
                hasApplicationBilFormat = true;
                break;
            }
        }

        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }	
	
}

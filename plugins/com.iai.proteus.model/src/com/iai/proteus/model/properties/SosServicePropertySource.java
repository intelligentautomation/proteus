package com.iai.proteus.model.properties;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iai.proteus.common.Util;
import com.iai.proteus.common.sos.SosCapabilitiesCache;
import com.iai.proteus.common.sos.model.ServiceIdentification;
import com.iai.proteus.common.sos.model.ServiceProvider;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.model.services.Service;

/**
 * Property provider for Source objects 
 * 
 * @author Jakob Henriksson
 *
 */
public class SosServicePropertySource implements IPropertySource {
	
	private final String CAT_PROVIDER = "Provider";
	private final String CAT_SERVICE = "Service";
	private final String CAT_CONTACT= "Contact";
	
	private final String PROP_PROVIDER_NAME = "provider_name";
	private final String PROP_PROVIDER_SITE = "provider_site";
	
	private final String PROP_SERVICE_TITLE = "service_title";
	private final String PROP_SERVICE_KEYWORDS = "service_keywords";
	private final String PROP_SERVICE_FEES = "service_fees";
	
	private final String PROP_CONTACT_NAME = "contact_name";
	private final String PROP_CONTACT_PHONE = "contact_phone";
	private final String PROP_CONTACT_EMAIL = "contact_email";
	
	private final String PROP_USED_SERVICE_URL = "get_service_url";
	private final String PROP_OFFERINGS = "offerings";
	
	private Service service; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public SosServicePropertySource() {
		
	}

	/**
	 * Constructor 
	 * 
	 * @param service
	 */
	public SosServicePropertySource(Service service) {
		this.service = service;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		PropertyDescriptor providerName = 
				new TextPropertyDescriptor(PROP_PROVIDER_NAME, "Provider name");
		providerName.setCategory(CAT_PROVIDER);
		
		PropertyDescriptor providerSite = 
				new TextPropertyDescriptor(PROP_PROVIDER_SITE, "Provider site");
		providerSite.setCategory(CAT_PROVIDER);

		PropertyDescriptor serviceTitle = 
				new TextPropertyDescriptor(PROP_SERVICE_TITLE, "Title");
		serviceTitle.setCategory(CAT_SERVICE);

		PropertyDescriptor serviceKeywords = 
				new TextPropertyDescriptor(PROP_SERVICE_KEYWORDS, "Keywords");
		serviceKeywords.setCategory(CAT_SERVICE);

		PropertyDescriptor serviceFees = 
				new TextPropertyDescriptor(PROP_SERVICE_FEES, "Fees");
		serviceFees.setCategory(CAT_SERVICE);
		
		PropertyDescriptor contactName = 
				new TextPropertyDescriptor(PROP_CONTACT_NAME, "Contact name");
		contactName.setCategory(CAT_CONTACT);

		PropertyDescriptor contactPhone = 
				new TextPropertyDescriptor(PROP_CONTACT_PHONE, "Phone");
		contactPhone.setCategory(CAT_CONTACT);

		PropertyDescriptor contactEmail = 
				new TextPropertyDescriptor(PROP_CONTACT_EMAIL, "E-mail");
		contactEmail.setCategory(CAT_CONTACT);		
		
		PropertyDescriptor getServiceUrl = 
				new TextPropertyDescriptor(PROP_USED_SERVICE_URL, 
						"Service URL");

		PropertyDescriptor offerings = 
				new TextPropertyDescriptor(PROP_OFFERINGS, 
						"Number of offerings");
		
		return new IPropertyDescriptor[] {
				providerName, 
				providerSite, 
				serviceTitle, 
				serviceKeywords, 
				serviceFees, 
				contactName,
				contactPhone,
				contactEmail, 
				getServiceUrl, 
				offerings, 
		};
	}
	
	@Override
	public Object getPropertyValue(Object id) {
		
		if (service == null)
			return null;
		
		SosCapabilities capabilities = 
					SosCapabilitiesCache.getInstance().get(service.getServiceUrl());
		
		if (capabilities == null)
			return null;
		
		ServiceProvider serviceProvider = capabilities.getServiceProvider();
		ServiceIdentification serviceIdentification = 
				capabilities.getServiceIdentification();
		
		if (id.equals(PROP_PROVIDER_NAME)) {
			if (serviceProvider != null) {
				return serviceProvider.getName();
			}
		} else if (id.equals(PROP_PROVIDER_SITE)) {
			if (serviceProvider != null) {
				return serviceProvider.getSite();
			}
		} else if (id.equals(PROP_SERVICE_TITLE)) {
			if (serviceIdentification != null) {
				return serviceIdentification.getTitle();
			}
		} else if (id.equals(PROP_SERVICE_KEYWORDS)) {
			if (serviceIdentification != null) {
				return Util.join(serviceIdentification.getKeywords(), ",");
			}
		} else if (id.equals(PROP_SERVICE_FEES)) {
			if (capabilities != null) {
				return serviceIdentification.getFees();
			}
		} else if (id.equals(PROP_CONTACT_NAME)) {
			if (capabilities != null) {
				return SosUtil.getServiceContactName(capabilities);
			}
		} else if (id.equals(PROP_CONTACT_PHONE)) {
			if (capabilities != null) { 
				return SosUtil.getServiceContactPhone(capabilities);
			}
		} else if (id.equals(PROP_CONTACT_EMAIL)) {
			if (capabilities != null) { 
				return SosUtil.getServiceContactEmail(capabilities);
			}
		} else if (id.equals(PROP_USED_SERVICE_URL)) {
			if (capabilities != null) { 
				return service.getServiceUrl();
			}
		} else if (id.equals(PROP_OFFERINGS)) {
			if (capabilities != null) { 
				return capabilities.getOfferings().size();
			}
		}
		
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		
	}

}

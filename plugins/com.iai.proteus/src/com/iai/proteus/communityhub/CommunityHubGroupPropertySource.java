package com.iai.proteus.communityhub;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Property provider for Source objects 
 * 
 * @author Jakob Henriksson
 *
 */
public class CommunityHubGroupPropertySource implements IPropertySource {
	
	private final String CAT_GROUP = "Community group";
	
	private final String PROP_GROUP_NAME = "group_name";
	private final String PROP_GROUP_DESC = "group_description";
	private final String PROP_GROUP_CREATED = "group_created";
	
	private Group group; 
	
	/**
	 * Default constructor 
	 * 
	 */
	public CommunityHubGroupPropertySource() {
		
	}

	/**
	 * Constructor 
	 * 
	 * @param group
	 */
	public CommunityHubGroupPropertySource(Group group) {
		this.group = group;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		PropertyDescriptor groupName = 
				new TextPropertyDescriptor(PROP_GROUP_NAME, "Name");
		groupName.setCategory(CAT_GROUP);
		
		PropertyDescriptor groupDescription = 
				new TextPropertyDescriptor(PROP_GROUP_DESC, "Description");
		groupDescription.setCategory(CAT_GROUP);

		PropertyDescriptor groupCreated = 
				new TextPropertyDescriptor(PROP_GROUP_CREATED, "Created");
		groupCreated.setCategory(CAT_GROUP);
		
		return new IPropertyDescriptor[] {
				groupName, 
				groupDescription, 
				groupCreated,
		};
	}
	
	@Override
	public Object getPropertyValue(Object id) {
		
		if (group != null) {
			if (id.equals(PROP_GROUP_NAME)) {
				return group.getName();
			} else if (id.equals(PROP_GROUP_DESC)) {
				return group.getDescription();
			} else if (id.equals(PROP_GROUP_CREATED)) {
				return group.getCreated();
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

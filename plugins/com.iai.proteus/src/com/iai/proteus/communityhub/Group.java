package com.iai.proteus.communityhub;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Model object for Community Groups on the Community Hub 
 * 
 * @author Jakob Henriksson
 *
 */
public class Group implements IAdaptable {
	
	private int id; 
	private String name;
	private String description; 
	private Date created;
	private String createdBy;
	private String admin; 
	
	private CommunityHubGroupPropertySource property;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}
	
	/**
	 * @param created the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
	
	/**
	 * @return the createBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createBy the createBy to set
	 */
	public void setCreatedBy(String createBy) {
		this.createdBy = createBy;
	} 
	
	/**
	 * @return the admin
	 */
	public String getAdmin() {
		return admin;
	}

	/**
	 * @param admin the admin to set
	 */
	public void setAdmin(String admin) {
		this.admin = admin;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Adapter
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		
		if (adapter == IPropertySource.class) {
			if (property == null) {
				// cache the source 
				property = new CommunityHubGroupPropertySource(this);
			}
			return property;
		}
		// default 
		return null;
	}	
}
/*
 * Copyright (C) 2013 Intelligent Automation Inc. 
 * 
 * All Rights Reserved.
 */
package com.iai.proteus.queryset.persist.v1;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Represents all stored details of a Query Set
 * 
 * @author Jakob Henriksson
 *
 */
public class QuerySet {
	
	// version of the serialization 
	String version;
	// the UUID of the query set
	String uuid;
	// the name/title of the query set
	String title;
	// when the query set was created 
	Date dateCreated;
	
	SectionSos sectionSos;
	SectionWms sectionWms;
	
	// the file where this query set is stored 
	File file;
	
	/**
	 * Constructor
	 */
	public QuerySet() {
		sectionSos = new SectionSos();
		sectionWms = new SectionWms();
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * @return the sectionSos
	 */
	public SectionSos getSectionSos() {
		return sectionSos;
	}

	/**
	 * @return the sectionWms
	 */
	public SectionWms getSectionWms() {
		return sectionWms;
	}


	/**
	 * SOS Section
	 * 
	 * @author Jakob Henriksson
	 */
	public class SectionSos {
		
		Collection<SosService> sosServices = new ArrayList<SosService>();
		Collection<SosObservedProperty> observedProperties = new ArrayList<SosObservedProperty>();
		SosBoundingBox boundingBox = new SosBoundingBox();
		
		/**
		 * @return the sosServices
		 */
		public Collection<SosService> getSosServices() {
			return sosServices;
		}
		/**
		 * @return the observedProperties
		 */
		public Collection<SosObservedProperty> getObservedProperties() {
			return observedProperties;
		}
		/**
		 * @return the boundingBox
		 */
		public SosBoundingBox getBoundingBox() {
			return boundingBox;
		}
	}
	
	
	public class SosService {
		
		String endpoint;
		String title; 
		boolean active;
		
		/**
		 * @return the endpoint
		 */
		public String getEndpoint() {
			return endpoint;
		}
		
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @return the active
		 */
		public boolean isActive() {
			return active;
		}
	}
	
	public class SosBoundingBox {
		Double latL;
		Double latU;
		Double lonL;
		Double lonU;
		
		/**
		 * Returns the bounding box as an array 
		 * 
		 * @return
		 */
		public double[] getAsArray() {
			if (latL != null && latU != null && lonL != null && lonU != null)
				return new double[] { latL, latU, lonL, lonU };
			return null;
		}
	}
	
	public class SosObservedProperty {

		String observedProperty;

		/**
		 * @return the observedProperty
		 */
		public String getObservedProperty() {
			return observedProperty;
		}
		
	}	
	
	/**
	 * WMS Section
	 * 
	 * @author Jakob Henriksson
	 */
	public class SectionWms {
		
		Collection<WmsSavedMap> maps = new ArrayList<WmsSavedMap>();

		/**
		 * @return the maps
		 */
		public Collection<WmsSavedMap> getMaps() {
			return maps;
		}
	}
	
	public class WmsSavedMap {
		String endpoint;
		String name;
		String title;
		String notes;
		boolean active;
		
		/**
		 * @return the endpoint
		 */
		public String getEndpoint() {
			return endpoint;
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
		
		/**
		 * @return the notes
		 */
		public String getNotes() {
			return notes;
		}
		
		/**
		 * @return the active
		 */
		public boolean isActive() {
			return active;
		}
				
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuerySet other = (QuerySet) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
}

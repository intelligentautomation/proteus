package com.iai.proteus.model.workspace;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Model;
import com.iai.proteus.model.services.ServiceRoot;

public class LayerRoot extends Model {
	
	private WorkspaceRoot projects; 
	private ServiceRoot sources;
	
	/**
	 * Constructor 
	 */
	private LayerRoot() {
		// defaults 
		projects = WorkspaceRoot.getInstance();
		sources = ServiceRoot.getInstance();
	}

	public void setServiceRoot(ServiceRoot sources) {
		this.sources = sources;
	}
	
	public void setProjectRoot(WorkspaceRoot projects) {
		this.projects = projects;
	}
	
	public List<Model> getChildren() {
		List<Model> all = new ArrayList<Model>();
		/* add the project one by one rather than the project root */
		for (Project project : projects) {
			all.add(project);
		}
		/* add the source root, each source will be displayed as sub items 
		 * but only do so if there are any sources */
		if (sources.getSize() > 0)
			all.add(sources);
		return all;
	}

	
	private static class SingletonHolder {
		public static final LayerRoot instance = new LayerRoot();
	}

	public static LayerRoot getInstance() {
		return SingletonHolder.instance;
	}	
	
	
	/**
	 * Serialized to:
	 * 
	 * <smt>
	 *  <sources>...</sources>
	 *  <projects>...</projects>
	 * </smt>
	 * 
	 */
	@Override 
	public Element serialize(Document document) {
		Element root = document.createElement("smt");
		// name spaces
		root.setAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
		root.setAttribute("xmlns:sos", "http://www.opengis.net/sos/1.0"); 
		root.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
		// sources
		root.appendChild(ServiceRoot.getInstance().serialize(document));
		// projects 
		root.appendChild(WorkspaceRoot.getInstance().serialize(document));
		return root;
	}
}

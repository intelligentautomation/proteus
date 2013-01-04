package com.iai.proteus.model.workspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Model;

public class Project extends Model implements Iterable<QueryLayer> {

	private static final long serialVersionUID = 1L;
	
//	private static final Logger log = Logger.getLogger(Project.class);

	private ArrayList<QueryLayer> layers;
	
	/**
	 * Constructor
	 * 
	 */
	public Project() {
		layers = new ArrayList<QueryLayer>();
	}
	
	/**
	 * Constructor 
	 * 
	 * @param name
	 */
	public Project(String name) {
		this();
		setName(name);
	}
	
	/**
	 * Sets the layers 
	 * 
	 * @param layers the layers to set
	 */
	public void setLayers(ArrayList<QueryLayer> layers) {
		for (QueryLayer queryLayer : layers) {
			addLayer(queryLayer);
		}
	}
	
	/**
	 * Returns the layers 
	 * 
	 * @return
	 */
	public ArrayList<QueryLayer> getLayers() {
		return layers;
	}

	/**
	 * Adds a layer to the project 
	 * 
	 * @param layer
	 */
	public void addLayer(QueryLayer layer) {
		// make the layer in-active by default
		layer.deactivate();
		// set this project as the parent of the added layer 
		layer.setParent(this); 
		layers.add(layer);
	}
	
	/**
	 * Removes the given layer 
	 * 
	 * @param layer
	 */
	public void removeLayer(QueryLayer layer) {
		int index = -1;
		int i = 0;
		for (QueryLayer queryLayer : getLayers()) {
			/**
			 * We are just using the name to find the right layer 
			 */
			if (queryLayer.getName().equals(layer.getName())) { 
				index = i;
				break;
			}
			i++;
		}
		if (index != -1)
			getLayers().remove(index);
	}
	
	/**
	 * Returns true if there is a query layer with the same name as the given
	 * one, false otherwise 
	 * 
	 * @param name
	 * @return
	 */
	public boolean hasDuplicateLayerName(String name) {
		for (QueryLayer queryLayer : this) {
			if (queryLayer.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the children of this model object in a tree view 
	 * 
	 * @return
	 */
	public Collection<Model> getChildren() {
		Collection<Model> children = new ArrayList<Model>();
		children.addAll(layers);
		// TODO: do this in a more general way 
//		children.add(plots);
//		children.add(manipulated);
		return children;
	}
	
	@Override
	public Iterator<QueryLayer> iterator() {
		return layers.iterator();
	}
	
	/**
	 * Serialized to:
	 * 
	 * <project>
	 * 	<name>...</name>
	 * 	<layers>...</layers>
	 * </project>
	 * 
	 */
	@Override 
	public Element serialize(Document document) {
		Element root = document.createElement("project");
		// name 
		Element name = document.createElement("name");
		name.setTextContent(getName());
		root.appendChild(name);
		// layers
		Element layers = document.createElement("layers");
		for (QueryLayer layer : this) {
			// append layers
			layers.appendChild(layer.serialize(document));
		}
		root.appendChild(layers);
		return root; 
	}
}

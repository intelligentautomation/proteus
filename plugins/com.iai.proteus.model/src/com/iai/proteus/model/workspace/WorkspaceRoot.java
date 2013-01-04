package com.iai.proteus.model.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.iai.proteus.model.Model;
import com.iai.proteus.model.event.WorkspaceEventType;

/**
 * The root of the user's workspace
 *
 * @author Jakob Henriksson
 *
 */
public class WorkspaceRoot extends Model implements Iterable<Project> {

	private static final long serialVersionUID = 1L;

	private ArrayList<Project> projects;

	static final Comparator<Project> ALPHABETICAL = new Comparator<Project>() {
		public int compare(Project e1, Project e2) {
			return e1.getName().compareTo(e2.getName());
		}
	};

	/**
	 * Constructor
	 *
	 * NOTE: This constructor is public to be bean-compliant. However,
	 *       the usage of this class is intended to follow the Singleton
	 *       pattern: WorkspaceRoot root = WorkspaceRoot.getInstance();
	 */
	public WorkspaceRoot() {
		projects = new ArrayList<Project>();
	}

	/**
	 * Adds a project
	 *
	 * @param project
	 */
	public synchronized void addProject(Project project) {
		projects.add(project);
		fireEvent(WorkspaceEventType.WORKSPACE_MODEL_UPDATED, project);
	}

	/**
	 * Removes a project
	 *
	 * @param project
	 */
	public synchronized void removeProject(Project project) {
		projects.remove(project);
		fireEvent(WorkspaceEventType.WORKSPACE_MODEL_UPDATED, project);
	}

	/**
	 * Returns the projects
	 *
	 * @return the projects
	 */
	public ArrayList<Project> getProjects() {
		return projects;
	}

	/**
	 * Sets the projects
	 *
	 * @param projects the projects to set
	 */
	public void setProjects(ArrayList<Project> projects) {
		this.projects = projects;
	}

	private static class SingletonHolder {
		public static final WorkspaceRoot instance = new WorkspaceRoot();
	}

	public static WorkspaceRoot getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public Iterator<Project> iterator() {
		/*
		 * Sort in alphabetical order first
		 */
		Collections.sort(projects, ALPHABETICAL);
		return projects.iterator();
	}

	/**
	 * Serialized to:
	 *
	 * <projects>
	 *  ...
	 * </projects>
	 *
	 */
	@Override
	public Element serialize(Document document) {
		Element root = document.createElement("projects");
		for (Project project : this) {
			root.appendChild(project.serialize(document));
		}
		return root;
	}
}

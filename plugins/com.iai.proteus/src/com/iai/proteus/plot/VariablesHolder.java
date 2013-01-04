package com.iai.proteus.plot;

import java.util.ArrayList;
import java.util.List;

import com.iai.proteus.common.sos.data.Field;



public class VariablesHolder {

	private List<Variables> variables;

	/**
	 * Private constructor
	 *
	 */
	private VariablesHolder() {
		variables = new ArrayList<Variables>();
	}

	/**
	 * Returns the variables if found, null otherwise
	 *
	 * @param offeringId
	 * @param observedProperty
	 * @return
	 */
	public Variables getVariables(String offeringId, String observedProperty) {
		for (Variables vars : variables) {
			if (vars.getSensorOfferingId().equals(offeringId) &&
					vars.getObservedProperty().equals(observedProperty)) {
				return vars;
			}
		}
		// default
		return null;
	}

	/**
	 * Adds variables if they do not already exist
	 *
	 * @param offeringId
	 * @param observedProperty
	 * @param vars
	 */
	public void addVariables(String offeringId, String observedProperty, List<Field> vars) {
		// only add if they don't already exists
		if (getVariables(offeringId, observedProperty) == null) {
			Variables newVariables = new Variables(offeringId, observedProperty);
			newVariables.setVariables(vars);
			this.variables.add(newVariables);
		}
	}

	/*
	 * Singleton holder
	 *
	 */

	private static class SingletonHolder {
		public static final VariablesHolder instance = new VariablesHolder();
	}

	public static VariablesHolder getInstance() {
		return SingletonHolder.instance;
	}


}

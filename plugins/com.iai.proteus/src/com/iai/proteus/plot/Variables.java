package com.iai.proteus.plot;

import java.util.ArrayList;
import java.util.List;

import com.iai.proteus.common.sos.data.Field;



public class Variables {

	private String sensorOfferingId;
	private String observedProperty;
	private List<Field> variables;

	// true if variables have been set, false otherwise
	private boolean variablesSet;

	/**
	 * Constructor
	 *
	 * @param offeringId
	 * @param property
	 */
	public Variables(String offeringId, String property) {
		this.sensorOfferingId = offeringId;
		this.observedProperty = property;
		variables = new ArrayList<Field>();
		variablesSet = false;
	}

	public void setVariables(List<Field> variables) {
		this.variables = variables;
		variablesSet = true;
	}

	public boolean variablesSet() {
		return variablesSet;
	}

	public List<Field> getVariables() {
		return variables;
	}

	public String[] getVariableStrings() {
		String[] vars = new String[getVariables().size()];
		int i = 0;
		for (Field field : getVariables()) {
			vars[i] = field.getName();
			i++;
		}
		return vars;
	}

	public String getSensorOfferingId() {
		return sensorOfferingId;
	}

	public String getObservedProperty() {
		return observedProperty;
	}

}

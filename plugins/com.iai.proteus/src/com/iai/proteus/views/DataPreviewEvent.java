package com.iai.proteus.views;

import java.util.Collection;

import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.SensorData;



public class DataPreviewEvent {

	private String offeringId;
	private String observedProperty;

	private SensorData sensorData;
	private Field domainVariable;
	private Collection<Field> rangeVariasbles;

	/**
	 * Constructor
	 *
	 * @param offeringId
	 * @param observedProperty
	 * @param sensorData
	 * @param domainVariable
	 * @param rangeVariasbles
	 */
	public DataPreviewEvent(String offeringId, String observedProperty,
			SensorData sensorData, Field domainVariable,
			Collection<Field> rangeVariasbles)
	{
		this.offeringId = offeringId;
		this.observedProperty = observedProperty;
		this.sensorData = sensorData;
		this.domainVariable = domainVariable;
		this.rangeVariasbles = rangeVariasbles;
	}

	/**
	 * @return the offeringId
	 */
	public String getOfferingId() {
		return offeringId;
	}

	/**
	 * @return the observedProperty
	 */
	public String getObservedProperty() {
		return observedProperty;
	}

	/**
	 * @return the sensorData
	 */
	public SensorData getSensorData() {
		return sensorData;
	}

	/**
	 * @return the domainVariable
	 */
	public Field getDomainVariable() {
		return domainVariable;
	}

	/**
	 * @return the rangeVariasbles
	 */
	public Collection<Field> getRangeVariasbles() {
		return rangeVariasbles;
	}

}

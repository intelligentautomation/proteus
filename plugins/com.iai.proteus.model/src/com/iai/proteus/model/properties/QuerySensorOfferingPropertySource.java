package com.iai.proteus.model.properties;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.iai.proteus.common.Util;
import com.iai.proteus.common.sos.model.SensorOffering;
import com.iai.proteus.common.sos.model.SosCapabilities;
import com.iai.proteus.common.sos.util.SosUtil;
import com.iai.proteus.model.services.Service;
import com.iai.proteus.model.workspace.QuerySensorOffering;

/**
 * Property provider for QuerySensorOffering objects objects
 *
 * @author Jakob Henriksson
 *
 */
public class QuerySensorOfferingPropertySource implements IPropertySource {

	private QuerySensorOffering queryOffering;

	private final String CAT_OFFERING = "Offering";
	private final String CAT_TECHNICAL = "Technical";
	private final String CAT_SENSOR = "Sensor";
	private final String CAT_CONSTRAINTS = "Constraints";

	private final String PROP_OFFERING_ID = "offering_id";
	private final String PROP_OFFERING_NAME = "offering_name";
	private final String PROP_OFFERING_DESC = "offering_description";

	private final String PROP_RESULT_MODEL = "result_model";
	private final String PROP_RESPONSE_MODE = "response_mode";
	private final String PROP_RESPONSE_FORMATS = "response_formats";

	private final String PROP_FEATURES_OF_INTEREST = "features_of_interest";
	private final String PROP_OBSERVED_PROPERTIES = "observed_properties";

	private final String PROP_START_TIME = "start_time";
	private final String PROP_END_TIME = "end_time";
	private final String PROP_BOUNDING_BOX = "bounding_box";

	/**
	 * Default constructor
	 */
	public QuerySensorOfferingPropertySource() {

	}

	/**
	 * Constructor
	 *
	 * @param offering
	 */
	public QuerySensorOfferingPropertySource(QuerySensorOffering offering) {
		this.queryOffering = offering;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {

		PropertyDescriptor offeringId =
				new TextPropertyDescriptor(PROP_OFFERING_ID, "ID");
		offeringId.setCategory(CAT_OFFERING);

		PropertyDescriptor offeringName =
				new TextPropertyDescriptor(PROP_OFFERING_NAME, "Name");
		offeringName.setCategory(CAT_OFFERING);

		PropertyDescriptor offeringDesc =
				new TextPropertyDescriptor(PROP_OFFERING_DESC, "Description");
		offeringDesc.setCategory(CAT_OFFERING);

		PropertyDescriptor offeringResultModel =
				new TextPropertyDescriptor(PROP_RESULT_MODEL, "Result model");
		offeringResultModel.setCategory(CAT_TECHNICAL);

		PropertyDescriptor offeringResponseMode =
				new TextPropertyDescriptor(PROP_RESPONSE_MODE,
						"Response mode");
		offeringResponseMode.setCategory(CAT_TECHNICAL);

		PropertyDescriptor offeringResponseFormats =
				new TextPropertyDescriptor(PROP_RESPONSE_FORMATS,
						"Response formats");
		offeringResponseFormats.setCategory(CAT_TECHNICAL);

		PropertyDescriptor offeringFeaturesOfInterest =
				new TextPropertyDescriptor(PROP_FEATURES_OF_INTEREST,
						"Features of interest");
		offeringFeaturesOfInterest.setCategory(CAT_SENSOR);

		PropertyDescriptor offeringObservedProperties =
				new TextPropertyDescriptor(PROP_OBSERVED_PROPERTIES,
						"Observed properties");
		offeringObservedProperties.setCategory(CAT_SENSOR);

		PropertyDescriptor offeringStartTime =
				new TextPropertyDescriptor(PROP_START_TIME,
						"Start time");
		offeringStartTime.setCategory(CAT_CONSTRAINTS);

		PropertyDescriptor offeringEndTime =
				new TextPropertyDescriptor(PROP_END_TIME,
						"End time");
		offeringEndTime.setCategory(CAT_CONSTRAINTS);

		PropertyDescriptor offeringBoundingBox =
				new TextPropertyDescriptor(PROP_BOUNDING_BOX,
						"Bounding box");
		offeringBoundingBox.setCategory(CAT_CONSTRAINTS);


		return new IPropertyDescriptor[] {

				offeringId,
				offeringName,
				offeringDesc,

				offeringResultModel,
				offeringResponseMode,
				offeringResponseFormats,

				offeringFeaturesOfInterest,
				offeringObservedProperties,

				offeringStartTime,
				offeringEndTime,
				offeringBoundingBox
		};
	}

	@Override
	public Object getPropertyValue(Object id) {

		SensorOffering sensorOffering = queryOffering.getSensorOffering();

		// populate the sensor offering if needed
		if (!sensorOffering.isLoaded()) {
			Service service = queryOffering.getProvenance().getService();
			SosCapabilities capabilities =
					SosUtil.getCapabilities(service.getServiceUrl());
			sensorOffering.loadSensorOffering(capabilities);
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		if (id.equals(PROP_OFFERING_ID)) {
			return sensorOffering.getGmlId();
		} else if (id.equals(PROP_OFFERING_NAME)) {
			return sensorOffering.getName();
		} else if (id.equals(PROP_OFFERING_DESC)) {
			return sensorOffering.getDescription();
		} else if (id.equals(PROP_RESULT_MODEL)) {
			return sensorOffering.getResultModel();
		} else if (id.equals(PROP_RESPONSE_MODE)) {
			return sensorOffering.getResponseMode();
		} else if (id.equals(PROP_RESPONSE_FORMATS)) {
			return Util.join(sensorOffering.getResponseFormats(), ",");
		} else if (id.equals(PROP_FEATURES_OF_INTEREST)) {
			return Util.join(sensorOffering.getFeaturesOfInterest(), ",");
		} else if (id.equals(PROP_OBSERVED_PROPERTIES)) {
			return Util.join(sensorOffering.getObservedProperties(), ",");
		} else if (id.equals(PROP_START_TIME)) {
			Date start = sensorOffering.getStartTime();
			if (start != null)
				return format.format(start);
			return null;
		} else if (id.equals(PROP_END_TIME)) {
			Date end = sensorOffering.getEndTime();
			if (end != null)
				return format.format(end);
			return null;
		} else if (id.equals(PROP_BOUNDING_BOX)) {
			return SosUtil.getBoundingBox(sensorOffering);
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

	/**
	 * @return the queryOffering
	 */
	public QuerySensorOffering getQueryOffering() {
		return queryOffering;
	}

	/**
	 * @param queryOffering the queryOffering to set
	 */
	public void setQueryOffering(QuerySensorOffering queryOffering) {
		this.queryOffering = queryOffering;
	}

}

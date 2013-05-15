package com.iai.proteus.csw;

import java.io.File;
import java.io.IOException;


import org.apache.commons.io.FileUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;

import com.iai.proteus.common.Util;
import com.iai.proteus.csw.parser.GetRecordsResponseParser;

public class PacIOOSWAF {
	
	protected class BB {
		public BB() {
			
		}
		public String lowerCorner;
		public String upperCorner;
	}
	
	protected void test() throws IOException {
		
		File file = new File("resources/templates/queryTemplate6.stg");
		String template = FileUtils.readFileToString(file);
		STGroup group = new STGroupString(template);
		ST st = group.getInstanceOf("query");
		
//		st.add("serviceType", "urn:ogc:serviceType:WebMapService");
//		st.add("serviceType", "urn:ogc:serviceType:SensorObservationService");
//		st.add("searchstrings", new String[] { "birds", "fowls" } );
		
		BB bb = new BB();
		bb.lowerCorner = "10.0 -20.2";
		bb.upperCorner = "123.0 123.1";
		
//		st.add("boundingboxes", new BB[] { bb } );

		String request = st.render();
		
		System.out.println("Request: " + request);		
		
		String service = "http://www.ngdc.noaa.gov/geoportal/csw";
//		String service = "http://geodiscover.cgdi.ca/wes/serviceManagerCSW/csw";
		
		String response = Util.post(service, request);
		
		System.out.println("Response: " + response);
		
		GetRecordsResponse getRecordsResponse = 
			new GetRecordsResponseParser().parse(response);
		
		for (Record record : getRecordsResponse.getRecords()) {
			System.out.println("Record title: " + record.getTitle());
			System.out.println("Source: " + record.getSource());
		}
		
	}
	

	public static void main(String[] args) throws IOException {
		
		new PacIOOSWAF().test();
		
	}
	
}

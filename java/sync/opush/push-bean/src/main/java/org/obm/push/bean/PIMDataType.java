package org.obm.push.bean;

import org.obm.push.exception.PIMDataTypeNotFoundException;

public enum PIMDataType {

	EMAIL("Email"), 
	CALENDAR("Calendar"), 
	CONTACTS("Contacts"), 
	TASKS("Tasks");
	
	private final String xmlValue;

	private PIMDataType(String xmlValue) {
		this.xmlValue = xmlValue;
	}
	
	public static PIMDataType getPIMDataType(String collectionPath) throws PIMDataTypeNotFoundException {
		if (collectionPath.contains("\\calendar\\")) {
			return PIMDataType.CALENDAR;
		} else if (collectionPath.contains("\\contacts")) {
			return PIMDataType.CONTACTS;
		} else if (collectionPath.contains("\\email")) {
			return PIMDataType.EMAIL;
		} else if (collectionPath.contains("\\tasks")) {
			return PIMDataType.TASKS;
		} else {
			throw new PIMDataTypeNotFoundException("PIMDataType " + collectionPath + " not found.");
		}
	}

	public String asXmlValue() {
		return xmlValue;
	}

}

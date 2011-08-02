package org.obm.push.bean;

public enum PIMDataType {

	EMAIL, CALENDAR, CONTACTS, TASKS, FOLDER;

	public String asXmlValue() {
		switch (this) {
		case CALENDAR:
			return "Calendar";
		case CONTACTS:
			return "Contacts";
		case TASKS:
			return "Tasks";
		case EMAIL:
			return "Email";
		default :
			return "";
		}
	}
	
	public static PIMDataType getPIMDataType(String collectionPath) {
		if (collectionPath.contains("\\calendar\\")) {
			return PIMDataType.CALENDAR;
		} else if (collectionPath.contains("\\contacts")) {
			return PIMDataType.CONTACTS;
		} else if (collectionPath.contains("\\email\\")) {
			return PIMDataType.EMAIL;
		} else if (collectionPath.contains("\\tasks\\")) {
			return PIMDataType.TASKS;
		} else {
			return PIMDataType.FOLDER;
		}
	}

}

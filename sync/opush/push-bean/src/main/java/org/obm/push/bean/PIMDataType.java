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

}

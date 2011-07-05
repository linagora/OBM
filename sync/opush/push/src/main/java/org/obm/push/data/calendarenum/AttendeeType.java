package org.obm.push.data.calendarenum;

public enum AttendeeType {
	REQUIRED, // 1
	OPTIONAL, // 2
	RESOURCE; // 3

	public String asIntString() {
		switch (this) {
		case OPTIONAL:
			return "2";
		case RESOURCE:
			return "3";
		default:
		case REQUIRED:
			return "1";
		}

	}
}

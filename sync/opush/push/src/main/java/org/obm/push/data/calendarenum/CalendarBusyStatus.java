package org.obm.push.data.calendarenum;

public enum CalendarBusyStatus {
	FREE, // 0
	TENTATIVE, // 1
	BUSY, // 2
	OUT_OF_OFFICE; // 3
	
	public String asIntString() {
		switch (this) {
		case FREE:
			return "0";
		case TENTATIVE:
			return "1";
		case BUSY:
			return "2";
		case OUT_OF_OFFICE:
			return "3";
		default:
			return "4";
		}
	}
}

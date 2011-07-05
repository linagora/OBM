package org.obm.push.data.calendarenum;

public enum CalendarMeetingStatus {

	IS_NOT_IN_MEETING, // 0
	IS_IN_MEETING, // 1
	MEETING_RECEIVED, // 3
	MEETING_IS_CANCELED, // 5
	MEETING_IS_CANCELED_AND_RECEIVED; // 7

	public String asIntString() {
		switch (this) {
		case MEETING_IS_CANCELED:
			return "5";
		case MEETING_IS_CANCELED_AND_RECEIVED:
			return "7";
		case MEETING_RECEIVED:
			return "3";
		case IS_IN_MEETING:
			return "1";

		default:
		case IS_NOT_IN_MEETING:
			return "0";

		}
	}
}

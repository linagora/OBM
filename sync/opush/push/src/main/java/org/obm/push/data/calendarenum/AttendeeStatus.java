package org.obm.push.data.calendarenum;

public enum AttendeeStatus {
	RESPONSE_UNKNOWN, // 0
	TENTATIVE, // 2
	ACCEPT, // 3
	DECLINE, // 4
	NOT_RESPONDED; // 5

	public String asIntString() {
		switch (this) {
		case ACCEPT:
			return "3";
		case DECLINE:
			return "4";
		case NOT_RESPONDED:
			return "5";
		case TENTATIVE:
			return "2";
		default:
		case RESPONSE_UNKNOWN:
			return "0";

		}

	}
}

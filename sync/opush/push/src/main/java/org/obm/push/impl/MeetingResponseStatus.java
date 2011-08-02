package org.obm.push.impl;

public enum MeetingResponseStatus {

	SUCCESS, // 1
	INVALID_MEETING_RREQUEST, // 2
	SERVER_MAILBOX_ERROR, // 3
	SERVER_ERROR; // 4

	public String asXmlValue() {
		switch (this) {
		case INVALID_MEETING_RREQUEST:
			return "2";
		case SERVER_MAILBOX_ERROR:
			return "3";
		case SERVER_ERROR:
			return "4";
		case SUCCESS:
		default:
			return "1";
		}
	}

}

package org.obm.push.impl;

public enum PingStatus {

	NO_CHANGES, // 1
	CHANGES_OCCURED, // 2
	MISSING_REQUEST_PARAMS, // 3
	SYNTAX_ERROR_IN_REQUEST, // 4
	INVALID_HEARTBEAT_INTERVAL, // 5
	TOO_MANY_FOLDERS, // 6
	FOLDER_SYNC_REQUIRED, // 7
	SERVER_ERROR; // 8

	public String asXmlValue() {
		switch (this) {
		case CHANGES_OCCURED:
			return "2";
		case MISSING_REQUEST_PARAMS:
			return "3";
		case SYNTAX_ERROR_IN_REQUEST:
			return "4";
		case INVALID_HEARTBEAT_INTERVAL:
			return "5";
		case TOO_MANY_FOLDERS:
			return "6";
		case FOLDER_SYNC_REQUIRED:
			return "7";
		case SERVER_ERROR:
			return "8";

		case NO_CHANGES:
		default:
			return "1";
		}
	}

}

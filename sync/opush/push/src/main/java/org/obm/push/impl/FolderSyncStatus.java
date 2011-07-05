package org.obm.push.impl;

public enum FolderSyncStatus {
    OK,//1
    SERVER_ERROR, //6 An error occurred on the server.
    ACCESS_DENIED,//7 Access denied.
    TIMED_OUT,//8 The request timed out.                                      
    INVALID_SYNC_KEY,//9 Synchronization key mismatch or invalid synchronization key.
    INVALID_REQUEST,//10 Incorrectly formatted request.
    UNKNOW_ERROR;//11 An unknown error occurred.
    
    public String asXmlValue() {
		switch (this) {
		case SERVER_ERROR:
			return "6";
		case ACCESS_DENIED:
			return "7";
		case TIMED_OUT:
			return "8";
		case INVALID_SYNC_KEY:
			return "9";
		case INVALID_REQUEST:
			return "10";
		case UNKNOW_ERROR:
			return "11";
		case OK:
		default:
			return "1";
		}
	}
}

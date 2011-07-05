package org.obm.push.impl;

public enum SearchStatus {
	
	SUCCESS, //1
	PROTOCOL_VIOLATION,// 2 XML validation error.
	SERVER_ERROR,// 3
	BAD_LINK, // 4
	ACCESS_DENIED, // 5
	NOT_FOUND, // 6
	CONNECTION_FAILED,// 7 
	QUERY_TOO_COMPLEX,// 8 The search query is too complex.
	INDEXING_NOT_LOADED,// 9 Unable to execute this query because Content Indexing is not loaded.
	TIME_OUT, // 10
	BAD_COLLECTION_ID, // 11 Bad CollectionId (the client MUST perform a FolderSync).
	END_OF_RANGE,// 12 Server reached the end of the range that is retrievable by synchronization.
	ACCESS_BLOCKED, // 13 Access Blocked (policy restriction)
	CREDENTIALS_REQUIRED; // 14 Credentials Required to Continue

	
	public String asXmlValue() {
		switch (this) {
		case PROTOCOL_VIOLATION:
			return "2";
		case SERVER_ERROR:
			return "3";
		case BAD_LINK:
			return "4";
		case ACCESS_DENIED:
			return "5";
		case NOT_FOUND:
			return "6";
		case CONNECTION_FAILED:
			return "7";
		case QUERY_TOO_COMPLEX:
			return "8";
		case INDEXING_NOT_LOADED:
			return "9";
		case TIME_OUT:
			return "10";
		case BAD_COLLECTION_ID:
			return "11";
		case END_OF_RANGE:
			return "12";
		case ACCESS_BLOCKED:
			return "13";
		case CREDENTIALS_REQUIRED:
			return "14";
		case SUCCESS:
		default:
			return "1";
		}
	}
}

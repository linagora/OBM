package org.obm.push.client.tests;

/**
 * Possible values for the status element in Sync reponses
 * 
 * @author tom
 *
 */
public enum SyncStatus {

	OK, // 1
	PROTOCOL_VERSION_MISMATCH, // 2
	INVALID_SYNC_KEY, // 3
	PROTOCOL_ERROR, // 4
	SERVER_ERROR, // 5
	CONVERSATION_ERROR, // 6
	CONFLICT, // 7
	OBJECT_NOT_FOUND, // 8
	OUT_OF_DISK_SPACE, // 9
	NOTIFICATION_GUID_ERROR, // 10
	NOT_YET_PROVISIONNED, // 11
	HIERARCHY_CHANGED, // 12
	PARTIAL_REQUEST, // 13
	WAIT_INTERVAL_OUT_OF_RANGE, // 14
	TO_MUCH_FOLDER_TO_MONITOR, // 15
	NEED_RETRY; // 16

	public String asXmlValue() {
		switch (this) {
		case CONFLICT:
			return "7";
		case CONVERSATION_ERROR:
			return "6";
		case HIERARCHY_CHANGED:
			return "12";
		case INVALID_SYNC_KEY:
			return "3";
		case NEED_RETRY:
			return "16";
		case NOTIFICATION_GUID_ERROR:
			return "10";
		case NOT_YET_PROVISIONNED:
			return "11";
		case OBJECT_NOT_FOUND:
			return "8";
		case OUT_OF_DISK_SPACE:
			return "9";
		case PARTIAL_REQUEST:
			return "13";
		case PROTOCOL_ERROR:
			return "4";
		case PROTOCOL_VERSION_MISMATCH:
			return "2";
		case SERVER_ERROR:
			return "5";
		case TO_MUCH_FOLDER_TO_MONITOR:
			return "15";
		case WAIT_INTERVAL_OUT_OF_RANGE:
			return "14";

		case OK:
		default:
			return "1";
		}
	}
}
